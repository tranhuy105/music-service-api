package com.tranhuy105.musicserviceapi.model.ref;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ArtistAlbum {
    private String id;
    private String title;
    private LocalDate releaseDate;
    private Boolean isSingle;
    private String coverUrl;
    private String role;
}
