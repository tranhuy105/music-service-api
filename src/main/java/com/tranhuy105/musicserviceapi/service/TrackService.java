package com.tranhuy105.musicserviceapi.service;

import com.tranhuy105.musicserviceapi.dto.CreateTrackRequestDto;
import com.tranhuy105.musicserviceapi.exception.ObjectNotFoundException;
import com.tranhuy105.musicserviceapi.model.*;
import com.tranhuy105.musicserviceapi.repository.api.AlbumRepository;
import com.tranhuy105.musicserviceapi.repository.api.ArtistRepository;
import com.tranhuy105.musicserviceapi.repository.api.TrackRepository;
import com.tranhuy105.musicserviceapi.utils.CachePrefix;
import com.tranhuy105.musicserviceapi.utils.Util;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class TrackService {
    private final TrackRepository trackRepository;
    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final CacheService cacheService;
    private final StorageService storageService;
    private static final int SEARCH_PAGE_SIZE = 20;

    public Page<TrackDetail> searchTrack(Integer page, String searchQuery) {
        return trackRepository.findAllTrack(
                QueryOptions.of(page != null ? page : 1,SEARCH_PAGE_SIZE).search(searchQuery).build()
        );
    }

    public TrackDetail findTrackById(Long id) {
        String cacheKey = cacheService.getCacheKey(CachePrefix.TRACK, id);
        return cacheService.cacheOrFetch(cacheKey, () ->
                trackRepository.findTrackById(id).orElseThrow(
                        () -> new ObjectNotFoundException("track", id.toString())
                ));
    }

    @Transactional
    public void uploadNewTrack(CreateTrackRequestDto dto,
                               MultipartFile multipartFile,
                               Authentication authentication) throws IOException {
        artistValidator(dto.getAlbumId(), authentication);
        File file = convertMultipartFileToFile(multipartFile);
        dto.setDuration(extractTrackDuration(file));
        Long trackId = trackRepository.insert(dto);
        storageService.uploadTrack(file, trackId.toString());
    }

    public int extractTrackDuration(File file) {
        return 0;
    }

    private void artistValidator(Long albumId, Authentication authentication) {
        Artist artist = checkArtistProfile(authentication);
        AlbumDetail albumDetail = albumRepository.findAlbumDetailById(albumId)
                .orElseThrow(() -> new ObjectNotFoundException("album", albumId.toString()));

        if (albumDetail.getArtists().stream().noneMatch(
                albumArtist -> albumArtist.getId().equals(artist.getId())
        )) {
            throw new AccessDeniedException("This artist is not authorized to perform action with this album");
        }
    }

    private Artist checkArtistProfile(Authentication authentication)  {
        Long userId = Util.extractUserIdFromAuthentication(authentication);

        return artistRepository.findArtistByUserId(userId).orElseThrow(
                () -> new AccessDeniedException("This artist does not associated with any artist profile!")
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