package com.tranhuy105.musicserviceapi.repository.api;

import com.tranhuy105.musicserviceapi.dto.CreateArtistProfileRequestDto;
import com.tranhuy105.musicserviceapi.dto.UpdateArtistProfileRequestDto;
import com.tranhuy105.musicserviceapi.model.Artist;
import com.tranhuy105.musicserviceapi.model.ArtistProfile;
import com.tranhuy105.musicserviceapi.model.Page;
import com.tranhuy105.musicserviceapi.model.QueryOptions;

import java.util.List;
import java.util.Optional;

public interface ArtistRepository {
    List<Artist> findAllArtist();
    Page<Artist> findAllArtist(QueryOptions queryOptions);
    List<Artist> findRelatedArtist(Long id, int limit, double threshHold);
    Page<Artist> findArtistProfileByGenre(Long genreId, QueryOptions queryOptions);

    Optional<ArtistProfile> findArtistProfileById(Long id);

    Optional<Artist> findArtistByUserId(Long userId);

    void insert(CreateArtistProfileRequestDto dto);
    void updateArtistProfile(Artist artist);
    void updateArtistGenres(Long artistId, List<Long> genreIds);
    void deleteArtistProfile(Long artistId);
}
