package com.tranhuy105.musicserviceapi.controller;

import com.tranhuy105.musicserviceapi.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileNotFoundException;
import java.net.URL;

@RestController
@RequestMapping("/api/me/player")
@RequiredArgsConstructor
public class PlayerController {
    private final StorageService storageService;

    @RequestMapping
    public URL playTrack(Authentication authentication, Long trackId) throws FileNotFoundException {
        return storageService.generatePresignedUrl(trackId.toString());
    }
}
