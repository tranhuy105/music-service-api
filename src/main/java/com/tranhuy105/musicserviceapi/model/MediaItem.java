package com.tranhuy105.musicserviceapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class MediaItem {
    @JsonIgnore
    public abstract String getURI();

    @JsonIgnore
    public abstract Long getItemId();
}
