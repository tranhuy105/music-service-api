package com.tranhuy105.musicserviceapi.repository.api;

import com.tranhuy105.musicserviceapi.model.Artist;
import com.tranhuy105.musicserviceapi.model.Page;
import com.tranhuy105.musicserviceapi.model.PlaylistTrack;
import com.tranhuy105.musicserviceapi.model.QueryOptions;

import java.util.List;
import java.util.Map;

public interface UserInteractionRepository {
    Page<PlaylistTrack> findSavedTracksByUserId(Long userId, QueryOptions queryOptions);
    void addSavedTrack(long userId, long trackId);
    void removeSavedTrack(long userId, long trackId);
    boolean isTrackSaved(Long userId, Long trackId);
    Map<Long, Boolean> findSavedTrackIds(Long userId, List<Long> trackIds);

    Page<Artist> findFollowingArtistById(Long userId, QueryOptions queryOptions);
    void followArtist(long userId, long artistProfileId);
    void unfollowArtist(long userId, long artistProfileId);
    boolean isFollowingArtist(Long userId, Long artistId);

    Map<Long, Boolean> findFollowingArtistIds(Long userId, List<Long> artistIds);
}
