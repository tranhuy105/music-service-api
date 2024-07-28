package com.tranhuy105.musicserviceapi.repository.impl;

import com.tranhuy105.musicserviceapi.mapper.AlbumDetailsRowMapper;
import com.tranhuy105.musicserviceapi.mapper.AlbumRowMapper;
import com.tranhuy105.musicserviceapi.model.Album;
import com.tranhuy105.musicserviceapi.model.AlbumDetail;
import com.tranhuy105.musicserviceapi.model.Page;
import com.tranhuy105.musicserviceapi.model.QueryOptions;
import com.tranhuy105.musicserviceapi.repository.api.AlbumRepository;
import com.tranhuy105.musicserviceapi.utils.QueryUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AlbumDao implements AlbumRepository {
    private final JdbcTemplate jdbcTemplate;
    private final QueryUtil queryUtil;
    @Override
    public Optional<AlbumDetail> findAlbumDetailById(Long albumId) {
        String sql = "SELECT * FROM album_details WHERE album_id = ?";
        return jdbcTemplate.query(sql, new AlbumDetailsRowMapper(), albumId).stream().findFirst();
    }

    @Override
    public List<Album> findAllAlbum() {
        String sql = "SELECT * FROM albums";
        return jdbcTemplate.query(sql, new AlbumRowMapper());
    }

    @Override
    public Page<Album> findAllAlbum(QueryOptions queryOptions) {
        String baseQuery = "SELECT * FROM albums";
        return queryUtil.executeQueryWithOptions(baseQuery, queryOptions, new AlbumRowMapper());
    }
}
