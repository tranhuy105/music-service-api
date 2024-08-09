package com.tranhuy105.musicserviceapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Track extends MediaItem {
    private Long id;
    private String title;
    private Integer duration;
    private Long streamCount;

    @Override
    public String getURI() {
        return String.format("spotify:track:%d", id);
    }

    @Override
    public Long getItemId() {
        return this.id;
    }
}
