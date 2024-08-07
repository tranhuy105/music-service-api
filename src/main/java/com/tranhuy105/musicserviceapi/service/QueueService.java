package com.tranhuy105.musicserviceapi.service;

import com.tranhuy105.musicserviceapi.dto.TrackQueueDto;
import com.tranhuy105.musicserviceapi.model.PlaybackMode;
import com.tranhuy105.musicserviceapi.model.QueueItem;
import com.tranhuy105.musicserviceapi.model.StreamingSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QueueService {
    private final StreamingSourceService streamingSourceService;
    private static final int QUEUE_SIZE = 30;

    public LinkedList<QueueItem> generateTrackQueue(StreamingSession session) {
        LinkedList<QueueItem> trackQueue = new LinkedList<>();

        if (session.getPlaybackMode() == PlaybackMode.REPEAT) {
            if (session.getCurrentTrack() != null) {
                trackQueue.add(new QueueItem(session.getCurrentTrack().getId(), QueueItem.ItemType.TRACK));
            }
            return trackQueue;
        }

        List<TrackQueueDto> tracks = streamingSourceService.getTracks(session.getStreamingSource());

        if (tracks.isEmpty()) {
            if (session.getCurrentTrack() != null) {
                trackQueue.add(new QueueItem(session.getCurrentTrack().getId(), QueueItem.ItemType.TRACK));
            }
            return trackQueue;
        }

        PlaybackMode playbackMode = session.getPlaybackMode();
        Long currentTrackId = session.getCurrentTrack() != null ? session.getCurrentTrack().getId() : null;

        if (playbackMode == PlaybackMode.SHUFFLE) {
            Collections.shuffle(tracks);
            trackQueue.addAll(tracks.stream().map(track -> new QueueItem(track.getTrackId(), QueueItem.ItemType.TRACK)).limit(QUEUE_SIZE).toList());
        }

        if (playbackMode == PlaybackMode.SEQUENTIAL) {
            tracks.sort(Comparator.comparingInt(TrackQueueDto::getPosition));
            if (currentTrackId != null) {
                int currentIndex = tracks.stream()
                        .map(TrackQueueDto::getTrackId)
                        .toList()
                        .indexOf(currentTrackId);

                if (currentIndex != -1) {
                    for (int i = currentIndex + 1; i < tracks.size() && trackQueue.size() < QUEUE_SIZE; i++) {
                        trackQueue.add(new QueueItem(tracks.get(i).getTrackId(), QueueItem.ItemType.TRACK));
                    }
                    for (int i = 0; i < currentIndex && trackQueue.size() < QUEUE_SIZE; i++) {
                        trackQueue.add(new QueueItem(tracks.get(i).getTrackId(), QueueItem.ItemType.TRACK));
                    }
                }
            } else {
                trackQueue.addAll(tracks.stream().map(track -> new QueueItem(track.getTrackId(), QueueItem.ItemType.TRACK)).limit(QUEUE_SIZE).toList());
            }
        }

        return trackQueue;
    }


    public void ensureQueueIsNotEmpty(StreamingSession session) {
        if (session.getItemQueue().isEmpty()) {
            session.setItemQueue(generateTrackQueue(session));
        }
    }
}
