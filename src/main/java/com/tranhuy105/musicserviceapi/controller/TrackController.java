package com.tranhuy105.musicserviceapi.controller;

import com.tranhuy105.musicserviceapi.dto.CreateTrackRequestDto;
import com.tranhuy105.musicserviceapi.model.Page;
import com.tranhuy105.musicserviceapi.model.TrackDetail;
import com.tranhuy105.musicserviceapi.service.TrackService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ARTIST')")
    @ResponseStatus(HttpStatus.CREATED)
    public void uploadTrack(Authentication authentication,
                            @RequestParam("album_id") Long albumId,
                            @RequestParam("title") @NotEmpty String title,
                            @RequestParam("file")MultipartFile file) throws IOException {
        trackService.uploadNewTrack(
                new CreateTrackRequestDto(albumId, title, null),
                file,
                authentication
        );
    }
}
