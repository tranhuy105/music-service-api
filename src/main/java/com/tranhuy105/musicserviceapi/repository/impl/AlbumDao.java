package com.tranhuy105.musicserviceapi.repository.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tranhuy105.musicserviceapi.dto.CreateAlbumRequestDto;
import com.tranhuy105.musicserviceapi.dto.AlbumArtistCRUDRequestDto;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AlbumDao implements AlbumRepository {
    private final JdbcTemplate jdbcTemplate;
    private final QueryUtil queryUtil;
    private final ObjectMapper objectMapper;

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

    @Override
    public List<Album> findRelatedAlbum(Long id, int limit) {
        String sql = "SELECT DISTINCT a.* " +
                "FROM albums a " +
                "JOIN album_artists aa ON a.id = aa.album_id " +
                "WHERE aa.artist_id IN (" +
                "SELECT artist_id " +
                "FROM album_artists " +
                "WHERE album_id = ?" +
                ") " +
                "AND a.id != ? LIMIT ?";
        return jdbcTemplate.query(sql, new AlbumRowMapper(), id, id, limit);
    }

    @Transactional
    @Override
    public void insert(CreateAlbumRequestDto dto) {
        String artistRolesJson;
        try {
            artistRolesJson = objectMapper.writeValueAsString(dto.getArtistRoles());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        String sql = "{CALL createAlbumWithArtists(?, ?, ?, ?, ?)}";
        jdbcTemplate.update(sql, dto.getTitle(), dto.getReleaseDate(), dto.getIsSingle(), dto.getCoverUrl(), artistRolesJson);
    }

    @Override
    public void update(Album album) {
        String sql = "UPDATE albums SET title = ?, cover_url = ?, release_date = ?, is_single = ? WHERE id = ?";
        jdbcTemplate.update(sql, album.getTitle(), album.getCoverUrl(), album.getReleaseDate(), album.getIsSingle(), album.getId());
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM albums WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public void linkNewArtist(AlbumArtistCRUDRequestDto dto) {
        String sql = "INSERT INTO album_artists (album_id, artist_id, role) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, dto.getAlbumId(), dto.getArtistId(), dto.getRole());
    }

    @Override
    public void unlinkArtist(AlbumArtistCRUDRequestDto dto) {
        String sql = "DELETE FROM album_artists WHERE album_id = ? AND artist_id = ?";
        jdbcTemplate.update(sql, dto.getAlbumId(), dto.getArtistId());
    }

    @Override
    public void updateLinkedArtist(AlbumArtistCRUDRequestDto dto) {
        String sql = "UPDATE album_artists SET role = ? WHERE album_id = ? AND artist_id = ?";
        jdbcTemplate.update(sql, dto.getRole(), dto.getAlbumId(), dto.getArtistId());
    }
}
