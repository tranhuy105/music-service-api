package com.tranhuy105.musicserviceapi.model;

import lombok.Getter;

@Getter
public class QueryOptions {
    private final int page;
    private final int size;
    private final String sortBy;
    private final boolean asc;
    private final String searchString;

    private QueryOptions(int page, int size, String sortBy, boolean asc, String searchString) {
        this.page = page;
        this.size = size;
        this.sortBy = sortBy;
        this.asc = asc;
        this.searchString = searchString;
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
        private String searchString = null;

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

        public Builder search(String searchString) {
            this.searchString = searchString;
            return this;
        }

        public QueryOptions build() {
            return new QueryOptions(page, size, sortBy, asc, searchString);
        }
    }
}
