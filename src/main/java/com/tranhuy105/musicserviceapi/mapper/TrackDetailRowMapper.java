package com.tranhuy105.musicserviceapi.mapper;

import com.tranhuy105.musicserviceapi.model.Track;
import com.tranhuy105.musicserviceapi.utils.CommonMapper;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TrackDetailRowMapper implements RowMapper<Track> {
    @Override
    public Track mapRow(ResultSet rs, int rowNum) throws SQLException {
        return CommonMapper.mapTrack(rs);
    }
}
