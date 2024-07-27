package com.tranhuy105.musicserviceapi.dto;

import com.tranhuy105.musicserviceapi.model.Playlist;
import com.tranhuy105.musicserviceapi.model.PlaylistTrack;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PlaylistTrackDto extends Playlist {
    List<PlaylistTrack> tracks;
}

