package com.tranhuy105.musicserviceapi.controller;

import com.tranhuy105.musicserviceapi.model.Page;
import com.tranhuy105.musicserviceapi.model.TrackDetail;
import com.tranhuy105.musicserviceapi.service.TrackService;
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
    private final TrackService trackService;

    @RequestMapping("/search")
    public ResponseEntity<Page<TrackDetail>> getAllTrack(
            @RequestParam(value = "q") String search,
            @RequestParam(value = "page", required = false) Integer page
            ) {
        return ResponseEntity.ok(trackService.searchTrack(page, search));
    }

    @RequestMapping("/{id}")
    public ResponseEntity<TrackDetail> getTrackById(@PathVariable Long id) {
        return ResponseEntity.ok(trackService.findTrackById(id));
    }
}
