package com.tranhuy105.musicserviceapi.service;

import com.tranhuy105.musicserviceapi.mapper.TrackRowMapper;
import com.tranhuy105.musicserviceapi.model.Genre;
import com.tranhuy105.musicserviceapi.model.Track;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class SystemService {
    private final JdbcTemplate jdbcTemplate;
    private final GenreService genreService;
    private final SimpleJdbcCall jdbcCall;

    public SystemService(JdbcTemplate jdbcTemplate, GenreService genreService) {
        this.genreService = genreService;
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcCall = new SimpleJdbcCall(this.jdbcTemplate)
                .withProcedureName("CreateSystemPlaylist");
    }

    private static final int SYSTEM_PLAYLIST_SIZE = 50;

    // ---------------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------------
    // -------------------------------SIMILAR ARTIST GENERATION--------------------------------------------
    // ---------------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------------
    @Transactional
    public void computeAndGenerateArtistSimilarity() {
        Map<String, Integer> commonFollowersMap = getCommonFollowers();
        Map<Integer, Integer> totalFollowersMap = getTotalFollowers();
        Map<String, Integer> commonGenresMap = getCommonGenres();
        Map<Integer, Integer> totalGenresMap = getTotalGenres();

        String upsertSql = "INSERT INTO artist_similarity (artist1, artist2, similarity) VALUES (?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE similarity = VALUES(similarity)";

        double weightFollower = 0.4;
        double weightGenre = 0.6;

        List<Object[]> batchArgs = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : commonFollowersMap.entrySet()) {
            String[] artists = entry.getKey().split("_");
            int artist1 = Integer.parseInt(artists[0]);
            int artist2 = Integer.parseInt(artists[1]);
            int commonFollowers = entry.getValue();
            int commonGenres = commonGenresMap.getOrDefault(entry.getKey(), 0);
            double followerSimilarity = calculateJaccardSimilarity(totalFollowersMap, artist1, artist2, commonFollowers);
            double genreSimilarity = calculateJaccardSimilarity(totalGenresMap, artist1, artist2, commonGenres);
            double combinedSimilarity = (weightFollower * followerSimilarity) + (weightGenre * genreSimilarity);

            batchArgs.add(new Object[]{artist1, artist2, combinedSimilarity});
        }

        commonFollowersMap.clear();
        commonGenresMap.clear();
        totalFollowersMap.clear();
        totalGenresMap.clear();
        jdbcTemplate.batchUpdate(upsertSql, batchArgs);
    }

    private Map<String, Integer> getCommonFollowers() {
        String sql = """
                SELECT
                f1.artist_profile_id AS artist1,
                f2.artist_profile_id AS artist2,
                COUNT(f1.user_id) AS common_followers
            FROM
                follows f1
            JOIN
                follows f2
            ON f1.user_id = f2.user_id AND f1.artist_profile_id < f2.artist_profile_id
            GROUP BY f1.artist_profile_id, f2.artist_profile_id""";
        return jdbcTemplate.query(sql, rs -> {
            Map<String, Integer> commonFollowersMap = new HashMap<>();
            while (rs.next()) {
                String key = rs.getInt("artist1") + "_" + rs.getInt("artist2");
                int count = rs.getInt("common_followers");
                commonFollowersMap.put(key, count);
            }
            return commonFollowersMap;
        });
    }

    private Map<Integer, Integer> getTotalFollowers() {
        String sql = "SELECT artist_profile_id, COUNT(user_id) AS total_followers FROM follows GROUP BY artist_profile_id";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            int artistId = rs.getInt("artist_profile_id");
            int totalFollowers = rs.getInt("total_followers");
            return Map.entry(artistId, totalFollowers);
        }).stream().collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), HashMap::putAll);
    }

    private Map<String, Integer> getCommonGenres() {
        String sql = """
                SELECT
                ag1.artist_id AS artist1,
                ag2.artist_id AS artist2,
                COUNT(ag1.genre_id) AS common_genres
            FROM
                artist_genres ag1
            JOIN
                artist_genres ag2 ON ag1.genre_id = ag2.genre_id AND ag1.artist_id < ag2.artist_id
            GROUP BY ag1.artist_id, ag2.artist_id""";
        return jdbcTemplate.query(sql, rs -> {
            Map<String, Integer> commonGenresMap = new HashMap<>();
            while (rs.next()) {
                String key = rs.getInt("artist1") + "_" + rs.getInt("artist2");
                int count = rs.getInt("common_genres");
                commonGenresMap.put(key, count);
            }
            return commonGenresMap;
        });
    }

    private Map<Integer, Integer> getTotalGenres() {
        String sql = "SELECT artist_id, COUNT(genre_id) AS total_genres FROM artist_genres GROUP BY artist_id";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            int artistId = rs.getInt("artist_id");
            int totalGenres = rs.getInt("total_genres");
            return Map.entry(artistId, totalGenres);
        }).stream().collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), HashMap::putAll);
    }

    private double calculateJaccardSimilarity(Map<Integer, Integer> totalFollowersMap, int artist1, int artist2, int common) {
        int total1 = totalFollowersMap.getOrDefault(artist1, 0);
        int total2 = totalFollowersMap.getOrDefault(artist2, 0);
        double similarity = 0.0;
        if (total1 + total2 - common > 0) {
            similarity = (double) common / (total1 + total2 - common);
        }
        return similarity;
    }

    // ---------------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------------
    // -------------------------------SYSTEM PLAYLIST GENERATION--------------------------------------------
    // ---------------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------------

    @Transactional
    public void generateSystemPlaylists() {
        List<Genre> genres = genreService.getAllGenre();
        for (Genre genre : genres) {
            generatePlaylistForGenre(genre, "Top Tracks", false);
            generatePlaylistForGenre(genre, "Newest Tracks", true);
        }
    }

    private void generatePlaylistForGenre(Genre genre, String playlistType, boolean newest) {
        String playlistName = getSystemPlaylistName(genre, playlistType);
        String playlistBio = getSystemPlaylistBio(genre, playlistType);
        String playlistCoverUrl = getSystemPlaylistCoverUrl(genre, playlistType);
        Optional<Long> playlistIdOp = findSystemPlaylistIdByName(playlistName);
        Long playlistId;

        if (playlistIdOp.isPresent()) {
            playlistId = playlistIdOp.get();
            clearPlaylistTracks(playlistId);
        } else {
            playlistId = createSystemPlaylist(playlistName, playlistBio, playlistCoverUrl);
        }

        List<Track> topTracks = findTrackByGenre(genre.getId(), newest);
        List<Object[]> batchArgs = new ArrayList<>();
        int position = 1;
        for (Track track : topTracks) {
            batchArgs.add(new Object[]{playlistId, track.getId(), position++, null});
        }

        String insertSql = "INSERT INTO playlist_track (playlist_id, track_id, position, added_by) VALUES (?, ?, ?, ?)";
        jdbcTemplate.batchUpdate(insertSql, batchArgs);
        topTracks.clear();
        batchArgs.clear();
    }

    private Optional<Long> findSystemPlaylistIdByName(String playlistName) {
        String sql = "SELECT id FROM playlists WHERE MATCH(name) AGAINST (? IN NATURAL LANGUAGE MODE) AND user_id IS NULL";
        List<Long> ids = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("id"), playlistName);
        return ids.stream().findFirst();
    }

    private void clearPlaylistTracks(Long playlistId) {
        String sql = "DELETE FROM playlist_track WHERE playlist_id = ?";
        jdbcTemplate.update(sql, playlistId);
    }

    private Long createSystemPlaylist(String name, String description, String coverUrl) {
        Map<String, Object> inParams = Map.of(
                "p_name", name,
                "p_description", description,
                "p_cover_url", coverUrl
        );
        Map<String, Object> out = jdbcCall.execute(inParams);
        return (Long) out.get("p_playlist_id");
    }

    private List<Track> findTrackByGenre(Long genreId, boolean newest) {
        String sql;
        if (newest) {
            sql = "SELECT t.* FROM tracks t JOIN track_genres tg ON t.id = tg.track_id WHERE tg.genre_id = ? ORDER BY tg.track_id DESC LIMIT ?";
        } else {
            sql = "SELECT * FROM tracks WHERE id IN (SELECT track_id FROM track_genres WHERE genre_id = ?) ORDER BY stream_count DESC LIMIT ?";
        }
        return jdbcTemplate.query(sql, new TrackRowMapper(), genreId, SYSTEM_PLAYLIST_SIZE);
    }

    private String getSystemPlaylistName(Genre genre, String playlistType) {
        return genre.getName() +"'s "+playlistType;
    }

    private String getSystemPlaylistBio(Genre genre, String playlistType) {
        return genre.getName()+"'s " + playlistType+", generated by spotify.";
    }

    private String getSystemPlaylistCoverUrl(Genre genre, String playlistType) {
        String genreName = genre.getName();
        String text = playlistType + " in " + genreName;
        String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
        return "https://via.placeholder.com/300x300/1DB954/ffffff.png?text=" + encodedText;
    }
}
