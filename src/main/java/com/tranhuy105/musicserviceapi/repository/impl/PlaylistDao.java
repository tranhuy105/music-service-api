package com.tranhuy105.musicserviceapi.repository.impl;

import com.tranhuy105.musicserviceapi.dto.PlaylistTrackDto;
import com.tranhuy105.musicserviceapi.mapper.PlaylistSummaryRowMapper;
import com.tranhuy105.musicserviceapi.mapper.PlaylistTrackRowMapper;
import com.tranhuy105.musicserviceapi.model.*;
import com.tranhuy105.musicserviceapi.model.ref.AlbumArtist;
import com.tranhuy105.musicserviceapi.model.ref.TrackAlbum;
import com.tranhuy105.musicserviceapi.repository.api.PlaylistRepository;
import com.tranhuy105.musicserviceapi.utils.QueryUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.*;

@RequiredArgsConstructor
@Repository
public class PlaylistDao implements PlaylistRepository {
    private final JdbcTemplate jdbcTemplate;
    private final QueryUtil queryUtil;

    @Override
    public Page<PlaylistTrack> findPlaylistTracksById(@NonNull Long id, @NonNull QueryOptions queryOptions) {
        String baseQuery = "SELECT * FROM playlist_track_details WHERE playlist_id = :id";
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        return queryUtil.executeQueryWithOptions(baseQuery, queryOptions, new PlaylistTrackRowMapper(), params);
    }

    @Override
    public Optional<Playlist> findPlaylistById(@NonNull Long id) {
        String sql = "SELECT * FROM playlist_summary WHERE playlist_id = ?";
        return jdbcTemplate.query(sql, new PlaylistSummaryRowMapper(), id).stream().findFirst();
    }
}
