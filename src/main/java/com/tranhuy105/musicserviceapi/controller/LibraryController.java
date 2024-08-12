package com.tranhuy105.musicserviceapi.controller;

import com.tranhuy105.musicserviceapi.model.Artist;
import com.tranhuy105.musicserviceapi.model.Page;
import com.tranhuy105.musicserviceapi.model.PlaylistTrack;
import com.tranhuy105.musicserviceapi.service.LibraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class LibraryController {
    private final LibraryService libraryService;

    @GetMapping("/following")
    public ResponseEntity<Page<Artist>> getFollowingArtistList(Authentication authentication,
                                                               @RequestParam(value = "page", required = false) Integer page){
        return ResponseEntity.ok().body(libraryService.findMyFollowedArtist(authentication, page));
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
    public ResponseEntity<List<Boolean>> checkIfUserFollow(Authentication authentication,
                                                           @RequestParam("artist_ids") List<Long> artistId){
        return ResponseEntity.ok().body(libraryService.areFollowingArtist(authentication, artistId));
    }

    @GetMapping("/tracks")
    public ResponseEntity<Page<PlaylistTrack>> getSavedTrack(Authentication authentication,
                                                             @RequestParam(value = "page", required = false) Integer page){
        return ResponseEntity.ok().body(libraryService.findMySavedTrack(authentication, page));
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
    public ResponseEntity<List<Boolean>> checkIfUserSavedMultiple(Authentication authentication,
                                                                  @RequestParam("track_ids") List<Long> trackIds) {
        return ResponseEntity.ok().body(libraryService.areTracksSaved(authentication, trackIds));
    }
}
