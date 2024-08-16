package com.tranhuy105.musicserviceapi.repository.impl;

import com.tranhuy105.musicserviceapi.mapper.ArtistRequestRowMapper;
import com.tranhuy105.musicserviceapi.model.ArtistRequest;
import com.tranhuy105.musicserviceapi.model.Page;
import com.tranhuy105.musicserviceapi.model.QueryOptions;
import com.tranhuy105.musicserviceapi.repository.api.ArtistRequestRepository;
import com.tranhuy105.musicserviceapi.utils.QueryUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ArtistRequestDao implements ArtistRequestRepository {
    private final JdbcTemplate jdbcTemplate;
    private final QueryUtil queryUtil;

    @Override
    public void createArtistRequest(Long userId, String artistName, String genre, String portfolioUrl, String bio, String socialMediaLinks) {
        String sql = "INSERT INTO artist_request (user_id, artist_name, genre, portfolio_url, bio, social_media_links) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, userId, artistName, genre, portfolioUrl, bio, socialMediaLinks);
    }

    @Override
    public Page<ArtistRequest> getPendingRequests(QueryOptions queryOptions) {
        String sql = "SELECT * FROM artist_request WHERE status = 'PENDING'";
        return queryUtil.executeQueryWithOptions(sql, queryOptions, new ArtistRequestRowMapper());
    }

    @Override
    public void reviewRequest(Long requestId, String status, Long reviewedBy, String reason) {
        String sql = "UPDATE artist_request SET status = ?, review_date = CURRENT_TIMESTAMP, reviewed_by = ?, reason = ? WHERE id = ?";
        jdbcTemplate.update(sql, status, reviewedBy, reason, requestId);
    }

    @Override
    public Optional<ArtistRequest> findArtistRequestById(Long id) {
        String sql = "SELECT * FROM artist_request WHERE id = ?";
        return jdbcTemplate.query(sql, new ArtistRequestRowMapper(), id)
                .stream()
                .findFirst();
    }

    @Override
    public List<ArtistRequest> getRequestsByUserId(Long userId) {
        String sql = "SELECT * FROM artist_request WHERE user_id = ?";
        return jdbcTemplate.query(sql, new ArtistRequestRowMapper(), userId);
    }
}
