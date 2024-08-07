package com.tranhuy105.musicserviceapi.service;

import com.tranhuy105.musicserviceapi.model.SuccessfulStreamEvent;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApplicationEventListener {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationEventListener.class);
    private final ScheduleService scheduleService;

    @Async
    @EventListener
    public void handleStreamingHistoryEvent(SuccessfulStreamEvent event) {
        // TODO: generate recommendation, analyze trending
        logger.info("Processing streaming history event: " + event.getStreamingHistory());
        scheduleService.incrementStreamCount(event.getStreamingHistory().getTrack().getId());
    }

    @PreDestroy
    public void onShutdown() {
        logger.info("Application is shutting down. Performing final updates.");
        scheduleService.updateStreamCounts();
        scheduleService.updateFollowerCounts();
    }
}
