package com.tranhuy105.musicserviceapi.controller;

import com.tranhuy105.musicserviceapi.model.Page;
import com.tranhuy105.musicserviceapi.model.Playlist;
import com.tranhuy105.musicserviceapi.model.PlaylistTrack;
import com.tranhuy105.musicserviceapi.service.PlaylistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/playlists")
public class PlaylistController {
    private final PlaylistService playlistService;

    @GetMapping("/{id}/tracks")
    public ResponseEntity<Page<PlaylistTrack>> findAllPlaylistTracks(
            @PathVariable Long id,
            @RequestParam(value = "page", required = false) Integer page) {
        return ResponseEntity.ok(playlistService.findPlaylistTracks(id, page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Playlist> findPlaylistById(@PathVariable Long id) {
        return ResponseEntity.ok(
                playlistService.findPlaylistById(id, 1)
        );
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Playlist>> searchPlaylist(
            @RequestParam(value = "q") String searchQuery,
            @RequestParam(value = "page", required = false) Integer page
    ) {
        return ResponseEntity.ok(playlistService.searchPlaylist(page, searchQuery));
    }
}
