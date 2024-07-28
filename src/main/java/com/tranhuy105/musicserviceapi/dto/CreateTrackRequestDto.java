package com.tranhuy105.musicserviceapi.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateTrackRequestDto {
    @NotNull
    private Long albumId;
    private String title;
    private Integer duration;
}

