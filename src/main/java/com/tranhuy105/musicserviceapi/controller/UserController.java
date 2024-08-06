package com.tranhuy105.musicserviceapi.controller;

import com.tranhuy105.musicserviceapi.dto.UserDto;
import com.tranhuy105.musicserviceapi.model.User;
import com.tranhuy105.musicserviceapi.service.CacheService;
import com.tranhuy105.musicserviceapi.service.S3Service;
import com.tranhuy105.musicserviceapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final CacheService cacheService;
    private final S3Service s3Service;

    @GetMapping
    public ResponseEntity<User> getAuthUserInfo(Authentication authentication) {
        return ResponseEntity.ok((User) authentication.getPrincipal());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/test/{id}")
    public ResponseEntity<?> testparam(@PathVariable Long id,
                                       @RequestParam(value = "file", required = false)MultipartFile file) throws IOException {
        s3Service.uploadTrack(convertMultipartFileToFile(file), String.valueOf(id));
        return ResponseEntity.ok(
               "ok"
        );
    }

    @GetMapping("/test")
    public ResponseEntity<?> test() {
        cacheService.evictAllCache();
        return ResponseEntity.ok(
                "Evict All Cache"
        );
    }

    private File convertMultipartFileToFile(MultipartFile multipartFile) throws IOException {
        File file = File.createTempFile("temp", multipartFile.getOriginalFilename());

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(multipartFile.getBytes());
        }

        return file;
    }
}
