package com.tranhuy105.musicserviceapi.controller;


import com.tranhuy105.musicserviceapi.dto.CreateArtistProfileRequestDto;
import com.tranhuy105.musicserviceapi.model.*;
import com.tranhuy105.musicserviceapi.service.ArtistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/artists")
@RequiredArgsConstructor
public class ArtistController {
    private final ArtistService artistService;

    @GetMapping("/search")
    public ResponseEntity<Page<Artist>> findAllArtist(
            @RequestParam(value = "q") String search,
            @RequestParam(value = "page", required = false) Integer page
    ) {
        return ResponseEntity.ok(artistService.searchArtist(page, search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArtistProfile> findArtistById(@PathVariable Long id) {
        return ResponseEntity.ok(artistService.findArtistProfileById(id));
    }

    @GetMapping("/{id}/related")
    public ResponseEntity<List<Artist>> findRelatedArtistById(@PathVariable Long id,
                                                             @RequestParam(value = "limit", required = false) Integer limit) {
        return ResponseEntity.ok(artistService.findRelatedArtist(id, limit));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_ADMIN') || hasRole('ROLE_MANAGER')")
    public void createArtistProfile(@RequestBody @Valid CreateArtistProfileRequestDto dto) {
        artistService.createArtistProfile(dto);
    }
}
