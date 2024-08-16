package com.tranhuy105.musicserviceapi.controller;

import com.tranhuy105.musicserviceapi.model.ArtistRequest;
import com.tranhuy105.musicserviceapi.model.Page;
import com.tranhuy105.musicserviceapi.service.*;
import com.tranhuy105.musicserviceapi.utils.Util;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')")
@RequiredArgsConstructor
public class AdminController {
    private final SystemService service;
    private final CacheService cacheService;
    private final S3Service s3Service;
    private final UserService userService;
    private final ArtistRequestService artistRequestService;

    @PostMapping("/similar-artist")
    public ResponseEntity<String> precomputeSimilarArtist() {
        return executeAndMeasureTime(service::computeAndGenerateArtistSimilarity, "Successfully computed and generated artist similarity.");
    }

    @PostMapping("/generate-genre-playlist")
    public ResponseEntity<String> generateSystemPlaylistSeedByGenre() {
        return executeAndMeasureTime(service::generateSystemPlaylists, "Successfully generated playlist seed by genre.");
    }

    @PutMapping("/users/{userId}/roles")
    public ResponseEntity<String> assignRolesToUser(
            @PathVariable Long userId,
            @RequestParam List<Long> roleIds
    ) {
        userService.assignRolesToUser(userId, roleIds);
        return ResponseEntity.ok("Roles successfully assigned.");
    }

    @DeleteMapping("/users/{userId}/roles")
    public ResponseEntity<String> deleteRolesFromUser(
            @PathVariable Long userId,
            @RequestParam List<Long> roleIds
    ) {
        userService.revokeRolesFromUser(userId, roleIds);
        return ResponseEntity.ok("Roles revoked successfully.");
    }

    @PostMapping("/tracks/{id}")
    public ResponseEntity<String> fixBrokenAudioLink(@PathVariable Long id,
                                       @RequestParam(value = "type") String type,
                                       @RequestParam(value = "file", required = false) MultipartFile file) {
        return executeAndMeasureTime(() -> {
            try {
                s3Service.uploadMediaItem(convertMultipartFileToFile(file), String.valueOf(id), type);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, String.format("Successfully Upload Media File For Track %s.\nFile: \"%s\", Size: %s.",
                id,
                file.getOriginalFilename(),
                file.getSize())
        );
    }

    @PostMapping("/cache-evict")
    public ResponseEntity<String> evictAllCache() {
        return executeAndMeasureTime(cacheService::evictAllCache, "Evict All Redis Cache.");
    }

    @GetMapping("/artist-requests")
    public ResponseEntity<Page<ArtistRequest>> getPendingArtistRequests(
            @RequestParam(value = "page", required = false) Integer page
    ) {
        return ResponseEntity.ok(artistRequestService.findPendingRequests(page));
    }

    @GetMapping("/artist-requests/{id}")
    public ResponseEntity<ArtistRequest> getPendingArtistRequests(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(artistRequestService.findById(id));
    }

    @PostMapping("/artist-requests/{id}")
    public ResponseEntity<String> reviewRequest(
            @PathVariable Long id,
            @RequestBody Map<String, Object> requestBody,
            Authentication authentication
    ) {
        Long adminId = Util.extractUserIdFromAuthentication(authentication);
        String status = (String) requestBody.get("status");
        String reason = (String) requestBody.get("reason");

        if (status == null) {
            return ResponseEntity.badRequest().body("status: can not be null.");
        }

        artistRequestService.reviewRequest(id, adminId, status, reason);
        return ResponseEntity.ok("OK");
    }


    private File convertMultipartFileToFile(MultipartFile multipartFile) throws IOException {
        File file = File.createTempFile("temp", multipartFile.getOriginalFilename());

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(multipartFile.getBytes());
        }

        return file;
    }

    private ResponseEntity<String> executeAndMeasureTime(Runnable action, String successMessage) {
        Instant start = Instant.now();
        try {
            action.run();
        } catch (Exception exception) {
            return ResponseEntity.badRequest().body("Unexpected Exception: " + exception.getMessage());
        }

        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);
        long seconds = duration.getSeconds();
        long millis = duration.toMillisPart();
        String responseMessage = String.format(
                "%s\nTime executed: %d seconds and %d milliseconds.",
                successMessage, seconds, millis
        );
        return ResponseEntity.ok(responseMessage);
    }
}
