package com.tranhuy105.musicserviceapi.utils;

import com.tranhuy105.musicserviceapi.model.Page;
import com.tranhuy105.musicserviceapi.model.QueryOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class QueryUtil {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public <T> Page<T> executeQueryWithOptions(String baseQuery, QueryOptions queryOptions, RowMapper<T> rowMapper) {
        Map<String, Object> params = new HashMap<>();

        String paginatedSql = toPaginatedSqlString(baseQuery, queryOptions, params);
        List<T> results = namedParameterJdbcTemplate.query(paginatedSql, params, rowMapper);

        String countSql = toCountSqlString(baseQuery, queryOptions, params);
        Long totalItems = namedParameterJdbcTemplate.queryForObject(countSql, params, Long.class);
        long totalItemsSafe = (totalItems != null) ? totalItems : 0;

        return new Page<>(results, queryOptions.getPage(), queryOptions.getSize(), totalItemsSafe);
    }
    private String toPaginatedSqlString(String baseQuery, QueryOptions queryOptions, Map<String, Object> params) {
        StringBuilder sql = new StringBuilder(baseQuery);
        appendSearchQuery(queryOptions, params, sql);
        if (queryOptions.getSortBy() != null && isValidSortBy(queryOptions.getSortBy())) {
            sql.append(" ORDER BY ").append(queryOptions.getSortBy()).append(" ").append(queryOptions.getOrder());
        }
        sql.append(" LIMIT ").append(queryOptions.getSize()).append(" OFFSET ").append(queryOptions.getOffset());

        return sql.toString();
    }

    private String toCountSqlString(String baseQuery, QueryOptions queryOptions, Map<String, Object> params) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM (");
        sql.append(baseQuery);
        appendSearchQuery(queryOptions, params, sql);
        sql.append(") AS count_table");
        return sql.toString();
    }

    private void appendSearchQuery(QueryOptions queryOptions, Map<String, Object> params, StringBuilder sql) {
        if (queryOptions.getSearchCriteria() != null && !queryOptions.getSearchCriteria().isEmpty()) {
            sql.append(" WHERE ");
            queryOptions.getSearchCriteria().forEach((field, value) -> {
                sql.append(field).append(" LIKE :").append(field).append(" AND ");
                params.put(field, "%" + value + "%");
            });
            sql.setLength(sql.length() - 5);
        }
    }

    private boolean isValidSortBy(String sortBy) {
        return sortBy.matches("^[a-zA-Z_]+$");
    }
}