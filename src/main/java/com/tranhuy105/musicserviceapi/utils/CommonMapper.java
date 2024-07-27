package com.tranhuy105.musicserviceapi.utils;

import com.tranhuy105.musicserviceapi.model.Track;
import com.tranhuy105.musicserviceapi.model.ref.AlbumArtist;
import com.tranhuy105.musicserviceapi.model.ref.TrackAlbum;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommonMapper {
    public static Track mapTrack(ResultSet rs) throws SQLException {
        Long trackId = rs.getLong("track_id");
        String trackTitle = rs.getString("track_title");
        Integer trackDuration = rs.getInt("track_duration");
        Long albumId = rs.getLong("album_id");
        String albumTitle = rs.getString("album_title");
        String albumCoverUrl = rs.getString("album_cover_url");

        String artistIdsString = rs.getString("artist_ids");
        String artistStageNamesString = rs.getString("artist_stage_names");
        String artistProfilePicturesString = rs.getString("artist_profile_pictures");
        String rolesString = rs.getString("roles");

        List<String> artistIds = parseStringToList(artistIdsString);
        List<String> artistStageNames = parseStringToList(artistStageNamesString);
        List<String> artistProfilePictures = parseStringToList(artistProfilePicturesString);
        List<String> roles = parseStringToList(rolesString);
        List<AlbumArtist> albumArtists = artistIds.stream()
                .map(id -> new AlbumArtist(Long.valueOf(id), artistStageNames.get(artistIds.indexOf(id)), artistProfilePictures.get(artistIds.indexOf(id)), roles.get(artistIds.indexOf(id))))
                .collect(Collectors.toList());

        TrackAlbum trackAlbum = new TrackAlbum(albumId, albumTitle, albumCoverUrl, albumArtists);

        return new Track(trackId, trackTitle, trackDuration, trackAlbum);
    }

    private static List<String> parseStringToList(String str) {
        return str == null || str.isEmpty() ? List.of() : Arrays.asList(str.split(","));
    }
}
