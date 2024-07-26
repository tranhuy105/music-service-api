package com.tranhuy105.musicserviceapi.mapper;

import com.tranhuy105.musicserviceapi.model.ArtistProfile;
import com.tranhuy105.musicserviceapi.model.ref.ArtistAlbum;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ArtistProfileRowMapper implements RowMapper<ArtistProfile> {

    @Override
    public ArtistProfile mapRow(ResultSet rs, int rowNum) throws SQLException {
        long artistId = rs.getLong("id");
        String stageName = rs.getString("stage_name");
        String bio = rs.getString("bio");
        String profilePictureUrl = rs.getString("profile_picture_url");

        ArtistProfile artistProfile = new ArtistProfile();
        artistProfile.setId(artistId);
        artistProfile.setStageName(stageName);
        artistProfile.setBio(bio);
        artistProfile.setProfilePictureUrl(profilePictureUrl);
        artistProfile.setAlbums(new ArrayList<>());

        do {
            long albumId = rs.getLong("album_id");
            ArtistAlbum album = new ArtistAlbum();
            album.setId(Long.toString(albumId));
            album.setTitle(rs.getString("album_title"));
            album.setReleaseDate(rs.getDate("release_date") != null ? rs.getDate("release_date").toLocalDate() : null);
            album.setIsSingle(rs.getBoolean("is_single"));
            album.setCoverUrl(rs.getString("cover_url"));
            album.setRole(rs.getString("role"));

            artistProfile.getAlbums().add(album);
        } while (rs.next() && rs.getLong("id") == artistId);

        return artistProfile;
    }
}

