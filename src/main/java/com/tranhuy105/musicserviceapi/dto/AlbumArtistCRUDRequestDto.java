package com.tranhuy105.musicserviceapi.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AlbumArtistCRUDRequestDto {
    private Long albumId;
    @NotNull
    private Long artistId;
    @Pattern(regexp = "main|support", message = "Role must be either 'main' or 'support'")
    @NotNull
    private String role;
}

