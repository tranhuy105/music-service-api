package com.tranhuy105.musicserviceapi.controller;

import com.tranhuy105.musicserviceapi.dto.PlaylistDto;
import com.tranhuy105.musicserviceapi.model.Page;
import com.tranhuy105.musicserviceapi.model.Playlist;
import com.tranhuy105.musicserviceapi.model.PlaylistTrack;
import com.tranhuy105.musicserviceapi.service.PlaylistService;
import com.tranhuy105.musicserviceapi.utils.Util;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

    @PostMapping
    public ResponseEntity<Void> createPlaylist(@RequestBody @Valid PlaylistDto playlistDto,
                                               Authentication authentication) {
        playlistService.createPlaylist(playlistDto, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updatePlaylist(@PathVariable Long id,
                                               @RequestBody @Valid PlaylistDto updatePlaylistDto,
                                               Authentication authentication) {
        playlistService.updatePlaylist(id, updatePlaylistDto, authentication);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{playlistId}/tracks")
    public ResponseEntity<Void> addTrackToEnd(@PathVariable Long playlistId,
                                              @RequestParam("track_id") Long trackId,
                                              Authentication authentication) {
        playlistService.insertTrackToEnd(playlistId, trackId, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{playlistId}/tracks/{trackId}")
    public ResponseEntity<Void> deleteTrack(@PathVariable Long playlistId,
                                            @PathVariable Long trackId,
                                            Authentication authentication) {
        playlistService.deleteTrack(playlistId, trackId, authentication);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{playlistId}/tracks/{trackId}/move")
    public ResponseEntity<Void> moveTrack(@PathVariable Long playlistId,
                                          @PathVariable Long trackId,
                                          @RequestParam("new_position") Long newPosition,
                                          Authentication authentication) {
        playlistService.moveTrack(playlistId, trackId, newPosition, authentication);
        return ResponseEntity.ok().build();
    }
}
