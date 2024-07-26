package com.tranhuy105.musicserviceapi.model;

import com.tranhuy105.musicserviceapi.model.ref.AlbumArtist;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Album {
    private Long id;
    private String title;
    private Boolean isSingle;
    private String coverUrl;
    private LocalDate releaseDate;
    private List<Track> tracks;
    private List<AlbumArtist> artists;
}

