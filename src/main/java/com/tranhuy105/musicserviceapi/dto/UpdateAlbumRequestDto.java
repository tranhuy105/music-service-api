package com.tranhuy105.musicserviceapi.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateAlbumRequestDto {
    @NotNull(message = "Title cannot be null")
    @NotEmpty
    @Size(min = 1, max = 100, message = "Title must be between 1 and 100 characters")
    private String title;

    @NotNull(message = "ReleaseDate cannot be null")
    @Past
    private LocalDate releaseDate;

    @NotNull(message = "isSingle cannot be null")
    private Boolean isSingle;

    @Size(max = 255, message = "Cover URL must be less than or equal to 255 characters")
    private String coverUrl;
}
