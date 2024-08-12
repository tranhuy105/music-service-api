package com.tranhuy105.musicserviceapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.LinkedList;
import java.util.Objects;
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
    private MediaItem currentMedia; // Current track being played

    private StreamingSource streamingSource;
    private PlaybackMode playbackMode = PlaybackMode.SHUFFLE;

    private long lastRecordedTime; // Timestamp of the last recorded activity
    private long accumulatedTime; // Total accumulated time for the current track (in milliseconds)
    private String deviceId; // Identifier for the device playing the track

    // a pointer to track where is the previous track really is to avoid a loop between current and previous track
    private int historyIndex = -1;
    private long lastPlayedAdTime;
    private int adInterval; // Interval to play an ad, in track counts
    private int adCounter;  // Counter to track how many tracks have been played since the last ad

    private LinkedList<QueueItem> itemQueue = new LinkedList<>();

    public StreamingSession(User user, String deviceId, StreamingSource streamingSource, PlaybackMode playbackMode) {
        this.userId = user.getId();
        this.isPlaying = false;
        this.currentMedia = null;
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
        if (this.currentMedia instanceof Advertisement) {
            this.currentMedia = newTrack;
            this.accumulatedTime = 0;
            this.lastRecordedTime = currentTimeMilisSupplier().get();
            return null;
        }

        TrackDetail prevItem = (TrackDetail) this.currentMedia;
        if (prevItem == null) {
            this.currentMedia = newTrack;
            return null;
        }

        if (Objects.equals(newTrack.getId(), prevItem.getItemId())) {
            return null;
        }

        this.currentMedia = newTrack;
        if (this.isPlaying) {
            updateTrackTime();
        }

        long accumulatedTime = this.accumulatedTime;
        this.accumulatedTime = 0;
        return StreamingHistory.of(prevItem, accumulatedTime);
    }

    public void validateDevice(String deviceId) {
        if (this.isPlaying && !this.deviceId.equals(deviceId)) {
            throw new AccessDeniedException("Another device is currently streaming.");
        } else {
            this.deviceId = deviceId;
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

    public void initializeAdSettings(int adInterval) {
        this.lastPlayedAdTime = 0;
        this.adInterval = adInterval;
        this.adCounter = 0;
    }

    public void incrementAdCounter() {
        this.adCounter++;
    }

    public void updateLastPlayedAdTime() {
        this.lastPlayedAdTime = currentTimeMilisSupplier().get();
    }

    @JsonIgnore
    public boolean isAdCooldownPeriodPassed(long threshold) {
        long currentTime = currentTimeMilisSupplier().get();
        return (currentTime - lastPlayedAdTime) >= threshold;
    }


    @JsonIgnore
    public boolean isAdIntervalReached() {
        return this.adCounter >= this.adInterval;
    }

    public void resetAdCounter() {
        this.adCounter = 0;
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
