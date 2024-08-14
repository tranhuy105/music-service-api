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

    public <T> Page<T> executeQueryWithOptions(String baseQuery,
                                               QueryOptions queryOptions,
                                               RowMapper<T> rowMapper) {
        return executeQueryWithOptions(baseQuery, queryOptions, rowMapper, new HashMap<>());
    }

    public <T> Page<T> executeQueryWithOptions(String baseQuery,
                                               QueryOptions queryOptions,
                                               RowMapper<T> rowMapper,
                                               Map<String, Object> params) {
        String paginatedSql = toPaginatedSqlString(baseQuery, queryOptions, params, rowMapper);
        List<T> results = namedParameterJdbcTemplate.query(paginatedSql, params, rowMapper);

        String countSql = toCountSqlString(baseQuery, queryOptions, params, rowMapper);
        Long totalItems = namedParameterJdbcTemplate.queryForObject(countSql, params, Long.class);

        long totalItemsSafe = (totalItems != null) ? totalItems : 0;

        return new Page<>(results, queryOptions.getPage(), queryOptions.getSize(), totalItemsSafe);
    }

    private <T> String toPaginatedSqlString(String baseQuery, QueryOptions queryOptions, Map<String, Object> params, RowMapper<T> rowMapper) {
        StringBuilder sql = new StringBuilder(baseQuery);
        sql = insertWhereClause(sql, queryOptions, params, rowMapper);
        if (queryOptions.getSortBy() != null && isValidSortBy(queryOptions.getSortBy())) {
            sql.append(" ORDER BY ").append(queryOptions.getSortBy()).append(" ").append(queryOptions.getOrder());
        }
        sql.append(" LIMIT ").append(queryOptions.getSize()).append(" OFFSET ").append(queryOptions.getOffset());

        return sql.toString();
    }

    private <T> String toCountSqlString(String baseQuery, QueryOptions queryOptions, Map<String, Object> params, RowMapper<T> rowMapper) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM (");
        sql.append(baseQuery);
        sql = insertWhereClause(sql, queryOptions, params, rowMapper);
        sql.append(") AS count_table");
        return sql.toString();
    }

    private <T> StringBuilder insertWhereClause(StringBuilder sql, QueryOptions queryOptions, Map<String, Object> params, RowMapper<T> rowMapper) {
        int groupByIndex = sql.indexOf("GROUP BY");

        if (groupByIndex != -1) {
            String beforeGroupBy = sql.substring(0, groupByIndex);
            String groupByClause = sql.substring(groupByIndex);
            sql = new StringBuilder(beforeGroupBy);
            appendSearchQuery(queryOptions, params, sql, rowMapper);
            sql.append(groupByClause);
        } else {
            appendSearchQuery(queryOptions, params, sql, rowMapper);
        }
        return sql;
    }

    private <T> void appendSearchQuery(QueryOptions queryOptions, Map<String, Object> params, StringBuilder sql, RowMapper<T> rowMapper) {
        if (queryOptions.getSearchString() != null && !queryOptions.getSearchString().trim().isEmpty()) {
            String searchQuery = SearchQueryFactory.buildSearchQuery(queryOptions.getSearchString(), rowMapper.getClass(), params);
            if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                if (sql.toString().contains("WHERE")) {
                    sql.append(" AND ");
                } else {
                    sql.append(" WHERE ");
                }
                sql.append(searchQuery);
            }
        }
    }

    private boolean isValidSortBy(String sortBy) {
        return sortBy.matches("^[a-zA-Z_.]+$");
    }
}
