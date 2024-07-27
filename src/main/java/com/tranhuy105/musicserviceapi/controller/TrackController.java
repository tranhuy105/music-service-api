package com.tranhuy105.musicserviceapi.controller;

import com.tranhuy105.musicserviceapi.model.Page;
import com.tranhuy105.musicserviceapi.model.QueryOptions;
import com.tranhuy105.musicserviceapi.model.TrackDetail;
import com.tranhuy105.musicserviceapi.repository.api.MetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tracks")
@RequiredArgsConstructor
public class TrackController {
    private final MetadataRepository metadataRepository;

    @RequestMapping
    public ResponseEntity<Page<TrackDetail>>getAllTrack(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", required = false) Integer page
            ) {
        return ResponseEntity.ok(metadataRepository.findAllTrack(
                QueryOptions.of(1, 10).search(search).build()
        ));
    }

    @RequestMapping("/{id}")
    public ResponseEntity<TrackDetail> getTrackById(@PathVariable Long id) {
        return ResponseEntity.ok(metadataRepository.findTrackById(id).orElseThrow());
    }
}