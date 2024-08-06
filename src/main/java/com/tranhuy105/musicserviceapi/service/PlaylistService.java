package com.tranhuy105.musicserviceapi.service;

import com.tranhuy105.musicserviceapi.dto.PlaylistDto;
import com.tranhuy105.musicserviceapi.dto.PlaylistTrackDto;
import com.tranhuy105.musicserviceapi.exception.ObjectNotFoundException;
import com.tranhuy105.musicserviceapi.model.Page;
import com.tranhuy105.musicserviceapi.model.Playlist;
import com.tranhuy105.musicserviceapi.model.PlaylistTrack;
import com.tranhuy105.musicserviceapi.model.QueryOptions;
import com.tranhuy105.musicserviceapi.repository.api.PlaylistRepository;
import com.tranhuy105.musicserviceapi.utils.CachePrefix;
import com.tranhuy105.musicserviceapi.utils.Util;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
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

    public void createPlaylist(PlaylistDto playlistDto, Authentication authentication) {
        Playlist playlist = new Playlist();
        playlist.setUserId(getUserId(authentication));
        playlist.setName(playlistDto.getName());
        playlist.setDescription(playlistDto.getDescription());
        playlist.setIsPublic(playlistDto.getIsPublic());
        playlist.setCoverUrl(playlistDto.getCoverUrl());
        playlistRepository.addPlaylist(playlist);
    }

    public void updatePlaylist(Long id, PlaylistDto updatePlaylistDto, Authentication authentication) {
        playlistValidate(id, authentication);
        Playlist playlist = new Playlist();
        playlist.setName(updatePlaylistDto.getName());
        playlist.setDescription(updatePlaylistDto.getDescription());
        playlist.setIsPublic(updatePlaylistDto.getIsPublic());
        playlist.setCoverUrl(updatePlaylistDto.getCoverUrl());
        playlistRepository.updatePlaylist(id, playlist);
        try {
            cacheService.evictCache(CachePrefix.PLAYLIST, id);
        } catch (Exception ex) {
            log.warn("Failed to revalidate cache.", ex.getCause());
        }
    }

    public void insertTrackToEnd(Long playlistId, Long trackId, Authentication authentication) {
        if (playlistRepository.trackExistsInPlaylist(playlistId, trackId)) {
            throw new DuplicateKeyException("Foreign Key Constraint Failed For Playlist Track Relationship");
        }
        playlistValidate(playlistId, authentication);

        playlistRepository.insertTrackToEnd(playlistId, trackId, getUserId(authentication));
        try {
            cacheService.evictCache(CachePrefix.PLAYLIST, playlistId);
            cacheService.evictCache(CachePrefix.PLAYLIST_TRACKS, playlistId);
        } catch (Exception ex) {
            log.warn("Failed to revalidate cache.", ex.getCause());
        }
    }

    public void deleteTrack(Long playlistId, Long trackId, Authentication authentication) {
        if (!playlistRepository.trackExistsInPlaylist(playlistId, trackId)) {
            throw new DataIntegrityViolationException("Foreign Key Constraint Failed For Playlist Track Relationship");
        }
        playlistValidate(playlistId, authentication);

        playlistRepository.deleteTrack(playlistId, trackId);
        try {
            cacheService.evictCache(CachePrefix.PLAYLIST, playlistId);
            cacheService.evictCache(CachePrefix.PLAYLIST_TRACKS, playlistId);
        } catch (Exception ex) {
            log.warn("Failed to revalidate cache.", ex.getCause());
        }
    }

    public void moveTrack(@NonNull Long playlistId, @NonNull Long trackId, @NonNull Long newPosition, Authentication authentication) {
        if (!playlistRepository.trackExistsInPlaylist(playlistId, trackId)) {
            throw new DataIntegrityViolationException("Foreign Key Constraint Failed For Playlist Track Relationship");
        }
        playlistValidate(playlistId, authentication);

        playlistRepository.moveTrack(playlistId, trackId, newPosition);
        try {
            cacheService.evictCache(CachePrefix.PLAYLIST, playlistId);
            cacheService.evictCache(CachePrefix.PLAYLIST_TRACKS, playlistId);
        } catch (Exception ex) {
            log.warn("Failed to revalidate cache.", ex.getCause());
        }
    }

    private void playlistValidate(Long playlistId, Authentication authentication) {
        Long userId = getUserId(authentication);
        Playlist playlist = playlistRepository.findPlaylistById(playlistId)
                .orElseThrow( () -> new ObjectNotFoundException("playlist", playlistId.toString()));

        if (!playlist.getIsPublic() && !userId.equals(playlist.getUserId())) {
            throw new AccessDeniedException("Private playlist!");
        }
    }

    private Long getUserId(Authentication authentication) {
        return Util.extractUserIdFromAuthentication(authentication);
    }
}
