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
public class ArtistRoleDto {
    @NotNull
    private Long artistId;
    @NotNull
    @Pattern(regexp = "main|support", message = "Role must be either 'main' or 'support'")
    private String role;
}
