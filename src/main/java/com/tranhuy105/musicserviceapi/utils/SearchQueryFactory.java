package com.tranhuy105.musicserviceapi.utils;

import com.tranhuy105.musicserviceapi.mapper.AlbumRowMapper;
import com.tranhuy105.musicserviceapi.mapper.ArtistRowMapper;
import com.tranhuy105.musicserviceapi.mapper.PlaylistSummaryRowMapper;
import com.tranhuy105.musicserviceapi.mapper.TrackDetailRowMapper;

import java.util.Map;

public class SearchQueryFactory {

    public static String buildSearchQuery(String searchString, Class<?> clazz, Map<String, Object> params) {
        if (searchString == null || searchString.trim().isEmpty()) {
            return "";
        }
        if (clazz.equals(TrackDetailRowMapper.class)) {
            return buildTrackSearchQuery(searchString, params);
        } else if (clazz.equals(AlbumRowMapper.class)) {
            return buildAlbumSearchQuery(searchString, params);
        } else if (clazz.equals(ArtistRowMapper.class)) {
            return buildArtistSearchQuery(searchString, params);
        } else if (clazz.equals(PlaylistSummaryRowMapper.class)) {
            return buildPlaylistSearchQuery(searchString, params);
        } else {
            throw new IllegalArgumentException("Unknown class type: " + clazz.getName());
        }
    }

    private static String buildTrackSearchQuery(String searchString, Map<String, Object> params) {
        params.put("searchString", searchString);
        return "MATCH (t.title) AGAINST (:searchString) OR MATCH(a.title) AGAINST (:searchString) OR MATCH(ap.stage_name) AGAINST (:searchString)";
    }

    private static String buildAlbumSearchQuery(String searchString, Map<String, Object> params) {
        params.put("searchString", searchString);
        return "MATCH (title) AGAINST (:searchString)";
    }

    private static String buildArtistSearchQuery(String searchString, Map<String, Object> params) {
        params.put("searchString", searchString);
        return "MATCH (stage_name) AGAINST (:searchString)";
    }

    private static String buildPlaylistSearchQuery(String searchString, Map<String, Object> params) {
        params.put("searchString", searchString);
        return "MATCH (name) AGAINST (:searchString)";
    }
}
