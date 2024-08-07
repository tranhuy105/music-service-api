package com.tranhuy105.musicserviceapi.mapper;

import com.tranhuy105.musicserviceapi.model.PlaylistTrack;
import com.tranhuy105.musicserviceapi.model.TrackDetail;
import com.tranhuy105.musicserviceapi.utils.CommonMapper;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class PlaylistTrackRowMapper implements RowMapper<PlaylistTrack> {
    private final boolean isSavedPlaylist;

    public PlaylistTrackRowMapper(boolean isSavedPlaylist) {
        this.isSavedPlaylist = isSavedPlaylist;
    }

    public PlaylistTrackRowMapper() {
        this.isSavedPlaylist = false;
    }

    @Override
    public PlaylistTrack mapRow(ResultSet rs, int rowNum) throws SQLException {
        TrackDetail track = CommonMapper.mapTrack(rs);

        PlaylistTrack playlistTrack = new PlaylistTrack();
        playlistTrack.setTrack(track);
        playlistTrack.setPosition(isSavedPlaylist ? rowNum + 1 : rs.getInt("position"));
        playlistTrack.setAddedBy(rs.getLong("added_by"));

        Timestamp addedAt = rs.getTimestamp("added_at");
        playlistTrack.setAddedAt(addedAt != null ? addedAt.toLocalDateTime() : null);

        return playlistTrack;
    }
}
