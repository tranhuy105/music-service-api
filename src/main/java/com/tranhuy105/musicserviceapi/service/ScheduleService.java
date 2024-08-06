package com.tranhuy105.musicserviceapi.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final JdbcTemplate jdbcTemplate;
    private final Map<Long, Long> streamCountCache = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(ScheduleService.class);

    public void incrementStreamCount(Long trackId) {
        streamCountCache.merge(trackId, 1L, Long::sum);
    }

    @Scheduled(fixedRate = 3600000) // Runs every hour
    @Transactional
    public void updateStreamCounts() {
        if (streamCountCache.isEmpty()) {
            return;
        }

        String sql = "UPDATE tracks SET stream_count = stream_count + ? WHERE id = ?";
        try {
            jdbcTemplate.batchUpdate(sql,
                    streamCountCache.entrySet().stream()
                            .map(entry -> new Object[]{entry.getValue(), entry.getKey()})
                            .toList()
            );
            logger.info("Successfully updated stream counts.");
            streamCountCache.clear();
        } catch (Exception e) {
            logger.error("Error updating stream counts: ", e);
        }
    }

    @Scheduled(fixedRate = 3600000) // Runs every hour
    @Transactional
    public void updateFollowerCounts() {
        String sql = "UPDATE artist_profiles ap " +
                "SET follower_count = ( " +
                "    SELECT COUNT(*) " +
                "    FROM follows f " +
                "    WHERE f.artist_profile_id = ap.id " +
                ")";
        try {
            jdbcTemplate.update(sql);
            logger.info("Successfully updated follower counts.");
        } catch (Exception e) {
            logger.error("Error updating follower counts: ", e);
        }
    }
}
