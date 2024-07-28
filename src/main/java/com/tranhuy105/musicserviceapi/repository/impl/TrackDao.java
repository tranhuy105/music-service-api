package com.tranhuy105.musicserviceapi.repository.impl;

import com.tranhuy105.musicserviceapi.mapper.TrackDetailRowMapper;
import com.tranhuy105.musicserviceapi.mapper.TrackRowMapper;
import com.tranhuy105.musicserviceapi.model.Page;
import com.tranhuy105.musicserviceapi.model.QueryOptions;
import com.tranhuy105.musicserviceapi.model.Track;
import com.tranhuy105.musicserviceapi.model.TrackDetail;
import com.tranhuy105.musicserviceapi.repository.api.TrackRepository;
import com.tranhuy105.musicserviceapi.utils.QueryUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TrackDao implements TrackRepository {
    private final JdbcTemplate jdbcTemplate;
    private final QueryUtil queryUtil;
    @Override
    public Optional<TrackDetail> findTrackById(Long trackId) {
        String sql = "SELECT * FROM track_details WHERE track_id = ?";
        List<TrackDetail> res = jdbcTemplate.query(sql, new TrackDetailRowMapper(), trackId);
        return res.stream().findFirst();
    }

    @Override
    public List<TrackDetail> findTrackDetailByAlbumId(Long albumId) {
        String sql = "SELECT * FROM track_details WHERE album_id = ?";
        return jdbcTemplate.query(sql, new TrackDetailRowMapper(), albumId);
    }

    @Override
    public Page<TrackDetail> findAllTrack(QueryOptions queryOptions) {
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
    public List<Track> findTrackRawByAlbumId(Long albumId) {
        String sql = "SELECT * FROM tracks WHERE album_id = ?";
        return jdbcTemplate.query(sql, new TrackRowMapper(), albumId);
    }
}
