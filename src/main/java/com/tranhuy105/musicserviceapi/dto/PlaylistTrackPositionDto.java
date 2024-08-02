package com.tranhuy105.musicserviceapi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PlaylistTrackPositionDto {
    private Long playlistId;
    private Integer position;
    private Long trackId;
    private Long addedBy;
    private LocalDateTime addedAt;
}
