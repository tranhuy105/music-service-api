package com.tranhuy105.musicserviceapi.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Advertisement extends MediaItem {
    private Long id;
    private String title;
    private String description;
    private String imageUrl;
    private String targetUrl;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String regionCode;

    @Override
    public String getURI() {
        return String.format("spotify:ad:%d", id);
    }

    @Override
    public Long getItemId() {
        return this.id;
    }
}
