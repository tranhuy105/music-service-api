package com.tranhuy105.musicserviceapi.controller;

import com.tranhuy105.musicserviceapi.dto.UserDto;
import com.tranhuy105.musicserviceapi.model.ArtistRequest;
import com.tranhuy105.musicserviceapi.model.User;
import com.tranhuy105.musicserviceapi.service.ArtistRequestService;
import com.tranhuy105.musicserviceapi.service.UserService;
import com.tranhuy105.musicserviceapi.utils.Util;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final ArtistRequestService artistRequestService;

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping("/request-artist")
    public ResponseEntity<String> requestArtistRole(
            @RequestBody @Valid ArtistRequest artistRequest,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        artistRequestService.createArtistRequest(
                user.getId(),
                artistRequest.getArtistName(),
                artistRequest.getGenre(),
                artistRequest.getPortfolioUrl(),
                artistRequest.getBio(),
                artistRequest.getSocialMediaLinks()
        );
        return ResponseEntity.ok("Your request to become an artist has been submitted.");
    }

    @PutMapping
    public ResponseEntity<String> updateUser(
            @RequestBody @Valid UserDto userDto,
            Authentication authentication
    ) {
        Long id = Util.extractUserIdFromAuthentication(authentication);
        userService.updateUser(id, userDto);
        return ResponseEntity.ok("User updated successfully.");
    }
}
