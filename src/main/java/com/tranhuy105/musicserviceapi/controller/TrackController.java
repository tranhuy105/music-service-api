package com.tranhuy105.musicserviceapi.controller;

import com.tranhuy105.musicserviceapi.dto.CreateTrackRequestDto;
import com.tranhuy105.musicserviceapi.dto.UpdateTrackRequestDto;
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
    public ResponseEntity<Void> uploadTrack(Authentication authentication,
                            @RequestParam("album_id") Long albumId,
                            @RequestParam("title") @NotEmpty String title,
                            @RequestParam("file")MultipartFile file) throws IOException {
        trackService.uploadNewTrack(
                new CreateTrackRequestDto(albumId, title, null),
                file,
                authentication
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ARTIST')")
    public ResponseEntity<Void> updateTrack(@PathVariable Long id,
                                            @RequestBody UpdateTrackRequestDto dto,
                                            Authentication authentication) {
        trackService.updateTrack(id, dto, authentication);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/file")
    public ResponseEntity<Void> updateTrackFile(@PathVariable Long id,
                                                @RequestParam("file")MultipartFile file,
                                                Authentication authentication) throws IOException {
        trackService.updateTrackFile(id, file, authentication);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ARTIST')")
    public ResponseEntity<Void> deleteTrack(@PathVariable Long id, Authentication authentication) {
        trackService.deleteTrack(id, authentication);
        return ResponseEntity.noContent().build();
    }
}
