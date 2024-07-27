package com.tranhuy105.musicserviceapi.controller;

import com.tranhuy105.musicserviceapi.dto.AlbumDto;
import com.tranhuy105.musicserviceapi.model.Album;
import com.tranhuy105.musicserviceapi.model.AlbumDetail;
import com.tranhuy105.musicserviceapi.model.Page;
import com.tranhuy105.musicserviceapi.model.QueryOptions;
import com.tranhuy105.musicserviceapi.repository.api.MetadataRepository;
import com.tranhuy105.musicserviceapi.service.MetadataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/albums")
@RequiredArgsConstructor
public class AlbumController {
    private final MetadataRepository metadataRepository;
    private final MetadataService metadataService;

    @GetMapping
    public ResponseEntity<Page<Album>> findAllAlbum(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", required = false) Integer page
    ) {
        return ResponseEntity.ok(metadataRepository.findAllAlbum(
                QueryOptions.of(page != null ? page : 1,10).search(search).build()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlbumDto> findAlbumById(@PathVariable Long id) {
        return ResponseEntity.ok(metadataService.findAlbumById(id));
    }
}
