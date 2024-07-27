package com.tranhuy105.musicserviceapi.mapper;

import com.tranhuy105.musicserviceapi.model.Album;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AlbumRowMapper implements RowMapper<Album> {
    @Override
    public Album mapRow(ResultSet rs, int rowNum) throws SQLException {
        Album album = new Album();
        album.setId(rs.getLong("id"));
        album.setTitle(rs.getString("title"));
        Date releaseDate = rs.getDate("release_date");
        album.setReleaseDate(releaseDate != null ? releaseDate.toLocalDate(): null);
        album.setIsSingle(rs.getBoolean("is_single"));
        album.setCoverUrl(rs.getString("cover_url"));

        return album;
    }
}
