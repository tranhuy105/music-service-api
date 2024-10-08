package com.tranhuy105.musicserviceapi.model;

import com.tranhuy105.musicserviceapi.model.ref.ArtistAlbum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ArtistProfile extends Artist{
    private List<ArtistAlbum> albums;
}
