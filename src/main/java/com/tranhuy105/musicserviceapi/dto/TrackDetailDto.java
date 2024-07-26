package com.tranhuy105.musicserviceapi.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrackDetailDto {
    private Long trackId;
    private String trackTitle;
    private Integer trackDuration;
    private Long albumId;
    private String albumTitle;
    private String albumCoverUrl;
    private Long artistId;
    private String artistStageName;
    private String artistProfilePictureUrl;
    private String role;
}

