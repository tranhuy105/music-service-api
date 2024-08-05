package com.tranhuy105.musicserviceapi.repository.impl;

import com.tranhuy105.musicserviceapi.repository.api.UserInteractionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserInteractionDao implements UserInteractionRepository {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void likeTrack(long userId, long trackId) {
        String sql = "INSERT INTO likes (user_id, track_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, trackId);
    }

    @Override
    public void unlikeTrack(long userId, long trackId) {
        String sql = "DELETE FROM likes WHERE user_id = ? AND track_id = ?";
        jdbcTemplate.update(sql, userId, trackId);
    }

    @Override
    public void followArtist(long userId, long artistProfileId) {
        String sql = "INSERT INTO follows (user_id, artist_profile_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, artistProfileId);
    }

    @Override
    public void unfollowArtist(long userId, long artistProfileId) {
        String sql = "DELETE FROM follows WHERE user_id = ? AND artist_profile_id = ?";
        jdbcTemplate.update(sql, userId, artistProfileId);
    }
}
