package com.tranhuy105.musicserviceapi.model.ref;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrackAlbum {
    private Long id;
    private String title;
    private String coverUrl;
    private List<AlbumArtist> artists;
}
