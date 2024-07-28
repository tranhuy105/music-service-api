package com.tranhuy105.musicserviceapi.service;

import com.tranhuy105.musicserviceapi.exception.ObjectNotFoundException;
import com.tranhuy105.musicserviceapi.model.Page;
import com.tranhuy105.musicserviceapi.model.QueryOptions;
import com.tranhuy105.musicserviceapi.model.TrackDetail;
import com.tranhuy105.musicserviceapi.repository.api.TrackRepository;
import com.tranhuy105.musicserviceapi.utils.CachePrefix;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrackService {
    private final TrackRepository trackRepository;
    private final CacheService cacheService;
    private static final int SEARCH_PAGE_SIZE = 20;

    public Page<TrackDetail> searchTrack(Integer page, String searchQuery) {
        return trackRepository.findAllTrack(
                QueryOptions.of(page != null ? page : 1,SEARCH_PAGE_SIZE).search(searchQuery).build()
        );
    }

    public TrackDetail findTrackById(Long id) {
        String cacheKey = cacheService.getCacheKey(CachePrefix.TRACK, id);
        return cacheService.cacheOrFetch(cacheKey, () ->
                trackRepository.findTrackById(id).orElseThrow(
                        () -> new ObjectNotFoundException("track", id.toString())
                ));
    }
}