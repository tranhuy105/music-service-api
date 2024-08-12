package com.tranhuy105.musicserviceapi.service;

import com.tranhuy105.musicserviceapi.model.*;
import com.tranhuy105.musicserviceapi.repository.api.UserInteractionRepository;
import com.tranhuy105.musicserviceapi.utils.CachePrefix;
import com.tranhuy105.musicserviceapi.utils.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LibraryService {
    private final UserInteractionRepository userInteractionRepository;
    private final CacheService cacheService;
    private static final int PLAYLIST_PAGE_SIZE = 200;
    private static final int FOLLOWED_ARTIST_PAGE_SIZE = 50;

    public Page<PlaylistTrack> findMySavedTrack(Authentication authentication, Integer page) {
        final int pageSafe = page != null ? page : 1;
        Long userId = getUser(authentication).getId();
        String cacheKey = cacheService.getCacheKey(CachePrefix.SAVED_TRACK, userId, pageSafe);
        return cacheService.cacheOrFetch(cacheKey, () ->
                userInteractionRepository.findSavedTracksByUserId(
                        userId,
                        QueryOptions
                                .of(pageSafe, PLAYLIST_PAGE_SIZE)
                                .sortBy("added_at")
                                .desc()
                                .build()
        ));
    }

    public Page<Artist> findMyFollowedArtist(Authentication authentication, Integer page) {
        final int pageSafe = page != null ? page : 1;
        Long userId = getUser(authentication).getId();
        String cacheKey = cacheService.getCacheKey(CachePrefix.FOLLOWED_ARTIST, userId, pageSafe);
        return cacheService.cacheOrFetch(cacheKey, () ->
            userInteractionRepository.findFollowingArtistById(userId, QueryOptions
                    .of(pageSafe, FOLLOWED_ARTIST_PAGE_SIZE)
                    .build())
        );
    }

    public void addSavedTrack(Authentication authentication, Long trackId) {
        Long userId = getUser(authentication).getId();
        userInteractionRepository.addSavedTrack(userId, trackId);
        evictLibraryCache(CachePrefix.SAVED_TRACK, userId);
    }

    public void removeSavedTrack(Authentication authentication, Long trackId) {
        Long userId = getUser(authentication).getId();
        userInteractionRepository.removeSavedTrack(userId, trackId);
        evictLibraryCache(CachePrefix.SAVED_TRACK, userId);
    }

    public List<Boolean> areTracksSaved(Authentication authentication, List<Long> trackIds) {
        Long userId = getUser(authentication).getId();
        Map<Long, Boolean> savedTrackMap = userInteractionRepository.findSavedTrackIds(userId, trackIds);

        return trackIds.stream()
                .map(trackId -> savedTrackMap.getOrDefault(trackId, false))
                .toList();
    }

    public void followArtist(Authentication authentication, Long artistProfileId) {
        Long userId = getUser(authentication).getId();
        userInteractionRepository.followArtist(userId, artistProfileId);
        evictLibraryCache(CachePrefix.FOLLOWED_ARTIST, userId);
    }

    public void unfollowArtist(Authentication authentication, Long artistProfileId) {
        Long userId = getUser(authentication).getId();
        userInteractionRepository.unfollowArtist(userId, artistProfileId);
        evictLibraryCache(CachePrefix.FOLLOWED_ARTIST, userId);
    }

    public List<Boolean> areFollowingArtist(Authentication authentication, List<Long> artistIds) {
        Long userId = getUser(authentication).getId();
        Map<Long, Boolean> followingArtistsIds = userInteractionRepository.findFollowingArtistIds(userId, artistIds);
        return artistIds.stream()
                .map(artistId -> followingArtistsIds.getOrDefault(artistId,false))
                .toList();
    }

    private void evictLibraryCache(CachePrefix cachePrefix, Object... part) {
        try {
            cacheService.evictCache(cachePrefix, part);
        } catch (Exception ex) {
            log.warn("Fail to revalidate cache", ex.getCause());
        }
    }

    private User getUser(Authentication authentication) {
        return Util.extractUserFromAuthentication(authentication);
    }
}
