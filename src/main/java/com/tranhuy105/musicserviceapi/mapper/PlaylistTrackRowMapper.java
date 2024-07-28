package com.tranhuy105.musicserviceapi.mapper;

import com.tranhuy105.musicserviceapi.model.PlaylistTrack;
import com.tranhuy105.musicserviceapi.model.TrackDetail;
import com.tranhuy105.musicserviceapi.utils.CommonMapper;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlaylistTrackRowMapper implements RowMapper<PlaylistTrack> {

    @Override
    public PlaylistTrack mapRow(ResultSet rs, int rowNum) throws SQLException {
        TrackDetail track = CommonMapper.mapTrack(rs);

        PlaylistTrack playlistTrack = new PlaylistTrack();
        playlistTrack.setTrack(track);
        playlistTrack.setPosition(rs.getInt("position"));
        playlistTrack.setAddedBy(rs.getLong("added_by"));

        Date addedAt = rs.getDate("added_at");
        playlistTrack.setAddedAt(addedAt != null ? addedAt.toLocalDate() : null);

        return playlistTrack;
    }
}
