package com.tranhuy105.musicserviceapi.controller;

import com.tranhuy105.musicserviceapi.model.Page;
import com.tranhuy105.musicserviceapi.model.Playlist;
import com.tranhuy105.musicserviceapi.model.PlaylistTrack;
import com.tranhuy105.musicserviceapi.model.QueryOptions;
import com.tranhuy105.musicserviceapi.repository.api.PlaylistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/playlists")
public class PlaylistController {
    private final PlaylistRepository playlistRepository;

    public ResponseEntity<Page<Playlist>> findAllPlaylist() {
        return null;
    }

    @RequestMapping("/{id}/tracks")
    public ResponseEntity<Page<PlaylistTrack>> findAllPlaylistTracks(@PathVariable Long id) {
        return ResponseEntity.ok(playlistRepository.findPlaylistTracksById(id,
                QueryOptions.of(1,10).sortBy("position").asc().build()
        ));
    }

    @RequestMapping("/{id}")
    public ResponseEntity<Playlist> findPlaylistById(@PathVariable Long id) {
        return ResponseEntity.ok(
                playlistRepository.findPlaylistById(id).orElseThrow()
        );
    }
}
