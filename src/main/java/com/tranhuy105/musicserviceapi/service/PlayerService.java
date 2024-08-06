package com.tranhuy105.musicserviceapi.service;

import com.tranhuy105.musicserviceapi.dto.TrackQueueDto;
import com.tranhuy105.musicserviceapi.exception.StreamingException;
import com.tranhuy105.musicserviceapi.model.*;
import com.tranhuy105.musicserviceapi.utils.CachePrefix;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PlayerService {
    private final CacheService cacheService;
    private final TrackService trackService;
    private final StreamingSourceService streamingSourceService;

    private final Logger logger = LoggerFactory.getLogger(PlayerService.class);
    private static final long VALID_STREAM_THRESHOLD = 30000;
    private static final int QUEUE_SIZE = 30;

    public StreamingSession getStreamingSession(User user) {
        return cacheService.getStreamingSessionCache(user.getId());
    }

    public void playTrack(String deviceId,
                          User user,
                          Long trackId,
                          @NonNull StreamingSource streamingSource,
                          PlaybackMode playbackMode) {
        String sessionLock = getStreamingSessionLock(user);
        cacheService.executeWithLock(sessionLock, () -> {
            StreamingSession session = getStreamingSession(user);
            if (session == null) {
                session = createStreamingSession(user, streamingSource, deviceId, playbackMode);
            }
            session.validateDevice(deviceId);

            TrackDetail track = trackService.findTrackById(trackId);
            session.resetHistoryIndex();
            switchSessionTrack(session, track);

            if ((!Objects.equals(streamingSource, session.getStreamingSource()) || session.getTrackQueue().isEmpty())) {
                session.setStreamingSource(streamingSource);
                session.setTrackQueue(generateTrackQueue(session));
            }
            cacheService.cacheStreamingSession(user.getId(), session);
        });
    }

    public Long nextTrack(String deviceId, User user) {
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
            ensureQueueIsNotEmpty(session);
            TrackDetail nextTrack = trackService.findTrackById(session.getTrackQueue().poll());
            session.resetHistoryIndex();
            switchSessionTrack(session, nextTrack);
            cacheService.cacheStreamingSession(user.getId(), session);
            return session.getCurrentTrack().getId();
        });
    }

    public Long prevTrack(String deviceId, User user) {
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
                return session.getCurrentTrack().getId();
            }

            switchSessionTrack(session, prevTrack.get());
            cacheService.cacheStreamingSession(user.getId(), session);
            return session.getCurrentTrack().getId();
        });
    }

    private Optional<TrackDetail> getTrackFromHistory(User user, int index) {
        String key = cacheService.getCacheKey(CachePrefix.STREAMING_HISTORY, user.getId());
        Object historyObject = cacheService.getRedisTemplate().opsForList().index(key, index);

        if (historyObject instanceof StreamingHistory) {
            return Optional.of(((StreamingHistory) historyObject).getTrack());
        }

        return Optional.empty();
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
            // generate new queue for new playmode
            session.setTrackQueue(generateTrackQueue(session));

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
            session.getTrackQueue().add(newTrackId);
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

    private Queue<Long> generateTrackQueue(StreamingSession session) {
        Queue<Long> trackQueue = new LinkedList<>();

        if (session.getPlaybackMode() == PlaybackMode.REPEAT) {
            if (session.getCurrentTrack() != null) {
                trackQueue.add(session.getCurrentTrack().getId());
            }
            return trackQueue;
        }

        List<TrackQueueDto> tracks = streamingSourceService.getTracks(session.getStreamingSource());

        if (tracks.isEmpty()) {
            if (session.getCurrentTrack() != null) {
                trackQueue.add(session.getCurrentTrack().getId());
            }
            return trackQueue;
        }


        PlaybackMode playbackMode = session.getPlaybackMode();
        Long currentTrackId = session.getCurrentTrack() != null ? session.getCurrentTrack().getId() : null;

        if (playbackMode == PlaybackMode.SHUFFLE) {
            Collections.shuffle(tracks);
            trackQueue.addAll(tracks.stream().map(TrackQueueDto::getTrackId).limit(QUEUE_SIZE).toList());
        }

        if (playbackMode == PlaybackMode.SEQUENTIAL) {
            tracks.sort(Comparator.comparingInt(TrackQueueDto::getPosition));
            if (currentTrackId != null) {
                int currentIndex = tracks.stream()
                        .map(TrackQueueDto::getTrackId)
                        .toList()
                        .indexOf(currentTrackId);

                if (currentIndex != -1) {
                    // Add tracks from current position to the end
                    // plus 1 to skip the current track to be added again to the queue
                    for (int i = currentIndex + 1; i < tracks.size() && trackQueue.size() < QUEUE_SIZE; i++) {
                        trackQueue.add(tracks.get(i).getTrackId());
                    }
                    // Add tracks from the start to the current position if needed
                    for (int i = 0; i < currentIndex && trackQueue.size() < QUEUE_SIZE; i++) {
                        trackQueue.add(tracks.get(i).getTrackId());
                    }
                }
            } else {
                trackQueue.addAll(tracks.stream().map(TrackQueueDto::getTrackId).limit(QUEUE_SIZE).toList());
            }
        }

        return trackQueue;
    }

    private void ensureQueueIsNotEmpty(StreamingSession session) {
        if (session.getTrackQueue().isEmpty()) {
            session.setTrackQueue(generateTrackQueue(session));
        }
    }

    private String getStreamingSessionLock(User user) {
        return cacheService.getCacheKey(CachePrefix.SESSION_LOCK, user.getId());
    }

    private void saveStreamingHistory(Long userId, StreamingHistory history) {
        String key = cacheService.getCacheKey(CachePrefix.STREAMING_HISTORY, userId);
        cacheService.getRedisTemplate().opsForList().leftPush(key, history);
        if (history.getListeningTime() >= VALID_STREAM_THRESHOLD) {
            // TODO: Produce an event
            logger.info("Successful stream: " + history);
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
