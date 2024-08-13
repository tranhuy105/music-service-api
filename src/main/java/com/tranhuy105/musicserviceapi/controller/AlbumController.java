package com.tranhuy105.musicserviceapi.controller;

import com.tranhuy105.musicserviceapi.dto.AlbumDto;
import com.tranhuy105.musicserviceapi.dto.CreateAlbumRequestDto;
import com.tranhuy105.musicserviceapi.dto.AlbumArtistCRUDRequestDto;
import com.tranhuy105.musicserviceapi.model.*;
import com.tranhuy105.musicserviceapi.service.AlbumService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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

    @GetMapping("/{id}/related")
    public ResponseEntity<List<Album>> findRelatedAlbumById(@PathVariable Long id,
                                                         @RequestParam(value = "limit", required = false) Integer limit) {
        return ResponseEntity.ok(albumService.findRelatedAlbum(id, limit));
    }

    @GetMapping("/{id}/tracks")
    public ResponseEntity<List<TrackDetail>> findAlbumTracks(@PathVariable Long id) {
        return ResponseEntity.ok(albumService.findAlbumTracks(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ARTIST')")
    @ResponseStatus(HttpStatus.CREATED)
    public void createNewAlbum(@RequestBody @Valid CreateAlbumRequestDto dto,
                               Authentication authentication) {
        albumService.createAlbum(dto, authentication);
    }

    @PostMapping("/{id}/artists")
    @PreAuthorize("hasRole('ROLE_ARTIST')")
    public void linkNewArtist(@PathVariable Long id,
                              @RequestBody @Valid AlbumArtistCRUDRequestDto dto,
                              Authentication authentication) {
        dto.setAlbumId(id);
        albumService.addAlbumArtist(dto, authentication);
    }

    @PutMapping("/{id}/artists")
    @PreAuthorize("hasRole('ROLE_ARTIST')")
    public void updateLinkedArtist(@PathVariable Long id,
                                   @RequestBody @Valid AlbumArtistCRUDRequestDto dto,
                                   Authentication authentication) {
        dto.setAlbumId(id);
        albumService.updateAlbumArtist(dto, authentication);
    }

    @DeleteMapping("/{albumId}/artists/{artistId}")
    @PreAuthorize("hasRole('ROLE_ARTIST')")
    public void unlinkArtist(@PathVariable Long albumId,
                             @PathVariable Long artistId,
                             Authentication authentication) {
        AlbumArtistCRUDRequestDto dto = new AlbumArtistCRUDRequestDto();
        dto.setAlbumId(albumId);
        dto.setArtistId(artistId);
        albumService.removeAlbumArtist(dto, authentication);
    }
}
