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
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public Map<Long, Boolean> findSavedTrackIds(Long userId, List<Long> trackIds) {
        String sql = "SELECT track_id FROM likes WHERE user_id = ? AND track_id IN (" +
                trackIds.stream().map(id -> "?").collect(Collectors.joining(",")) +
                ")";
        Object[] params = new Object[trackIds.size() + 1];
        params[0] = userId;
        for (int i = 0; i < trackIds.size(); i++) {
            params[i + 1] = trackIds.get(i);
        }

        RowMapper<Long> rowMapper = (rs, rowNum) -> rs.getLong("track_id");
        List<Long> savedTrackIds = jdbcTemplate.query(sql, params, rowMapper);

        return trackIds.stream()
                .collect(Collectors.toMap(
                        trackId -> trackId,
                        savedTrackIds::contains
                ));
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

    @Override
    public Map<Long, Boolean> findFollowingArtistIds(Long userId, List<Long> artistIds) {
        String sql = "SELECT artist_profile_id FROM follows WHERE user_id = ? AND artist_profile_id IN (" +
                artistIds.stream().map(id -> "?").collect(Collectors.joining(",")) +
                ")";
        Object[] params = new Object[artistIds.size() + 1];
        params[0] = userId;
        for (int i = 0; i < artistIds.size(); i++) {
            params[i + 1] = artistIds.get(i);
        }

        RowMapper<Long> rowMapper = (rs, rowNum) -> rs.getLong("artist_profile_id");
        List<Long> followArtistId = jdbcTemplate.query(sql, params, rowMapper);

        return artistIds.stream()
                .collect(Collectors.toMap(
                        artistId -> artistId,
                        followArtistId::contains
                ));
    }
}
