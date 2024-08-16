package com.tranhuy105.musicserviceapi.mapper;

import com.tranhuy105.musicserviceapi.model.ArtistRequest;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ArtistRequestRowMapper implements RowMapper<ArtistRequest> {
    @Override
    public ArtistRequest mapRow(ResultSet rs, int rowNum) throws SQLException {
        ArtistRequest request = new ArtistRequest();
        request.setId(rs.getLong("id"));
        request.setUserId(rs.getLong("user_id"));
        request.setArtistName(rs.getString("artist_name"));
        request.setGenre(rs.getString("genre"));
        request.setPortfolioUrl(rs.getString("portfolio_url"));
        request.setBio(rs.getString("bio"));
        request.setSocialMediaLinks(rs.getString("social_media_links"));
        request.setRequestDate(rs.getTimestamp("request_date").toLocalDateTime());
        request.setStatus(rs.getString("status"));
        request.setReviewDate(rs.getTimestamp("review_date") != null ? rs.getTimestamp("review_date").toLocalDateTime() : null);
        request.setReviewedBy(rs.getLong("reviewed_by"));
        request.setReason(rs.getString("reason"));
        return request;
    }
}
