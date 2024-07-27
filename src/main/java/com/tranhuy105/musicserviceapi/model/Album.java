package com.tranhuy105.musicserviceapi.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

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
}
