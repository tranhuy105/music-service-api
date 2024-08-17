package com.tranhuy105.musicserviceapi.service;

import com.tranhuy105.musicserviceapi.exception.StreamingException;
import com.tranhuy105.musicserviceapi.model.*;
import com.tranhuy105.musicserviceapi.utils.CachePrefix;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PlayerService {
    private final CacheService cacheService;
    private final TrackService trackService;
    private final StorageService storageService;
    private final AdService adService;
    private final QueueService queueService;
    private final ApplicationEventPublisher eventPublisher;

    private final Logger logger = LoggerFactory.getLogger(PlayerService.class);
    private static final long VALID_STREAM_THRESHOLD = 30000;
    private static final int AD_INTERVAL = 10; // Play ad every 10 tracks for non-premium users

    public StreamingSession getStreamingSession(User user) {
        return cacheService.getStreamingSessionCache(user.getId());
    }

    public URL playTrack(String deviceId,
                          User user,
                          Long trackId,
                          @NonNull StreamingSource streamingSource,
                          PlaybackMode playbackMode) {
        String sessionLock = getStreamingSessionLock(user);
        return cacheService.executeWithLock(sessionLock, () -> {
            StreamingSession session = getStreamingSession(user);
            if (session == null) {
                session = createStreamingSession(user, streamingSource, deviceId, playbackMode);
                session.initializeAdSettings(AD_INTERVAL);
            }
            session.validateDevice(deviceId);

            session.getItemQueue().addFirst(new QueueItem(trackId, QueueItem.ItemType.TRACK));
            adService.processAdService(user, session);
            URL url = processToNextItemInQueue(session, user.getIsPremium());
            session.resetHistoryIndex();

            if ((!Objects.equals(streamingSource, session.getStreamingSource()) || session.getItemQueue().isEmpty())) {
                session.setStreamingSource(streamingSource);
                session.setItemQueue(queueService.generateTrackQueue(session));
            }

            cacheService.cacheStreamingSession(user.getId(), session);
            return url;
        });
    }

    public URL nextTrack(String deviceId, User user) {
        String sessionLock = getStreamingSessionLock(user);
        return cacheService.executeWithLock(sessionLock, () -> {
            StreamingSession session = getStreamingSession(user);
            if (session == null) {
                throw new StreamingException("No session found");
            }

            if (session.getStreamingSource() == null) {
                throw new StreamingException("Session is not associated with any source");
            }

            session.validateDevice(deviceId);
            queueService.ensureQueueIsNotEmpty(session);
            adService.processAdService(user, session);
            URL url = processToNextItemInQueue(session, user.getIsPremium());
            session.resetHistoryIndex();
            cacheService.cacheStreamingSession(user.getId(), session);
            return url;
        });
    }

    public URL prevTrack(String deviceId, User user) {
        String sessionLock = getStreamingSessionLock(user);
        return cacheService.executeWithLock(sessionLock, () -> {
            StreamingSession session = getStreamingSession(user);
            if (session == null) {
                throw new StreamingException("No session found");
            }
            session.validateDevice(deviceId);
            session.incrementHistoryIndex();

            Optional<TrackDetail> prevTrack = getTrackFromHistory(user, session.getHistoryIndex());
            if (prevTrack.isEmpty()){
                return storageService.generateUrl(session.getCurrentMedia(), user.getIsPremium());
            }

            switchSessionTrack(session, prevTrack.get());
            cacheService.cacheStreamingSession(user.getId(), session);
            return storageService.generateUrl(session.getCurrentMedia(), user.getIsPremium());
        });
    }

    public void pauseSession(String deviceId, User user) {
        String sessionLock = getStreamingSessionLock(user);
        cacheService.executeWithLock(sessionLock, () -> {
            StreamingSession session = getStreamingSession(user);
            if (session != null) {
                session.validateDevice(deviceId);
                session.pause();
                cacheService.cacheStreamingSession(user.getId(), session);
            }
        });
    }

    public void resumeSession(String deviceId, User user) {
        String sessionLock = getStreamingSessionLock(user);
        cacheService.executeWithLock(sessionLock, () -> {
            StreamingSession session = getStreamingSession(user);
            if (session != null) {
                session.validateDevice(deviceId);
                session.resume();
                cacheService.cacheStreamingSession(user.getId(), session);
            }
        });
    }

    public void changePlaybackMode(String deviceId, User user, PlaybackMode newMode) {
        String sessionLock = getStreamingSessionLock(user);
        cacheService.executeWithLock(sessionLock, () -> {
            StreamingSession session = getStreamingSession(user);
            if (session == null) {
                throw new StreamingException("No session found");
            }
            session.validateDevice(deviceId);
            session.setPlaybackMode(newMode);
            // generate new queue for new play mode
            session.setItemQueue(queueService.generateTrackQueue(session));

            cacheService.cacheStreamingSession(user.getId(), session);
        });
    }

    public void addToQueue(String deviceId, User user, Long newTrackId) {
        String sessionLock = getStreamingSessionLock(user);
        cacheService.executeWithLock(sessionLock, () -> {
            StreamingSession session = getStreamingSession(user);
            if (session == null) {
                throw new StreamingException("No session found");
            }
            session.validateDevice(deviceId);
            session.getItemQueue().add(new QueueItem(newTrackId, QueueItem.ItemType.TRACK));
            cacheService.cacheStreamingSession(user.getId(), session);
        });
    }

    public List<StreamingHistory> getSessionHistory(User user) {
        String key = cacheService.getCacheKey(CachePrefix.STREAMING_HISTORY, user.getId());
        List<Object> historyObjects = cacheService
                .getRedisTemplate()
                .opsForList()
                .range(key, 0, -1);
        return convertToListeningHistoryList(historyObjects);
    }

    private Optional<TrackDetail> getTrackFromHistory(User user, int index) {
        String key = cacheService.getCacheKey(CachePrefix.STREAMING_HISTORY, user.getId());
        Object historyObject = cacheService.getRedisTemplate().opsForList().index(key, index);

        if (historyObject instanceof StreamingHistory) {
            return Optional.of(((StreamingHistory) historyObject).getTrack());
        }

        return Optional.empty();
    }

    private URL processToNextItemInQueue(StreamingSession session, boolean isPremium) {
        QueueItem nextItem = session.getItemQueue().poll();
        if (nextItem == null) return null;
        if (nextItem.itemType() == QueueItem.ItemType.AD) {
            Advertisement ad = adService.getAdById(nextItem.id());
            adService.handleAdPlayback(ad, session);
            return storageService.generateUrl(ad, isPremium);
        } else {
            TrackDetail nextTrack = trackService.findTrackById(nextItem.id());
            session.resetHistoryIndex();
            switchSessionTrack(session, nextTrack);
            return storageService.generateUrl(nextTrack, isPremium);
        }
    }

    private void switchSessionTrack(StreamingSession session, TrackDetail track) {
        if (session != null) {
            session.resume();
            StreamingHistory history = session.playTrack(track);
            if (history != null) {
                saveStreamingHistory(session.getUserId(), history);
            }
        }
    }

    private StreamingSession createStreamingSession(User user, StreamingSource streamingSource, String deviceId, PlaybackMode playbackMode) {
        return new StreamingSession(user, deviceId, streamingSource, playbackMode);
    }

    private String getStreamingSessionLock(User user) {
        return cacheService.getCacheKey(CachePrefix.SESSION_LOCK, user.getId());
    }

    private void saveStreamingHistory(Long userId, StreamingHistory history) {
        String key = cacheService.getCacheKey(CachePrefix.STREAMING_HISTORY, userId);
        cacheService.getRedisTemplate().opsForList().leftPush(key, history);
        if (history.getListeningTime() >= VALID_STREAM_THRESHOLD) {
            // async event
            eventPublisher.publishEvent(new SuccessfulStreamEvent(this, userId, history));
        }
    }

    private List<StreamingHistory> convertToListeningHistoryList(List<Object> historyObjects) {
        List<StreamingHistory> listeningHistory = new ArrayList<>();
        if (historyObjects != null) {
            for (Object obj : historyObjects) {
                if (obj instanceof StreamingHistory) {
                    listeningHistory.add((StreamingHistory) obj);
                }
            }
        }
        return listeningHistory;
    }
}
