package com.tranhuy105.musicserviceapi.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateArtistProfileRequestDto {
    @NotNull
    private Long userId;
    @NotNull
    private String stageName;
    private String bio;
    private String profilePictureUrl;
}

