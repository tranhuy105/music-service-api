package com.tranhuy105.musicserviceapi.repository.api;

import com.tranhuy105.musicserviceapi.model.Playlist;
import com.tranhuy105.musicserviceapi.model.PlaylistTrack;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface PlaylistRepository {
    List<PlaylistTrack> findPlaylistTracksById(@NonNull Long id);

    Optional<Playlist> findPlaylistById(@NonNull Long id);
}
