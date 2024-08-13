package com.tranhuy105.musicserviceapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RecommendService {
    private final JdbcTemplate jdbcTemplate;

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

    @Transactional
    public void computeAndSaveSimilarity() {
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
            double followerSimilarity = caculateJaccardSimilary(totalFollowersMap, artist1, artist2, commonFollowers);;
            double genreSimilarity = caculateJaccardSimilary(totalGenresMap, artist1, artist2, commonGenres);
            double combinedSimilarity = (weightFollower * followerSimilarity) + (weightGenre * genreSimilarity);

            batchArgs.add(new Object[]{artist1, artist2, combinedSimilarity});
        }

        commonFollowersMap.clear();
        commonGenresMap.clear();
        totalFollowersMap.clear();
        totalGenresMap.clear();
        jdbcTemplate.batchUpdate(upsertSql, batchArgs);
    }

    private double caculateJaccardSimilary(Map<Integer, Integer> totalFollowersMap, int artist1, int artist2, int common) {
        int total1 = totalFollowersMap.getOrDefault(artist1, 0);
        int total2 = totalFollowersMap.getOrDefault(artist2, 0);
        double similarity = 0.0;
        if (total1 + total2 - common > 0) {
            similarity = (double) common / (total1 + total2 - common);
        }
        return similarity;
    }
}
