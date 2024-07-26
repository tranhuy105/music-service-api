package com.tranhuy105.musicserviceapi.model;

import com.tranhuy105.musicserviceapi.model.ref.TrackAlbum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Track {
    private Long id;
    private String title;
    private Integer duration;
    private TrackAlbum album;
}
