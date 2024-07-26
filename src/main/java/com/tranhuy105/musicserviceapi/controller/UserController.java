package com.tranhuy105.musicserviceapi.controller;

import com.tranhuy105.musicserviceapi.dto.UserDto;
import com.tranhuy105.musicserviceapi.model.User;
import com.tranhuy105.musicserviceapi.repository.api.MetadataRepository;
import com.tranhuy105.musicserviceapi.repository.api.PlaylistRepository;
import com.tranhuy105.musicserviceapi.service.UserService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final MetadataRepository metadataRepository;
    private final PlaylistRepository playlistRepository;

    @GetMapping
    public ResponseEntity<User> getAuthUserInfo(Authentication authentication) {
        return ResponseEntity.ok((User) authentication.getPrincipal());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/test/{id}")
    public ResponseEntity<?> testparam(@PathVariable Long id) {
        return ResponseEntity.ok(
                playlistRepository.findPlaylistById(id)
        );
    }

    @GetMapping("/test")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok(
                metadataRepository.findAllTrack()
        );
    }
}
