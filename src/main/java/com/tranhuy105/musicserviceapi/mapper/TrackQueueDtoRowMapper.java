package com.tranhuy105.musicserviceapi.mapper;

import com.tranhuy105.musicserviceapi.dto.TrackQueueDto;
import com.tranhuy105.musicserviceapi.model.SourceType;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TrackQueueDtoRowMapper implements RowMapper<TrackQueueDto> {
    private final SourceType sourceType;

    public TrackQueueDtoRowMapper(SourceType sourceType) {
        this.sourceType = sourceType;
    }
    @Override
    public TrackQueueDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        return switch (sourceType) {
            case PLAYLIST -> new TrackQueueDto(
                    rs.getLong("playlist_id"),
                    sourceType,
                    rs.getInt("position"),
                    rs.getLong("track_id"),
                    rs.getLong("added_by"),
                    rs.getTimestamp("added_at").toLocalDateTime()
            );
            case ALBUM -> new TrackQueueDto(
                    rs.getLong("album_id"),
                    sourceType,
                    rowNum,
                    rs.getLong("id"),
                    null,
                    null
            );
            case LIKED -> new TrackQueueDto(
                    rs.getLong("user_id"),
                    sourceType,
                    rowNum,
                    rs.getLong("track_id"),
                    null,
                    null
            );
        };
    }
}
