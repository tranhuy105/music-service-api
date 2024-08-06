package com.tranhuy105.musicserviceapi.service;

import com.tranhuy105.musicserviceapi.dto.TrackQueueDto;
import com.tranhuy105.musicserviceapi.model.StreamingSource;
import com.tranhuy105.musicserviceapi.repository.api.AlbumRepository;
import com.tranhuy105.musicserviceapi.repository.api.PlaylistRepository;
import com.tranhuy105.musicserviceapi.repository.api.TrackRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StreamingSourceService {
    private  final TrackRepository trackRepository;;


    public List<TrackQueueDto> getTracks(@NonNull StreamingSource source) {
        return switch (source.getSourceType()) {
            case PLAYLIST -> trackRepository.findTrackQueueFromPlaylist(source.getSourceId());
            case ALBUM -> trackRepository.findTrackQueueFromAlbum(source.getSourceId());
            case LIKED -> trackRepository.findTrackQueueFromLiked(source.getSourceId());
        };
    }
}
