package com.tranhuy105.musicserviceapi.controller;

import com.tranhuy105.musicserviceapi.model.Artist;
import com.tranhuy105.musicserviceapi.model.Genre;
import com.tranhuy105.musicserviceapi.model.Page;
import com.tranhuy105.musicserviceapi.model.Playlist;
import com.tranhuy105.musicserviceapi.service.ArtistService;
import com.tranhuy105.musicserviceapi.service.GenreService;
import com.tranhuy105.musicserviceapi.service.PlaylistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/browse")
public class BrowseController {
    private final ArtistService artistService;
    private final PlaylistService playlistService;
    private final GenreService genreService;

    @GetMapping("/genres")
    public ResponseEntity<List<Genre>> getSeveralGenres() {
        return ResponseEntity.ok(genreService.getAllGenre());
    }

    @GetMapping("/genres/{genre_id}")
    public ResponseEntity<Genre> getSingleGenre(@PathVariable Long genre_id) {
        return ResponseEntity.ok(genreService.getGenre(genre_id));
    }

    @GetMapping("/genres/{genre_id}/playlists")
    public ResponseEntity<List<Playlist>> getGenrePlaylist(@PathVariable Long genre_id) {
        return ResponseEntity.ok(playlistService.findSystemGeneratedPlaylistByGenre(genre_id));
    }

    @GetMapping("/genres/{genre_id}/artists")
    public ResponseEntity<Page<Artist>> getGenreArtist(@PathVariable Long genre_id,
                                                       @RequestParam(value = "page", required = false) Integer page,
                                                       @RequestParam(value = "sort_by", required = false) String sort_by) {
        Page<Artist> res = !"top".equals(sort_by)
                ? artistService.browseNewArtistByGenre(genre_id, page)
                : artistService.browseTopArtistByGenre(genre_id, page);
        return ResponseEntity.ok(res);
    }
}
