package com.tranhuy105.musicserviceapi.model;

import lombok.Getter;

import java.util.Map;

@Getter
public class QueryOptions {
    private final int page;
    private final int size;
    private final String sortBy;
    private final boolean asc;
    private final Map<String, String> searchCriteria;

    private QueryOptions(int page, int size, String sortBy, boolean asc, Map<String, String> searchCriteria) {
        this.page = page;
        this.size = size;
        this.sortBy = sortBy;
        this.asc = asc;
        this.searchCriteria = searchCriteria;
    }

    public int getOffset() {
        return (page - 1) * size;
    }

    public String getOrder() {
        return asc ? "ASC" : "DESC";
    }

    public static Builder of(int page, int size) {
        return new Builder(page, size);
    }

    public static class Builder {
        private final int page;
        private final int size;
        private String sortBy = null;
        private boolean asc = true;
        private Map<String, String> searchCriteria = null;

        public Builder(int page, int size) {
            this.page = page;
            this.size = size;
        }

        public Builder sortBy(String sortBy) {
            this.sortBy = sortBy;
            return this;
        }

        public Builder desc() {
            this.asc = false;
            return this;
        }

        public Builder asc() {
            this.asc = true;
            return this;
        }

        public Builder search(Map<String, String> searchCriteria) {
            this.searchCriteria = searchCriteria;
            return this;
        }

        public QueryOptions build() {
            return new QueryOptions(page, size, sortBy, asc, searchCriteria);
        }
    }
}
