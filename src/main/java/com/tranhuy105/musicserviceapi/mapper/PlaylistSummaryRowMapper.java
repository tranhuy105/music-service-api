package com.tranhuy105.musicserviceapi.mapper;

import com.tranhuy105.musicserviceapi.model.Playlist;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PlaylistSummaryRowMapper implements RowMapper<Playlist> {
    @Override
    public Playlist mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Playlist(
                rs.getLong("playlist_id"),
                rs.getLong("user_id"),
                rs.getString("playlist_name"),
                rs.getString("playlist_description"),
                rs.getBoolean("is_public"),
                rs.getString("playlist_cover_url"),
                rs.getInt("total_tracks")
        );
    }
}
