package com.tranhuy105.musicserviceapi.repository.api;

import com.tranhuy105.musicserviceapi.model.*;

import java.util.List;
import java.util.Optional;

public interface MetadataRepository {
    Optional<TrackDetail> findTrackById(Long trackId);

    Page<TrackDetail> findAllTrack(QueryOptions queryOptions);

    Optional<AlbumDetail> findAlbumById(Long albumId);
    List<Album> findAllAlbum();
    Page<Album> findAllAlbum(QueryOptions queryOptions);
    List<Track> findAllTrackByAlbumId(Long albumId);
    List<Artist> findAllArtist();
    Page<Artist> findAllArtist(QueryOptions queryOptions);

    Optional<ArtistProfile> findArtistProfileById(Long id);

    Optional<ArtistProfile> findArtistProfileByUserId(Long userId);
}
