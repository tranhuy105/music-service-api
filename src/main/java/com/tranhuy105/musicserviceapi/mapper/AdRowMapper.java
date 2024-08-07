package com.tranhuy105.musicserviceapi.mapper;

import com.tranhuy105.musicserviceapi.model.Advertisement;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AdRowMapper implements RowMapper<Advertisement> {
    @Override
    public Advertisement mapRow(ResultSet rs, int rowNum) throws SQLException {
        Advertisement ad = new Advertisement();
        ad.setId(rs.getLong("id"));
        ad.setTitle(rs.getString("title"));
        ad.setDescription(rs.getString("description"));
        ad.setImageUrl(rs.getString("image_url"));
        ad.setTargetUrl(rs.getString("target_url"));
        ad.setStartDate(rs.getTimestamp("start_date").toLocalDateTime());
        ad.setEndDate(rs.getTimestamp("end_date").toLocalDateTime());
        ad.setRegionCode(rs.getString("region_code"));
        return ad;
    }
}
