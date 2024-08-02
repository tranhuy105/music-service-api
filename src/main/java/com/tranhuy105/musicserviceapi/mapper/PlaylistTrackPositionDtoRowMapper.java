package com.tranhuy105.musicserviceapi.mapper;

import com.tranhuy105.musicserviceapi.dto.PlaylistTrackPositionDto;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PlaylistTrackPositionDtoRowMapper implements RowMapper<PlaylistTrackPositionDto> {
    @Override
    public PlaylistTrackPositionDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new PlaylistTrackPositionDto(
                rs.getLong("playlist_id"),
                rs.getInt("position"),
                rs.getLong("track_id"),
                rs.getLong("added_by"),
                rs.getTimestamp("added_at").toLocalDateTime()
        );
    }
}
