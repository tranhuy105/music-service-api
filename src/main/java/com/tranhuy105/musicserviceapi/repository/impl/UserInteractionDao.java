package com.tranhuy105.musicserviceapi.repository.impl;

import com.tranhuy105.musicserviceapi.mapper.ArtistRowMapper;
import com.tranhuy105.musicserviceapi.mapper.PlaylistTrackRowMapper;
import com.tranhuy105.musicserviceapi.model.Artist;
import com.tranhuy105.musicserviceapi.model.Page;
import com.tranhuy105.musicserviceapi.model.PlaylistTrack;
import com.tranhuy105.musicserviceapi.model.QueryOptions;
import com.tranhuy105.musicserviceapi.repository.api.UserInteractionRepository;
import com.tranhuy105.musicserviceapi.utils.QueryUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class UserInteractionDao implements UserInteractionRepository {
    private final JdbcTemplate jdbcTemplate;
    private final QueryUtil queryUtil;

    @Override
    public Page<PlaylistTrack> findSavedTracksByUserId(Long userId, QueryOptions queryOptions) {
        String baseQuery = "SELECT * FROM user_saved_tracks WHERE user_id = :userId";
        Map<String, Object> params = Map.of("userId", userId);
        return queryUtil.executeQueryWithOptions(baseQuery, queryOptions, new PlaylistTrackRowMapper(true), params);
    }

    @Override
    public void addSavedTrack(long userId, long trackId) {
        String sql = "INSERT INTO likes (user_id, track_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, trackId);
    }

    @Override
    public void removeSavedTrack(long userId, long trackId) {
        String sql = "DELETE FROM likes WHERE user_id = ? AND track_id = ?";
        jdbcTemplate.update(sql, userId, trackId);
    }

    @Override
    public boolean isTrackSaved(Long userId, Long trackId) {
        String sql = "SELECT EXISTS(SELECT 1 FROM likes WHERE user_id = ? AND track_id = ?)";
        Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, userId, trackId);
        return exists != null && exists;
    }

    @Override
    public Page<Artist> findFollowingArtistById(Long userId, QueryOptions queryOptions) {
        String baseSql = "SELECT ap.* FROM follows f JOIN artist_profiles ap ON f.artist_profile_id = ap.id WHERE f.user_id = :userId";
        Map<String, Object> params = Map.of("userId", userId);
        return queryUtil.executeQueryWithOptions(baseSql, queryOptions, new ArtistRowMapper(), params);
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

    @Override
    public boolean isFollowingArtist(Long userId, Long artistId) {
        String sql = "SELECT EXISTS(SELECT 1 FROM follows WHERE user_id = ? AND artist_profile_id = ?)";
        Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, userId, artistId);
        return exists != null && exists;
    }
}
