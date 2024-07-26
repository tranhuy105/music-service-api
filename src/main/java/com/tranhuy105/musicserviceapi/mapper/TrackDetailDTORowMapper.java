package com.tranhuy105.musicserviceapi.mapper;

import com.tranhuy105.musicserviceapi.dto.TrackDetailDto;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TrackDetailDTORowMapper implements RowMapper<TrackDetailDto> {
    @Override
    public TrackDetailDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        TrackDetailDto dto = new TrackDetailDto();
        dto.setTrackId(rs.getLong("track_id"));
        dto.setTrackTitle(rs.getString("track_title"));
        dto.setTrackDuration(rs.getInt("track_duration"));
        dto.setAlbumId(rs.getLong("album_id"));
        dto.setAlbumTitle(rs.getString("album_title"));
        dto.setAlbumCoverUrl(rs.getString("album_cover_url"));
        dto.setArtistId(rs.getLong("artist_id"));
        dto.setArtistStageName(rs.getString("artist_stage_name"));
        dto.setArtistProfilePictureUrl(rs.getString("artist_profile_picture_url"));
        dto.setRole(rs.getString("role"));

        return dto;
    }
}
