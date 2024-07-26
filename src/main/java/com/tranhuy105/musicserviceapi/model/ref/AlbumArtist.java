package com.tranhuy105.musicserviceapi.model.ref;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AlbumArtist {
    private Long id;
    private String stageName;
    private String profilePictureUrl;
    private String role;
}
