package com.tranhuy105.musicserviceapi.service;

import com.tranhuy105.musicserviceapi.dto.PlaylistTrackDto;
import com.tranhuy105.musicserviceapi.exception.ObjectNotFoundException;
import com.tranhuy105.musicserviceapi.model.Page;
import com.tranhuy105.musicserviceapi.model.Playlist;
import com.tranhuy105.musicserviceapi.model.PlaylistTrack;
import com.tranhuy105.musicserviceapi.model.QueryOptions;
import com.tranhuy105.musicserviceapi.repository.api.PlaylistRepository;
import com.tranhuy105.musicserviceapi.utils.CachePrefix;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaylistService {
    private final PlaylistRepository playlistRepository;
    private final CacheService cacheService;
    private static final int PLAYLIST_PAGE_SIZE = 200;
    private static final int SEARCH_PAGE_SIZE = 20;

    public PlaylistTrackDto findPlaylistById(Long id, Integer page) {
        final int pageSafe = page != null ? page : 1;
        String cacheKey = cacheService.getCacheKey(CachePrefix.PLAYLIST, id, pageSafe);
        return cacheService.cacheOrFetch(cacheKey, () -> {
            Playlist playlist = playlistRepository.findPlaylistById(id).orElseThrow(
                    () -> new ObjectNotFoundException("playlist", id.toString())
            );

            Page<PlaylistTrack> tracks = findPlaylistTracks(id, pageSafe);

            return new PlaylistTrackDto(playlist, tracks);
        });
    }

    public Page<PlaylistTrack> findPlaylistTracks(Long id, Integer page) {
        final int pageSafe = page != null ? page : 1;
        String cacheKey = cacheService.getCacheKey(CachePrefix.PLAYLIST_TRACKS, id, pageSafe);
        return cacheService.cacheOrFetch(cacheKey, () ->
                playlistRepository.findPlaylistTracksById(id,
                        QueryOptions
                                .of(pageSafe, PLAYLIST_PAGE_SIZE)
                                .sortBy("position")
                                .build()
                ));
    }

    public Page<Playlist> searchPlaylist(Integer page, String searchQuery) {
        return playlistRepository.findAllPlaylist(QueryOptions
                .of(page != null ? page : 1, SEARCH_PAGE_SIZE)
                .search(searchQuery)
                .build());
    }
}
