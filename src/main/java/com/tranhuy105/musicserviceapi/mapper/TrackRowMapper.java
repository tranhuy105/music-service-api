package com.tranhuy105.musicserviceapi.mapper;

import com.tranhuy105.musicserviceapi.model.Track;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TrackRowMapper implements RowMapper<Track> {
    @Override
    public Track mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Track(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getInt("duration")
        );
    }
}
