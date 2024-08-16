package com.tranhuy105.musicserviceapi.service;

import com.tranhuy105.musicserviceapi.dto.CreateArtistProfileRequestDto;
import com.tranhuy105.musicserviceapi.dto.UpdateArtistProfileRequestDto;
import com.tranhuy105.musicserviceapi.exception.ObjectNotFoundException;
import com.tranhuy105.musicserviceapi.model.*;
import com.tranhuy105.musicserviceapi.repository.api.ArtistRepository;
import com.tranhuy105.musicserviceapi.repository.api.TrackRepository;
import com.tranhuy105.musicserviceapi.repository.api.UserRepository;
import com.tranhuy105.musicserviceapi.utils.CachePrefix;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ArtistService {
    private final ArtistRepository artistRepository;
    private final CacheService cacheService;
    private final UserRepository userRepository;
    private final TrackRepository trackRepository;

    private static final int SEARCH_PAGE_SIZE = 20;

    public void createArtistProfile(CreateArtistProfileRequestDto dto) {
        UserDetails userDetails = userRepository.findById(dto.getUserId()).orElseThrow(
                () -> new ObjectNotFoundException("user", dto.getUserId().toString())
        );
        artistRepository.insert(dto);
        cacheService.evictCache(CachePrefix.USER, userDetails.getUsername());
    }

    public Page<Artist> searchArtist(Integer page, String searchQuery) {
        return artistRepository.findAllArtist(
                QueryOptions.of(page != null ? page : 1,SEARCH_PAGE_SIZE).search(searchQuery).build()
        );
    }

    public Page<Artist> browseNewArtistByGenre(Long genreId, Integer page) {
        page = page == null ? 1 : page;
        return artistRepository.findArtistProfileByGenre(genreId,
                QueryOptions
                        .of(page, SEARCH_PAGE_SIZE)
                        .sortBy("ag.artist_id")
                        .desc()
                        .build()
        );
    }

    public Page<Artist> browseTopArtistByGenre(Long genreId, Integer page) {
        page = page == null ? 1 : page;
        return artistRepository.findArtistProfileByGenre(genreId,
                QueryOptions
                        .of(page, SEARCH_PAGE_SIZE)
                        .sortBy("ap.follower_count")
                        .desc()
                        .build()
        );
    }


    public List<Artist> findRelatedArtist(Long id, Integer limit) {
        int limitSafe = limit != null ? limit : 20;
        double threshHold = 0.2;
        return artistRepository.findRelatedArtist(id, limitSafe, threshHold);
    }

    public List<TrackDetail> findTopTrack(Long id) {
        String cacheKey = cacheService.getCacheKey(CachePrefix.ARTIST_TOP_TRACKS, id);
        return cacheService.cacheOrFetch(cacheKey, () -> trackRepository.findTopTrackByArtistId(id, 10));
    }

    public ArtistProfile findArtistProfileById(Long id) {
        String cacheKey = cacheService.getCacheKey(CachePrefix.ARTIST_PROFILE, id);
        return cacheService.cacheOrFetch(cacheKey, () ->
                artistRepository.findArtistProfileById(id).orElseThrow(
                        () -> new ObjectNotFoundException("artist", id.toString())
                )
        );
    }

    @Transactional
    public void updateArtistProfile(Long artistId, UpdateArtistProfileRequestDto dto) {
        findArtistProfileById(artistId);
        artistRepository.updateArtistProfile(artistId, dto);
        if (dto.getGenreIds() != null && !dto.getGenreIds().isEmpty()) {
            artistRepository.updateArtistGenres(artistId, dto.getGenreIds());
        }
        cacheService.evictCache(CachePrefix.ARTIST_PROFILE, artistId);
    }

    public void updateArtistGenre(Long artistId, List<Long> genreIds) {
        if (genreIds == null) {
            genreIds = new ArrayList<>();
        }
        artistRepository.updateArtistGenres(artistId, genreIds);
    }

    @Transactional
    public void deleteArtistProfile(Long artistId) {
        findArtistProfileById(artistId);
        artistRepository.deleteArtistProfile(artistId);
        cacheService.evictCache(CachePrefix.ARTIST_PROFILE, artistId);
    }

    public Artist findArtistByUserId(Long userId) {
        return artistRepository.findArtistByUserId(userId).orElse(null);
    }
}
