package com.tranhuy105.musicserviceapi.repository.api;

import com.tranhuy105.musicserviceapi.model.*;

import java.util.List;
import java.util.Optional;

public interface MetadataRepository {
    Optional<Track> findTrackById(Long trackId);

    List<Track> findAllTrack();

    Optional<AlbumDetail> findAlbumById(Long albumId);
    List<Album> findAllAlbum();
    Page<Album> findAllAlbum(QueryOptions queryOptions);
    List<Artist> findAllArtist();
    Page<Artist> findAllArtist(QueryOptions queryOptions);

    Optional<ArtistProfile> findArtistProfileById(Long id);

    Optional<ArtistProfile> findArtistProfileByUserId(Long userId);
}
