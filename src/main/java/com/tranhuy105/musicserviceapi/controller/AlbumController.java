package com.tranhuy105.musicserviceapi.controller;

import com.tranhuy105.musicserviceapi.dto.AlbumDto;
import com.tranhuy105.musicserviceapi.model.*;
import com.tranhuy105.musicserviceapi.service.AlbumService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/albums")
@RequiredArgsConstructor
public class AlbumController {
    private final AlbumService albumService;

    @GetMapping("/search")
    public ResponseEntity<Page<Album>> searchAlbum(
            @RequestParam(value = "q") String search,
            @RequestParam(value = "page", required = false) Integer page
    ) {
        return ResponseEntity.ok(albumService.searchAlbum(page, search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlbumDto> findAlbumById(@PathVariable Long id) {
        return ResponseEntity.ok(albumService.findAlbumById(id));
    }

    @GetMapping("/{id}/tracks")
    public ResponseEntity<List<TrackDetail>> findAlbumTracks(@PathVariable Long id) {
        return ResponseEntity.ok(albumService.findAlbumTracks(id));
    }
}
