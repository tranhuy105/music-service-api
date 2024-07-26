package com.tranhuy105.musicserviceapi.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class PlaylistTrackDto {
    private int position;
    private LocalDate addedAt;
    private long addedBy;
    private long trackId;
    private String trackTitle;
    private int trackDuration;
    private long albumId;
    private String albumTitle;
    private String albumCoverUrl;
    private long artistId;
    private String artistStageName;
    private String artistProfilePictureUrl;
    private String artistRole;
}

