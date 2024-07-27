package com.tranhuy105.musicserviceapi.controller;


import com.tranhuy105.musicserviceapi.model.*;
import com.tranhuy105.musicserviceapi.repository.api.MetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/artists")
@RequiredArgsConstructor
public class ArtistController {
    private final MetadataRepository metadataRepository;

    @GetMapping
    public ResponseEntity<Page<Artist>> findAllArtist(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", required = false) Integer page
    ) {
        return ResponseEntity.ok(metadataRepository.findAllArtist(
                QueryOptions.of(page != null ? page : 1,10).search(search).build()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArtistProfile> findArtistById(@PathVariable Long id) {
        return ResponseEntity.ok(metadataRepository.findArtistProfileById(id).orElseThrow());
    }

}
