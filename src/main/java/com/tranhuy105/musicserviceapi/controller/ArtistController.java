package com.tranhuy105.musicserviceapi.controller;


import com.tranhuy105.musicserviceapi.model.*;
import com.tranhuy105.musicserviceapi.service.MetadataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/artists")
@RequiredArgsConstructor
public class ArtistController {
    private final MetadataService metadataService;

    @GetMapping("/search")
    public ResponseEntity<Page<Artist>> findAllArtist(
            @RequestParam(value = "q") String search,
            @RequestParam(value = "page", required = false) Integer page
    ) {
        return ResponseEntity.ok(metadataService.searchArtist(page, search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArtistProfile> findArtistById(@PathVariable Long id) {
        return ResponseEntity.ok(metadataService.findArtistProfileById(id));
    }

}
