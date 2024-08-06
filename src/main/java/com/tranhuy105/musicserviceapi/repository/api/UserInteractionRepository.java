package com.tranhuy105.musicserviceapi.repository.api;

public interface UserInteractionRepository {
    void likeTrack(long userId, long trackId);
    void unlikeTrack(long userId, long trackId);
    void followArtist(long userId, long artistProfileId);
    void unfollowArtist(long userId, long artistProfileId);
}
