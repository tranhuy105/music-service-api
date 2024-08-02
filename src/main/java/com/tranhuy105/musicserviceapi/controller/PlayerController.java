package com.tranhuy105.musicserviceapi.controller;

import com.tranhuy105.musicserviceapi.model.PlaybackMode;
import com.tranhuy105.musicserviceapi.model.StreamingHistory;
import com.tranhuy105.musicserviceapi.model.StreamingSession;
import com.tranhuy105.musicserviceapi.model.User;
import com.tranhuy105.musicserviceapi.service.PlayerService;
import com.tranhuy105.musicserviceapi.service.StorageService;
import com.tranhuy105.musicserviceapi.utils.Util;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@RestController
@RequestMapping("/api/me/player")
@RequiredArgsConstructor
public class PlayerController {
    private final StorageService storageService;
    private final PlayerService playerService;

    @GetMapping("/play/{trackId}")
    public ResponseEntity<String> playTrack(Authentication authentication,
                         @PathVariable Long trackId,
                         @RequestParam(value = "playlist", required = false) Long playlistId,
                         @RequestParam(value = "mode", required = false)
                                                @Valid
                                                @Pattern(regexp = "shuffle|repeat|sequential",
                                                        message = "play mode must be either 'shuffle' or 'repeat' or 'sequential'")
                                                String playmode,
                         HttpServletRequest request
    ) throws FileNotFoundException {
        User user = getUser(authentication);
        String deviceId = request.getHeader("User-Agent");
//      URL url = storageService.generatePresignedUrl(trackId.toString());
        playerService.playTrack(
                deviceId,
                user,
                trackId,
                playlistId,
                PlaybackMode.valueOf(playmode.toUpperCase())
        );
        return ResponseEntity.ok("url");
    }

    @PutMapping("/resume")
    public void resumeSession(Authentication authentication,
                              HttpServletRequest request) {
        User user = getUser(authentication);
        String deviceId = request.getHeader("User-Agent");
        playerService.resumeSession(deviceId, user);
    }

    @PutMapping("/pause")
    public void pauseSession(Authentication authentication,
                             HttpServletRequest request) {
        User user = getUser(authentication);
        String deviceId = request.getHeader("User-Agent");
        playerService.pauseSession(deviceId, user);
    }

    @GetMapping("/queue")
    public ResponseEntity<Queue<Long>> getQueue(
            Authentication authentication,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(new LinkedList<>());
    }

    @PostMapping("/queue")
    public void addItemToQueue(Authentication authentication,
                               HttpServletRequest request) {

    }

    @PostMapping("/next")
    public ResponseEntity<Long> next(Authentication authentication,
                     HttpServletRequest request) {
        User user = getUser(authentication);
        String deviceId = request.getHeader("User-Agent");
        Long nextTrackId = playerService.nextTrack(deviceId, user);
        return ResponseEntity.ok(nextTrackId);
    }

    @PostMapping("/previous")
    public ResponseEntity<Long> previous(Authentication authentication,
                         HttpServletRequest request) {
        User user = getUser(authentication);
        String deviceId = request.getHeader("User-Agent");
        Long preTrackId = playerService.prevTrack(deviceId, user);
        return ResponseEntity.ok(preTrackId);
    }

    @PutMapping("/repeat")
    public void setRepeatMode(Authentication authentication,
                              HttpServletRequest request) {
        User user = getUser(authentication);
        String deviceId = request.getHeader("User-Agent");
        playerService.changePlaybackMode(deviceId, user, PlaybackMode.REPEAT);
    }

    @PutMapping("/shuffle")
    public void setShuffleMode(Authentication authentication,
                               HttpServletRequest request) {
        User user = getUser(authentication);
        String deviceId = request.getHeader("User-Agent");
        playerService.changePlaybackMode(deviceId, user, PlaybackMode.SHUFFLE);
    }

    @PutMapping("/sequential")
    public void setSequenceMode(Authentication authentication,
                                HttpServletRequest request) {
        User user = getUser(authentication);
        String deviceId = request.getHeader("User-Agent");
        playerService.changePlaybackMode(deviceId, user, PlaybackMode.SEQUENTIAL);
    }


    @GetMapping
    public ResponseEntity<StreamingSession> getStreamingSession(Authentication authentication) {
        return ResponseEntity.ok(playerService.getStreamingSession(getUser(authentication)));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<StreamingHistory>> getStreamingHistory(Authentication authentication) {
        return ResponseEntity.ok(playerService.getSessionHistory(getUser(authentication)));
    }

    private User getUser(Authentication authentication) {
        return Util.extractUserFromAuthentication(authentication);
    }
}
