package com.tranhuy105.musicserviceapi.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StreamingHistory {
    private TrackDetail track;
    private long listeningTime;
    private LocalDateTime timestamp;

    public static StreamingHistory of(TrackDetail track, long accumulatedTime) {
        LocalDateTime startTime = LocalDateTime.now().minus(Duration.ofMillis(accumulatedTime));
        return new StreamingHistory(track, accumulatedTime, startTime);
    }
}
