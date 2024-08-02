package com.tranhuy105.musicserviceapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.function.Supplier;

/**
 * Represents a streaming session for a user.
 */
@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
@NoArgsConstructor
public class StreamingSession {

    private Long userId;
    private boolean isPlaying; // Indicates if the session is currently playing pr pausing
    private TrackDetail currentTrack; // Current track being played

    private StreamingSource streamingSource;
    private PlaybackMode playbackMode = PlaybackMode.SHUFFLE;

    private long lastRecordedTime; // Timestamp of the last recorded activity
    private long accumulatedTime; // Total accumulated time for the current track (in milliseconds)
    private String deviceId; // Identifier for the device playing the track

    private Queue<Long> trackQueue= new LinkedList<>();
    // a pointer to track where is the previous track really is to avoid a loop between current and previous track
    private int historyIndex = -1;

    public StreamingSession(User user, String deviceId, StreamingSource streamingSource, PlaybackMode playbackMode) {
        this.userId = user.getId();
        this.isPlaying = false;
        this.currentTrack = null;
        this.streamingSource = streamingSource;
        if (playbackMode != null) {
            this.playbackMode = playbackMode;
        }
        this.deviceId = deviceId;
    }

    public void pause() {
        if (this.isPlaying) {
            updateTrackTime();
            this.isPlaying = false;
        }
    }

    public void resume() {
        if (!this.isPlaying) {
            this.isPlaying = true;
            this.lastRecordedTime = currentTimeMilisSupplier().get();
        }
    }

    public StreamingHistory playTrack(TrackDetail newTrack) {
        TrackDetail prevTrack = this.currentTrack;
        if (prevTrack != null && Objects.equals(newTrack.getId(), prevTrack.getId())) {
            System.out.println("Already playing that track");
            return null;
        }
        this.currentTrack = newTrack;
        if (this.isPlaying) {
            updateTrackTime();
        }

        long accumulatedTime = this.accumulatedTime;
        this.accumulatedTime = 0;
        return prevTrack != null ? StreamingHistory.of(prevTrack, accumulatedTime) : null;
    }

    public void validateDevice(String deviceId) {
        if (this.isPlaying && !this.deviceId.equals(deviceId)) {
            throw new AccessDeniedException("Another device is currently streaming.");
        }
    }

    public void resetHistoryIndex() {
        this.historyIndex = -1;
    }

    public void incrementHistoryIndex() {
        this.historyIndex++;
        if (this.historyIndex != 0) {
            this.historyIndex++;
        }
    }

    public void endSession() {

    }

    private void updateTrackTime() {
        long currentTime = currentTimeMilisSupplier().get();
        long timeElapsed = currentTime - this.lastRecordedTime;
        this.accumulatedTime += timeElapsed;
        this.lastRecordedTime = currentTime;
    }

    private Supplier<Long> currentTimeMilisSupplier() {
        return () -> Instant.now().toEpochMilli();
    }
}
