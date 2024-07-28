package com.tranhuy105.musicserviceapi.repository.api;

import com.tranhuy105.musicserviceapi.model.Page;
import com.tranhuy105.musicserviceapi.model.Playlist;
import com.tranhuy105.musicserviceapi.model.PlaylistTrack;
import com.tranhuy105.musicserviceapi.model.QueryOptions;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface PlaylistRepository {
    Page<PlaylistTrack> findPlaylistTracksById(@NonNull Long id, @NonNull QueryOptions queryOptions);

    Optional<Playlist> findPlaylistById(@NonNull Long id);

    Page<Playlist> findAllPlaylist(@NonNull QueryOptions queryOptions);
}
