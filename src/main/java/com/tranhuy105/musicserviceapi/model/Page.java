package com.tranhuy105.musicserviceapi.model;
import lombok.Getter;

import java.util.List;

@Getter
public class Page<T> {
    private final int page;
    private final int size;
    private final long totalItems;
    private final int totalPages;
    private final boolean hasNext;
    private final boolean hasPrevious;
    private final List<T> items;

    public Page(List<T> items, int page, int size, long totalItems) {
        this.page = page;
        this.size = size;
        this.totalItems = totalItems;
        this.totalPages = (int) Math.ceil((double) totalItems / size);
        this.hasNext = page < totalPages;
        this.hasPrevious = page > 1;
        this.items = items;
    }
}
