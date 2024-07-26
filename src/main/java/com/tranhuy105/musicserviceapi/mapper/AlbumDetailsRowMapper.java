package com.tranhuy105.musicserviceapi.mapper;

import com.tranhuy105.musicserviceapi.model.Album;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AlbumDetailsRowMapper implements RowMapper<Album> {
    @Override
    public Album mapRow(ResultSet rs, int rowNum) throws SQLException {
        return null;
    }
}
