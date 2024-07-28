package com.tranhuy105.musicserviceapi.repository.api;

import com.tranhuy105.musicserviceapi.model.Artist;
import com.tranhuy105.musicserviceapi.model.ArtistProfile;
import com.tranhuy105.musicserviceapi.model.Page;
import com.tranhuy105.musicserviceapi.model.QueryOptions;

import java.util.List;
import java.util.Optional;

public interface ArtistRepository {
    List<Artist> findAllArtist();
    Page<Artist> findAllArtist(QueryOptions queryOptions);

    Optional<ArtistProfile> findArtistProfileById(Long id);

    Optional<ArtistProfile> findArtistProfileByUserId(Long userId);
}
