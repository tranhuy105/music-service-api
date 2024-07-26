package com.tranhuy105.musicserviceapi.repository.api;

import com.tranhuy105.musicserviceapi.model.Album;
import com.tranhuy105.musicserviceapi.model.ArtistProfile;
import com.tranhuy105.musicserviceapi.model.Track;

import java.util.List;
import java.util.Optional;

public interface MetadataRepository {
    Optional<Track> findTrackById(Long trackId);

    List<Track> findAllTrack();

    Optional<Album> findAlbumById(Long albumId);

    Optional<ArtistProfile> findArtistProfileById(Long id);

    Optional<ArtistProfile> findArtistProfileByUserId(Long userId);
}
