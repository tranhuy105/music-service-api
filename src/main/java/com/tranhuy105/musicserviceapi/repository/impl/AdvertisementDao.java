package com.tranhuy105.musicserviceapi.repository.impl;

import com.tranhuy105.musicserviceapi.mapper.AdRowMapper;
import com.tranhuy105.musicserviceapi.model.Advertisement;
import com.tranhuy105.musicserviceapi.repository.api.AdvertisementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AdvertisementDao implements AdvertisementRepository {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void save(Advertisement advertisement) {
        String sql = "INSERT INTO advertisements (title, description, image_url, target_url, start_date, end_date, region_code) VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(
                sql,
                advertisement.getTitle(),
                advertisement.getDescription(),
                advertisement.getImageUrl(),
                advertisement.getTargetUrl(),
                advertisement.getStartDate(),
                advertisement.getEndDate(),
                advertisement.getRegionCode()
        );
    }

    @Override
    public Optional<Advertisement> findById(Long id) {
        String sql = "SELECT * FROM advertisements WHERE id = ?";
        return jdbcTemplate.query(sql, new AdRowMapper(), id).stream().findFirst();
    }

    @Override
    public Optional<Advertisement> findRandomAdByRegion(String regionCode) {
        String sql = "SELECT * FROM advertisements WHERE region_code = ? ORDER BY RAND() LIMIT 1";
        return jdbcTemplate.query(sql, new AdRowMapper(), regionCode).stream().findFirst();
    }

    @Override
    public List<Advertisement> findAll() {
        String sql = "SELECT * FROM advertisements";
        return jdbcTemplate.query(sql, new AdRowMapper());
    }

    @Override
    public void update(Advertisement advertisement) {
        String sql = "UPDATE advertisements SET title = ?, description = ?, image_url = ?, target_url = ?, start_date = ?, end_date = ?, region_code = ? WHERE id = ?";
        jdbcTemplate.update(
                sql,
                advertisement.getTitle(),
                advertisement.getDescription(),
                advertisement.getImageUrl(),
                advertisement.getTargetUrl(),
                advertisement.getStartDate(),
                advertisement.getEndDate(),
                advertisement.getRegionCode(),
                advertisement.getId());
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM advertisements WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
