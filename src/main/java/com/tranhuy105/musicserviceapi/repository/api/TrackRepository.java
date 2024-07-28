package com.tranhuy105.musicserviceapi.repository.api;

import com.tranhuy105.musicserviceapi.model.Page;
import com.tranhuy105.musicserviceapi.model.QueryOptions;
import com.tranhuy105.musicserviceapi.model.Track;
import com.tranhuy105.musicserviceapi.model.TrackDetail;

import java.util.List;
import java.util.Optional;

public interface TrackRepository {
    Optional<TrackDetail> findTrackById(Long trackId);

    List<TrackDetail> findTrackDetailByAlbumId(Long albumId);

    Page<TrackDetail> findAllTrack(QueryOptions queryOptions);

    List<Track> findTrackRawByAlbumId(Long albumId);
}
