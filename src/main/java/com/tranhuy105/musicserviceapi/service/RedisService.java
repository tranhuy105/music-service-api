package com.tranhuy105.musicserviceapi.service;

import com.tranhuy105.musicserviceapi.dto.AlbumDto;
import com.tranhuy105.musicserviceapi.dto.PlaylistTrackDto;
import com.tranhuy105.musicserviceapi.model.ArtistProfile;
import com.tranhuy105.musicserviceapi.model.Page;
import com.tranhuy105.musicserviceapi.model.PlaylistTrack;
import com.tranhuy105.musicserviceapi.model.TrackDetail;
import com.tranhuy105.musicserviceapi.utils.CachePrefix;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class RedisService implements CacheService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final Duration cacheDuration = Duration.ofHours(6);

    @Override
    public <T> T cacheOrFetch(String cacheKey, Supplier<T> fallback) {
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        T cachedValue = (T) valueOperations.get(cacheKey);
        if (cachedValue == null) {
            cachedValue = fallback.get();
            if (cachedValue != null) {
                valueOperations.set(cacheKey, cachedValue, cacheDuration);
            }
        }
        return cachedValue;
    }

    @Override
    public String getCacheKey(CachePrefix prefix, Object... parts) {
        StringBuilder key = new StringBuilder(prefix.getPrefix());
        for (Object part : parts) {
            key.append(part.toString()).append(":");
        }
        key.setLength(key.length() - 1);
        return key.toString();
    }

    @Override
    public void evictCache(CachePrefix cachePrefix, Object... parts) {
        String cacheKeyPattern = getCacheKey(cachePrefix, parts) + "*";
        Set<String> keys = redisTemplate.keys(cacheKeyPattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }


    @Override
    public Page<PlaylistTrack> findPlaylistTracksCache(Long id, Integer page) {
        String cacheKey = getCacheKey(CachePrefix.PLAYLIST_TRACKS, id, page);
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        return (Page<PlaylistTrack>) valueOperations.get(cacheKey);
    }

    @Override
    public PlaylistTrackDto findPlaylistByIdCache(Long id, Integer page) {
        String cacheKey = getCacheKey(CachePrefix.PLAYLIST, id, page);
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        return (PlaylistTrackDto) valueOperations.get(cacheKey);
    }

    @Override
    public void cachePlaylistTracks(Long id, Integer page, Page<PlaylistTrack> tracks) {
        String cacheKey = getCacheKey(CachePrefix.PLAYLIST_TRACKS, id, page);
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(cacheKey, tracks, cacheDuration);
    }

    @Override
    public void cachePlaylist(Long id, Integer page, PlaylistTrackDto playlistTrackDto) {
        String cacheKey = getCacheKey(CachePrefix.PLAYLIST, id, page);
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(cacheKey, playlistTrackDto, cacheDuration);
    }

    @Override
    public UserDetails loadUserByUsernameCache(String username) {
        String cacheKey = getCacheKey(CachePrefix.USER, username);
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        return (UserDetails) valueOperations.get(cacheKey);
    }

    @Override
    public void cacheUserByUsername(String username, UserDetails userDetails) {
        String cacheKey = getCacheKey(CachePrefix.USER, username);
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(cacheKey, userDetails, cacheDuration);
    }

    @Override
    public ArtistProfile findArtistProfileByIdCache(Long id) {
        String cacheKey = getCacheKey(CachePrefix.ARTIST_PROFILE, id);
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        return (ArtistProfile) valueOperations.get(cacheKey);
    }

    @Override
    public void cacheArtistProfile(Long id, ArtistProfile artistProfile) {
        String cacheKey = getCacheKey(CachePrefix.ARTIST_PROFILE, id);
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(cacheKey, artistProfile, cacheDuration);
    }

    @Override
    public AlbumDto findAlbumByIdCache(Long albumId) {
        String cacheKey = getCacheKey(CachePrefix.ALBUM, albumId);
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        return (AlbumDto) valueOperations.get(cacheKey);
    }

    @Override
    public void cacheAlbum(Long albumId, AlbumDto albumDto) {
        String cacheKey = getCacheKey(CachePrefix.ALBUM, albumId);
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(cacheKey, albumDto, cacheDuration);
    }

    @Override
    public List<TrackDetail> findAlbumTracksCache(Long albumId) {
        String cacheKey = getCacheKey(CachePrefix.ALBUM_TRACKS, albumId);
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        return (List<TrackDetail>) valueOperations.get(cacheKey);
    }

    @Override
    public void cacheAlbumTracks(Long albumId, List<TrackDetail> albumTracks) {
        String cacheKey = getCacheKey(CachePrefix.ALBUM_TRACKS, albumId);
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(cacheKey, albumTracks, cacheDuration);
    }

    @Override
    public TrackDetail findTrackByIdCache(Long id) {
        String cacheKey = getCacheKey(CachePrefix.TRACK, id);
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        return (TrackDetail) valueOperations.get(cacheKey);
    }

    @Override
    public void cacheTrack(Long id, TrackDetail trackDetail) {
        String cacheKey = getCacheKey(CachePrefix.TRACK, id);
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(cacheKey, trackDetail, cacheDuration);
    }

    @Override
    public void evictAllCache() {
        for (CachePrefix prefix : CachePrefix.values()) {
            evictCache(prefix);
        }
    }
}
