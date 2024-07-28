package com.tranhuy105.musicserviceapi.controller;

import com.tranhuy105.musicserviceapi.dto.AlbumDto;
import com.tranhuy105.musicserviceapi.model.*;
import com.tranhuy105.musicserviceapi.service.MetadataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/albums")
@RequiredArgsConstructor
public class AlbumController {
    private final MetadataService metadataService;

    @GetMapping("/search")
    public ResponseEntity<Page<Album>> searchAlbum(
            @RequestParam(value = "q") String search,
            @RequestParam(value = "page", required = false) Integer page
    ) {
        return ResponseEntity.ok(metadataService.searchAlbum(page, search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlbumDto> findAlbumById(@PathVariable Long id) {
        return ResponseEntity.ok(metadataService.findAlbumById(id));
    }

    @GetMapping("/{id}/tracks")
    public ResponseEntity<List<TrackDetail>> findAlbumTracks(@PathVariable Long id) {
        return ResponseEntity.ok(metadataService.findAlbumTracks(id));
    }
}
