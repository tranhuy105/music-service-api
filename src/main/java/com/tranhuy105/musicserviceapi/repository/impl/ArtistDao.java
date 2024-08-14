package com.tranhuy105.musicserviceapi.repository.impl;

import com.tranhuy105.musicserviceapi.dto.CreateArtistProfileRequestDto;
import com.tranhuy105.musicserviceapi.mapper.ArtistProfileRowMapper;
import com.tranhuy105.musicserviceapi.mapper.ArtistRowMapper;
import com.tranhuy105.musicserviceapi.model.Artist;
import com.tranhuy105.musicserviceapi.model.ArtistProfile;
import com.tranhuy105.musicserviceapi.model.Page;
import com.tranhuy105.musicserviceapi.model.QueryOptions;
import com.tranhuy105.musicserviceapi.repository.api.ArtistRepository;
import com.tranhuy105.musicserviceapi.utils.QueryUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ArtistDao implements ArtistRepository {
    private final JdbcTemplate jdbcTemplate;
    private final QueryUtil queryUtil;
    @Override
    public List<Artist> findAllArtist() {
        String sql = "SELECT * FROM artist_profiles";
        return jdbcTemplate.query(sql, new ArtistRowMapper());
    }

    @Override
    public Page<Artist> findAllArtist(QueryOptions queryOptions) {
        String baseQuery = "SELECT * FROM artist_profiles";
        return queryUtil.executeQueryWithOptions(baseQuery, queryOptions, new ArtistRowMapper());
    }

    @Override
    public List<Artist> findRelatedArtist(Long id, int limit, double threshHold) {
        String sql = "SELECT " +
                    "ap.*, " +
                    "s.similarity " +
                "FROM artist_similarity s " +
                "JOIN artist_profiles ap " +
                    "ON (s.artist1 = ap.id AND s.artist2 = ?) " +
                    "OR (s.artist2 = ap.id AND s.artist1 = ?) " +
                "WHERE s.similarity > ? " +
                "ORDER BY s.similarity DESC LIMIT ?;";
        return jdbcTemplate.query(sql, new ArtistRowMapper(), id, id, threshHold, limit);
    }

    @Override
    public Page<Artist> findArtistProfileByGenre(Long genreId, QueryOptions queryOptions) {
        String sql = "SELECT " +
                "ap.* " +
                "FROM artist_profiles ap " +
                "JOIN artist_genres ag ON ap.id = ag.artist_id AND ag.genre_id = :genreId";
        Map<String, Object> params = Map.of("genreId", genreId);
        return queryUtil.executeQueryWithOptions(sql, queryOptions, new ArtistRowMapper(), params);
    }

    @Override
    public Optional<ArtistProfile> findArtistProfileById(Long id) {
        String sql = "SELECT * FROM artist_details WHERE artist_id = ?";
        return jdbcTemplate.query(sql, new ArtistProfileRowMapper(), id).stream().findFirst();
    }

    @Override
    public Optional<Artist> findArtistByUserId(Long userId) {
        String sql = "SELECT * FROM artist_profiles WHERE user_id = ?";
        return jdbcTemplate.query(sql, new ArtistRowMapper(), userId).stream().findFirst();
    }

    @Transactional
    @Override
    public void insert(CreateArtistProfileRequestDto dto) {
        String sql = "{CALL createArtistProfile(?, ?, ?, ?)}";
        jdbcTemplate.update(sql, dto.getUserId(), dto.getStageName(), dto.getBio(), dto.getProfilePictureUrl());
    }
}
