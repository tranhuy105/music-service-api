package com.tranhuy105.musicserviceapi.model;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class SuccessfulStreamEvent extends ApplicationEvent {
    private final Long userId;
    private final StreamingHistory streamingHistory;

    public SuccessfulStreamEvent(Object source, Long userId, StreamingHistory streamingHistory) {
        super(source);
        this.userId = userId;
        this.streamingHistory = streamingHistory;
    }
}
