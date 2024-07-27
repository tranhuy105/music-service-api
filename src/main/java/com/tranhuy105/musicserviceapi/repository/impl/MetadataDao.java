package com.tranhuy105.musicserviceapi.repository.impl;

import com.tranhuy105.musicserviceapi.mapper.*;
import com.tranhuy105.musicserviceapi.model.*;
import com.tranhuy105.musicserviceapi.repository.api.MetadataRepository;
import com.tranhuy105.musicserviceapi.utils.QueryUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class MetadataDao implements MetadataRepository {
    private final JdbcTemplate jdbcTemplate;
    private final QueryUtil queryUtil;

    @Override
    public Optional<TrackDetail> findTrackById(Long trackId) {
        String sql = "SELECT * FROM track_details WHERE track_id = ?";
        List<TrackDetail> res = jdbcTemplate.query(sql, new TrackDetailRowMapper(), trackId);
        return res.stream().findFirst();
    }

    @Override
    public Page<TrackDetail> findAllTrack(QueryOptions queryOptions) {
        // mysql views can't use underlying table fulltext search index 🥲
        String baseQuery = """
                SELECT t.id AS track_id,
                       t.title AS track_title,
                       t.duration AS track_duration,
                       t.album_id,
                       a.title AS album_title,
                       a.cover_url AS album_cover_url,
                       GROUP_CONCAT(aa.artist_id) AS artist_ids,
                       GROUP_CONCAT(ap.stage_name) AS artist_stage_names,
                       GROUP_CONCAT(ap.profile_picture_url) AS artist_profile_pictures,
                       GROUP_CONCAT(aa.role) AS roles
                FROM tracks t
                JOIN albums a ON t.album_id = a.id
                JOIN album_artists aa ON a.id = aa.album_id
                JOIN artist_profiles ap ON aa.artist_id = ap.id
                GROUP BY t.id""";
        return queryUtil.executeQueryWithOptions(
                baseQuery,
                queryOptions,
                new TrackDetailRowMapper());
    }

    @Override
    public Optional<AlbumDetail> findAlbumById(Long albumId) {
        String sql = "SELECT * FROM album_details WHERE album_id = ?";
        return jdbcTemplate.query(sql, new AlbumDetailsRowMapper(), albumId).stream().findFirst();
    }

    @Override
    public List<Album> findAllAlbum() {
        String sql = "SELECT * FROM albums";
        return jdbcTemplate.query(sql, new AlbumRowMapper());
    }

    @Override
    public Page<Album> findAllAlbum(QueryOptions queryOptions) {
        String baseQuery = "SELECT * FROM albums";
        return queryUtil.executeQueryWithOptions(baseQuery, queryOptions, new AlbumRowMapper());
    }

    @Override
    public List<Track> findAllTrackByAlbumId(Long albumId) {
        String sql = "SELECT * FROM tracks WHERE album_id = ?";
        return jdbcTemplate.query(sql, new TrackRowMapper(), albumId);
    }

    @Override
    public List<Artist> findAllArtist() {
        String sql = "SELECT * FROM artist_profiles";
        return jdbcTemplate.query(sql, new ArtistRowMapper());
    }

    @Override
    public Page<Artist> findAllArtist(QueryOptions queryOptions) {
        String baseQuery = "SELECT * FROM artist_profiles";
        return queryUtil.executeQueryWithOptions(baseQuery, queryOptions, new ArtistRowMapper());
    }

    @Override
    public Optional<ArtistProfile> findArtistProfileById(Long id) {
        String sql = "SELECT * FROM artist_details WHERE artist_id = ?";
        return jdbcTemplate.query(sql, new ArtistProfileRowMapper(), id).stream().findFirst();
    }

    @Override
    public Optional<ArtistProfile> findArtistProfileByUserId(Long userId) {
        String sql = "SELECT * FROM artist_profiles WHERE user_id = ?";
        return jdbcTemplate.query(sql, new ArtistProfileRowMapper(), userId).stream().findFirst();
    }
}
