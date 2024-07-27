package com.tranhuy105.musicserviceapi.mapper;

import com.tranhuy105.musicserviceapi.model.AlbumDetail;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AlbumDetailsRowMapper implements RowMapper<AlbumDetail> {
    @Override
    public AlbumDetail mapRow(ResultSet rs, int rowNum) throws SQLException {
        return null;
    }
}
