package com.tranhuy105.musicserviceapi.dto;

import com.tranhuy105.musicserviceapi.model.SourceType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TrackQueueDto {
    private Long sourceId;
    private SourceType sourceType;
    private Integer position;
    private Long trackId;
    private Long addedBy;
    private LocalDateTime addedAt;
}
