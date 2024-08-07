package com.tranhuy105.musicserviceapi.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Advertisement extends MediaItem {
    private Long id;
    private String title;
    private String coverUrl;

    @Override
    public String getURI() {
        return String.format("spotify:ad:%d", id);
    }
}
