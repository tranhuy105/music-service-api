package com.tranhuy105.musicserviceapi.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UpdateArtistProfileRequestDto {
    @NotEmpty @NotNull
    private String stageName;
    private String bio;
    private String profilePictureUrl;
    @NotNull
    private List<Long> genreIds;
}
