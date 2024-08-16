package com.tranhuy105.musicserviceapi.dto;

import lombok.Data;

import java.util.List;

@Data
public class UpdateArtistProfileRequestDto {
    private String stageName;
    private String bio;
    private String profilePictureUrl;
    private List<Long> genreIds;
}
