package com.tranhuy105.musicserviceapi.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Playlist {
    private Long id;
    private Long userId;
    private String name;
    private String description;
    private Boolean isPublic;
    private String coverUrl;
    private Integer totalTrack;
}
