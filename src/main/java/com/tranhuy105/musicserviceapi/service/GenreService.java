package com.tranhuy105.musicserviceapi.service;

import com.tranhuy105.musicserviceapi.exception.ObjectNotFoundException;
import com.tranhuy105.musicserviceapi.model.Genre;
import com.tranhuy105.musicserviceapi.utils.CachePrefix;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreService {
    private final JdbcTemplate jdbcTemplate;
    private final CacheService cacheService;

    public List<Genre> getAllGenre() {
        String cacheKey = cacheService.getCacheKey(CachePrefix.GENRE);
        return cacheService.cacheOrFetch(cacheKey, () -> {
            String sql = "SELECT * FROM genres";
            RowMapper<Genre> rowMapper = (rs, rowNum) -> {
                long id = rs.getLong("id");
                String name = rs.getString("name");
                return new Genre(id, name);
            };
            return jdbcTemplate.query(sql, rowMapper);
        });
    }

    public Genre getGenre(Long genreId) {
        return getAllGenre()
                .stream()
                .filter(genre -> genre.getId().equals(genreId))
                .findFirst()
                .orElseThrow(
                        () -> new ObjectNotFoundException("genre", genreId.toString())
                );
    }
}
