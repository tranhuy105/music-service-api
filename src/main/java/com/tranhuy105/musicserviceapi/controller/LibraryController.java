package com.tranhuy105.musicserviceapi.controller;

import com.tranhuy105.musicserviceapi.model.Artist;
import com.tranhuy105.musicserviceapi.model.Page;
import com.tranhuy105.musicserviceapi.model.PlaylistTrack;
import com.tranhuy105.musicserviceapi.service.LibraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class LibraryController {
    private final LibraryService libraryService;

    @GetMapping("/following")
    public ResponseEntity<Page<Artist>> getFollowingArtistList(Authentication authentication){
        return ResponseEntity.ok().body(libraryService.findMyFollowedArtist(authentication, null));
    }

    @DeleteMapping("/following")
    public ResponseEntity<Void> unfollowArtist(Authentication authentication, @RequestParam("artist_id") Long artistId){
        libraryService.unfollowArtist(authentication, artistId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/following")
    public ResponseEntity<Void> followArtist(Authentication authentication, @RequestParam("artist_id") Long artistId){
        libraryService.followArtist(authentication, artistId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/following/contains")
    public ResponseEntity<Boolean> checkIfUserFollow(Authentication authentication, @RequestParam("artist_id") Long artistId){
        return ResponseEntity.ok().body(libraryService.isFollowingArtist(authentication, artistId));
    }

    @GetMapping("/tracks")
    public ResponseEntity<Page<PlaylistTrack>> getSavedTrack(Authentication authentication){
        return ResponseEntity.ok().body(libraryService.findMySavedTrack(authentication, null));
    }

    @PutMapping("/tracks")
    public ResponseEntity<Void> likeTrack(Authentication authentication, @RequestParam("track_id") Long trackId){
        libraryService.addSavedTrack(authentication, trackId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/tracks")
    public ResponseEntity<Void> unlikeTrack(Authentication authentication, @RequestParam("track_id") Long trackId){
        libraryService.removeSavedTrack(authentication, trackId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/tracks/contains")
    public ResponseEntity<Boolean> checkIfUserSaved(Authentication authentication, @RequestParam("track_id") Long trackId){
        return ResponseEntity.ok().body(libraryService.isTrackSaved(authentication, trackId));
    }
}
