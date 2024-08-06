package com.tranhuy105.musicserviceapi.repository.api;

import com.tranhuy105.musicserviceapi.dto.TrackQueueDto;
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

    void addPlaylist(@NonNull Playlist playlist);

    void updatePlaylist(@NonNull Long id, @NonNull Playlist playlist);

    void insertTrackToEnd(@NonNull Long playlistId, @NonNull Long trackId, @NonNull Long addedBy);

    void deleteTrack(@NonNull Long playlistId, @NonNull Long trackId);

    void moveTrack(@NonNull Long playlistId, @NonNull Long trackId, @NonNull Long newPosition);

    boolean trackExistsInPlaylist(@NonNull Long playlistId, @NonNull Long trackId);

}
