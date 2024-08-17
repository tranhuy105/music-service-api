package com.tranhuy105.musicserviceapi.repository.api;

import com.tranhuy105.musicserviceapi.dto.CreateTrackRequestDto;
import com.tranhuy105.musicserviceapi.dto.TrackQueueDto;
import com.tranhuy105.musicserviceapi.model.Page;
import com.tranhuy105.musicserviceapi.model.QueryOptions;
import com.tranhuy105.musicserviceapi.model.Track;
import com.tranhuy105.musicserviceapi.model.TrackDetail;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface TrackRepository {
    Optional<TrackDetail> findTrackById(Long trackId);

    List<TrackDetail> findTrackDetailByAlbumId(Long albumId);

    Page<TrackDetail> findAllTrack(QueryOptions queryOptions);

    List<TrackDetail> findTopTrackByArtistId(Long artistId, int limit);

    List<Track> findTrackRawByAlbumId(Long albumId);

    Long insert(CreateTrackRequestDto dto);

    Optional<Track> findRawTrackById(Long trackId);

    void updateTrack(Track track);

    void deleteTrack(Long trackId);

    List<TrackQueueDto> findTrackQueueFromPlaylist(@NonNull Long playlistId);
    List<TrackQueueDto> findTrackQueueFromAlbum(@NonNull Long albumId);
    List<TrackQueueDto> findTrackQueueFromLiked(@NonNull Long userId);
}
