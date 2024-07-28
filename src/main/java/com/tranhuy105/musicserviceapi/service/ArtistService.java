package com.tranhuy105.musicserviceapi.service;

import com.tranhuy105.musicserviceapi.exception.ObjectNotFoundException;
import com.tranhuy105.musicserviceapi.model.Artist;
import com.tranhuy105.musicserviceapi.model.ArtistProfile;
import com.tranhuy105.musicserviceapi.model.Page;
import com.tranhuy105.musicserviceapi.model.QueryOptions;
import com.tranhuy105.musicserviceapi.repository.api.ArtistRepository;
import com.tranhuy105.musicserviceapi.utils.CachePrefix;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArtistService {
    private final ArtistRepository artistRepository;
    private final CacheService cacheService;
    private static final int SEARCH_PAGE_SIZE = 20;

    public Page<Artist> searchArtist(Integer page, String searchQuery) {
        return artistRepository.findAllArtist(
                QueryOptions.of(page != null ? page : 1,SEARCH_PAGE_SIZE).search(searchQuery).build()
        );
    }

    public ArtistProfile findArtistProfileById(Long id) {
        String cacheKey = cacheService.getCacheKey(CachePrefix.ARTIST_PROFILE, id);
        return cacheService.cacheOrFetch(cacheKey, () ->
                artistRepository.findArtistProfileById(id).orElseThrow(
                        () -> new ObjectNotFoundException("artist", id.toString())
                )
        );
    }
}
