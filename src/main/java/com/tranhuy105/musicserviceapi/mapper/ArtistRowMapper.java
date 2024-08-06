package com.tranhuy105.musicserviceapi.mapper;

import com.tranhuy105.musicserviceapi.model.Artist;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ArtistRowMapper implements RowMapper<Artist> {
    @Override
    public Artist mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Artist(
                rs.getLong("id"),
                rs.getString("stage_name"),
                rs.getString("bio"),
                rs.getString("profile_picture_url"),
                rs.getInt("follower_count")
        );
    }
}
