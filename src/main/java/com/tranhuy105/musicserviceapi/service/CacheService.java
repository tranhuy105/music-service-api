package com.tranhuy105.musicserviceapi.service;

import com.tranhuy105.musicserviceapi.dto.AlbumDto;
import com.tranhuy105.musicserviceapi.dto.PlaylistTrackDto;
import com.tranhuy105.musicserviceapi.model.ArtistProfile;
import com.tranhuy105.musicserviceapi.model.Page;
import com.tranhuy105.musicserviceapi.model.PlaylistTrack;
import com.tranhuy105.musicserviceapi.model.TrackDetail;
import com.tranhuy105.musicserviceapi.utils.CachePrefix;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.function.Supplier;

public interface CacheService {
    <T> T cacheOrFetch(String cacheKey, Supplier<T> fallback);
    String getCacheKey(CachePrefix prefix, Object... parts);
    void evictCache(CachePrefix cachePrefix, Object... parts);
    Page<PlaylistTrack> findPlaylistTracksCache(Long id, Integer page);

    PlaylistTrackDto findPlaylistByIdCache(Long id, Integer page);

    void cachePlaylistTracks(Long id, Integer page, Page<PlaylistTrack> tracks);

    void cachePlaylist(Long id, Integer page, PlaylistTrackDto playlistTrackDto);

    UserDetails loadUserByUsernameCache(String username);
    void cacheUserByUsername(String username, UserDetails userDetails);

    ArtistProfile findArtistProfileByIdCache(Long id);
    void cacheArtistProfile(Long id, ArtistProfile artistProfile);

    AlbumDto findAlbumByIdCache(Long albumId);
    void cacheAlbum(Long album, AlbumDto albumDto);

    List<TrackDetail> findAlbumTracksCache(Long albumId);
    void cacheAlbumTracks(Long albumId, List<TrackDetail> albumTracks);

    TrackDetail findTrackByIdCache(Long id);
    void cacheTrack(Long id, TrackDetail trackDetail);

    void evictAllCache();
}
