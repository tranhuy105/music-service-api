package com.tranhuy105.musicserviceapi.repository.impl;

import com.tranhuy105.musicserviceapi.dto.CreateTrackRequestDto;
import com.tranhuy105.musicserviceapi.dto.TrackQueueDto;
import com.tranhuy105.musicserviceapi.mapper.TrackDetailRowMapper;
import com.tranhuy105.musicserviceapi.mapper.TrackQueueDtoRowMapper;
import com.tranhuy105.musicserviceapi.mapper.TrackRowMapper;
import com.tranhuy105.musicserviceapi.model.*;
import com.tranhuy105.musicserviceapi.repository.api.TrackRepository;
import com.tranhuy105.musicserviceapi.utils.QueryUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
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
                       t.stream_count,
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

    @Override
    public Long insert(CreateTrackRequestDto dto) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("createTrack");

        Map<String, Object> out = jdbcCall.execute(
                Map.of("p_album_id", dto.getAlbumId(),
                        "p_title", dto.getTitle(),
                        "p_duration", dto.getDuration())
        );
        return (Long) out.get("p_track_id");
    }

    @Override
    public List<TrackQueueDto> findTrackQueueFromPlaylist(@NonNull Long playlistId) {
        String sql = "SELECT * FROM playlist_track WHERE playlist_id = ?";
        return jdbcTemplate.query(sql, new TrackQueueDtoRowMapper(SourceType.PLAYLIST), playlistId);
    }

    @Override
    public List<TrackQueueDto> findTrackQueueFromAlbum(@NonNull Long albumId) {
        String sql = "SELECT * FROM tracks WHERE album_id = ? ORDER BY id";
        return jdbcTemplate.query(sql, new TrackQueueDtoRowMapper(SourceType.ALBUM), albumId);
    }

    @Override
    public List<TrackQueueDto> findTrackQueueFromLiked(@NonNull Long userId) {
        String sql = "SELECT * FROM likes WHERE user_id = ? ORDER BY liked_at DESC";
        return jdbcTemplate.query(sql, new TrackQueueDtoRowMapper(SourceType.LIKED), userId);
    }
}
