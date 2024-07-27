package com.tranhuy105.musicserviceapi.mapper;

import com.tranhuy105.musicserviceapi.model.AlbumDetail;
import com.tranhuy105.musicserviceapi.model.ArtistProfile;
import com.tranhuy105.musicserviceapi.model.ref.AlbumArtist;
import com.tranhuy105.musicserviceapi.model.ref.ArtistAlbum;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AlbumDetailsRowMapper implements RowMapper<AlbumDetail> {
    @Override
    public AlbumDetail mapRow(ResultSet rs, int rowNum) throws SQLException {
        AlbumDetail albumDetail = new AlbumDetail();
        albumDetail.setId(rs.getLong("album_id"));
        albumDetail.setTitle(rs.getString("album_title"));
        albumDetail.setIsSingle(rs.getBoolean("is_single"));
        albumDetail.setCoverUrl(rs.getString("cover_url"));
        albumDetail.setReleaseDate(rs.getDate("release_date") != null ? rs.getDate("release_date").toLocalDate() : null);

        List<AlbumArtist> artists = new ArrayList<>();

        do {
            AlbumArtist artist = new AlbumArtist();
            artist.setId(rs.getLong("artist_id"));
            artist.setStageName(rs.getString("artist_stage_name"));
            artist.setProfilePictureUrl(rs.getString("artist_profile_picture_url"));
            artist.setRole(rs.getString("role"));

            artists.add(artist);
        } while (rs.next());

        albumDetail.setArtists(artists);

        return albumDetail;
    }
}
