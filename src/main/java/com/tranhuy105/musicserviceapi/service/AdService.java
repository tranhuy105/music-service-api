package com.tranhuy105.musicserviceapi.service;

import com.tranhuy105.musicserviceapi.model.*;
import com.tranhuy105.musicserviceapi.repository.api.AdvertisementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdService {
    private final AdvertisementRepository repository;
    private static final long AD_COOLDOWN_THRESHOLD = 10000;

    public Advertisement getAdById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public Advertisement getAdByRegion(StreamingSession session) {
        return repository.findRandomAdByRegion("VN").orElse(null);
    }

    public void processAdService(User user, StreamingSession session) {
        if (shouldForceAd(user, session)) {
            insertAdToQueue(session);
        } else {
            handleAdInsertionIfNeeded(user, session);
        }
    }

    public void handleAdInsertionIfNeeded(User user, StreamingSession session) {
        if (user.getIsPremium()) {
            session.incrementAdCounter();
            if (session.isAdIntervalReached()) {
                insertAdToQueue(session);
            }
        }
    }

    private boolean shouldForceAd(User user, StreamingSession session) {
        return !session.isAdCooldownPeriodPassed(AD_COOLDOWN_THRESHOLD) && user.getIsPremium();
    }

    private void insertAdToQueue(StreamingSession session) {
        Advertisement adTrack = getAdByRegion(session);
        session.getItemQueue().addFirst(new QueueItem(adTrack.getId(), QueueItem.ItemType.AD));
        session.resetAdCounter();
        session.updateLastPlayedAdTime();
    }


    public void handleAdPlayback(Advertisement advertisement, StreamingSession session) {
        session.setCurrentMedia(advertisement);
    }
}
