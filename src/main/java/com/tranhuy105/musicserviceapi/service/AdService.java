package com.tranhuy105.musicserviceapi.service;

import com.tranhuy105.musicserviceapi.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdService {
    private static final long AD_COOLDOWN_THRESHOLD = 10000;

    public Advertisement getAd(String uri) {
        return new Advertisement( 3L, "Want to break from the ad?", "https://something.com");
    }


    public void processAdService(User user, StreamingSession session) {
        // forcing to watch another ad if user attempt to skip the ad too quickly
        if (!session.isAdCooldownPeriodPassed(AD_COOLDOWN_THRESHOLD) && !user.getIsPremium()) {
            Advertisement adTrack = getAd("1");
            session.getItemQueue().addFirst(new QueueItem(adTrack.getId(), QueueItem.ItemType.AD));
            session.resetAdCounter();
            session.updateLastPlayedAdTime(); // Update ad time after forcing another ad
        } else {
            handleAdInsertionIfNeeded(user, session);
        }
    }

    public void handleAdInsertionIfNeeded(User user, StreamingSession session) {
        if (!user.getIsPremium()) {
            session.incrementAdCounter();
            if (session.isAdIntervalReached()) {
                Advertisement adTrack = getAd("1");
                session.getItemQueue().addFirst(new QueueItem(adTrack.getId(), QueueItem.ItemType.AD));
                session.resetAdCounter();
                session.updateLastPlayedAdTime();
            }
        }
    }

    public void handleAdPlayback(Advertisement advertisement, StreamingSession session) {
        System.out.println("playing ad: "+advertisement.getTitle()+", user: "+session.getUserId());
    }
}
