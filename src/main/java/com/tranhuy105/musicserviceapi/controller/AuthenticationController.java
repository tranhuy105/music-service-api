package com.tranhuy105.musicserviceapi.controller;

import com.tranhuy105.musicserviceapi.dto.AuthenticationResponseDto;
import com.tranhuy105.musicserviceapi.dto.AuthenticationRequestDto;
import com.tranhuy105.musicserviceapi.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<?> register(
            @RequestBody @Valid AuthenticationRequestDto request
    ) {
        authenticationService.register(request);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponseDto> login(
            @RequestBody @Valid AuthenticationRequestDto request
    ) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @PostMapping("/validate-token")
    public ResponseEntity<String> validateToken() {
        return ResponseEntity.ok().body("OK");
    }
}
