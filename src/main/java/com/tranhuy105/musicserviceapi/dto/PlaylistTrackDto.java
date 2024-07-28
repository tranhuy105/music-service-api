package com.tranhuy105.musicserviceapi.dto;

import com.tranhuy105.musicserviceapi.model.Page;
import com.tranhuy105.musicserviceapi.model.Playlist;
import com.tranhuy105.musicserviceapi.model.PlaylistTrack;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PlaylistTrackDto extends Playlist {
    Page<PlaylistTrack> tracks;

    public PlaylistTrackDto(Playlist playlist, Page<PlaylistTrack> tracks) {
        super.setId(playlist.getId());
        super.setUserId(playlist.getUserId());
        super.setName(playlist.getName());
        super.setDescription(playlist.getDescription());
        super.setIsPublic(playlist.getIsPublic());
        super.setCoverUrl(playlist.getCoverUrl());
        super.setTotalTrack(playlist.getTotalTrack());
        this.tracks = tracks;

    }
}

