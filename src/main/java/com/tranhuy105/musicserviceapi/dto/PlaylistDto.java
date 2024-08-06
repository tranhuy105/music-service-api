package com.tranhuy105.musicserviceapi.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlaylistDto {
    private Long userId;
    private String name;
    private String description;
    private Boolean isPublic;
    private String coverUrl;
}