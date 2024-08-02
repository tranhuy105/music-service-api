package com.tranhuy105.musicserviceapi.service;

import com.tranhuy105.musicserviceapi.dto.PlaylistTrackPositionDto;
import com.tranhuy105.musicserviceapi.exception.StreamingException;
import com.tranhuy105.musicserviceapi.model.*;
import com.tranhuy105.musicserviceapi.repository.api.PlaylistRepository;
import com.tranhuy105.musicserviceapi.utils.CachePrefix;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PlayerService {
    private final CacheService cacheService;
    private final TrackService trackService;
    private final PlaylistRepository playlistRepository;
    private final Logger logger = LoggerFactory.getLogger(PlayerService.class);
    private static final long VALID_STREAM_THRESHOLD = 30000;
    private static final int QUEUE_SIZE = 30;

    public StreamingSession getStreamingSession(User user) {
        return cacheService.getStreamingSessionCache(user.getId());
    }

    public void playTrack(String deviceId,
                          User user,
                          Long trackId,
                          Long playlistId,
                          PlaybackMode playbackMode) {
        String sessionLock = getStreamingSessionLock(user);
        cacheService.executeWithLock(sessionLock, () -> {
            StreamingSession session = getStreamingSession(user);
            if (session == null) {
                session = createStreamingSession(user, playlistId, deviceId, playbackMode);
            }
            session.validateDevice(deviceId);

            TrackDetail track = trackService.findTrackById(trackId);
            switchSessionTrack(session, user, track);

            if (playlistId != null) {
                if ((!Objects.equals(playlistId, session.getPlaylistId()) || session.getTrackQueue().isEmpty())) {
                    // generate new queue only when try to play a track from a new playlist or the queue itself is empty
                    session.setTrackQueue(generateTrackQueue(session));
                }
            }
            cacheService.cacheStreamingSession(user.getId(), session);
        });
    }

    public Long nextTrack(String deviceId, User user) {
        String sessionLock = getStreamingSessionLock(user);
        cacheService.executeWithLock(sessionLock, () -> {
            StreamingSession session = getStreamingSession(user);
            if (session == null) {
                throw new StreamingException("No session found");
            }

            if (session.getPlaylistId() == null) {
                throw new StreamingException("This session is not associated with any playlist");
            }

            session.validateDevice(deviceId);
            ensureQueueIsNotEmpty(session);
            TrackDetail nextTrack = trackService.findTrackById(session.getTrackQueue().poll());
            switchSessionTrack(session, user, nextTrack);
            cacheService.cacheStreamingSession(user.getId(), session);
        });
        return getStreamingSession(user).getCurrentTrack().getId();
    }

    public Long prevTrack(String deviceId, User user) {
        String sessionLock = getStreamingSessionLock(user);
        cacheService.executeWithLock(sessionLock, () -> {
            StreamingSession session = getStreamingSession(user);
            if (session == null) {
                throw new StreamingException("No session found");
            }
            session.validateDevice(deviceId);
            List<StreamingHistory> history = getSessionHistory(user);
            if (history.isEmpty()) {
                throw new StreamingException("Empty history");
            }

            switchSessionTrack(session, user, history.get(0).getTrack());
            cacheService.cacheStreamingSession(user.getId(), session);
        });
        return getStreamingSession(user).getCurrentTrack().getId();
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
            if (session.getPlaylistId() != null) {
                session.setTrackQueue(generateTrackQueue(session));
            }
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

    private void switchSessionTrack(StreamingSession session, User user, TrackDetail track) {
        if (session != null) {
            session.resume();
            StreamingHistory history = session.playTrack(track);
            if (history != null) {
                saveStreamingHistory(user.getId(), history);
            }
        }
    }

    private StreamingSession createStreamingSession(User user, Long playlistId, String deviceId, PlaybackMode playbackMode) {
        StreamingSession session = new StreamingSession(user, deviceId);
        session.setPlaylistId(playlistId);
        if (playbackMode != null) {
            session.setPlaybackMode(playbackMode);
        }
        return session;
    }

    private Queue<Long> generateTrackQueue(StreamingSession session) {
        Queue<Long> trackQueue = new LinkedList<>();

        if (session.getPlaybackMode() == PlaybackMode.REPEAT) {
            if (session.getCurrentTrack() != null) {
                trackQueue.add(session.getCurrentTrack().getId());
            }
            return trackQueue;
        }

        List<PlaylistTrackPositionDto> playlistTracks = playlistRepository.findAllPlaylistTracksByIdRaw(session.getPlaylistId());

        if (playlistTracks.isEmpty()) {
            throw new StreamingException("Playlist is empty");
        }


        PlaybackMode playbackMode = session.getPlaybackMode();
        Long currentTrackId = session.getCurrentTrack() != null ? session.getCurrentTrack().getId() : null;

        if (playbackMode == PlaybackMode.SHUFFLE) {
            Collections.shuffle(playlistTracks);
            trackQueue.addAll(playlistTracks.stream().map(PlaylistTrackPositionDto::getTrackId).limit(QUEUE_SIZE).toList());
        }

        if (playbackMode == PlaybackMode.SEQUENTIAL) {
            playlistTracks.sort(Comparator.comparingInt(PlaylistTrackPositionDto::getPosition));
            if (currentTrackId != null) {
                int currentIndex = -1;
                for (int i = 0; i < playlistTracks.size(); i++) {
                    if (playlistTracks.get(i).getTrackId().equals(currentTrackId)) {
                        currentIndex = i;
                        break;
                    }
                }

                if (currentIndex != -1) {
                    // Add tracks from current position to the end
                    // plus 1 to skip the current track to be added again to the queue
                    for (int i = currentIndex + 1; i < playlistTracks.size() && trackQueue.size() < QUEUE_SIZE; i++) {
                        trackQueue.add(playlistTracks.get(i).getTrackId());
                    }
                    // Add tracks from the start to the current position if needed
                    for (int i = 0; i < currentIndex && trackQueue.size() < QUEUE_SIZE; i++) {
                        trackQueue.add(playlistTracks.get(i).getTrackId());
                    }
                }
            } else {
            trackQueue.addAll(playlistTracks.stream().map(PlaylistTrackPositionDto::getTrackId).limit(QUEUE_SIZE).toList());
            }
        }

        return trackQueue;
    }

    private void ensureQueueIsNotEmpty(StreamingSession session) {
        if (session.getPlaylistId() != null) {
            if (session.getTrackQueue().isEmpty()) {
                session.setTrackQueue(generateTrackQueue(session));
            }
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
