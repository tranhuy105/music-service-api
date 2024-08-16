package com.tranhuy105.musicserviceapi.service;

import com.tranhuy105.musicserviceapi.dto.CreateTrackRequestDto;
import com.tranhuy105.musicserviceapi.exception.ObjectNotFoundException;
import com.tranhuy105.musicserviceapi.model.*;
import com.tranhuy105.musicserviceapi.repository.api.AlbumRepository;
import com.tranhuy105.musicserviceapi.repository.api.ArtistRepository;
import com.tranhuy105.musicserviceapi.repository.api.TrackRepository;
import com.tranhuy105.musicserviceapi.utils.CachePrefix;
import com.tranhuy105.musicserviceapi.utils.FileUtil;
import com.tranhuy105.musicserviceapi.utils.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
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

        File originalFile = FileUtil.convertMultipartFileToFile(multipartFile);
        File lowQualityFile = null;
        String highQualityKey = null;

        try {
            int duration = 0;
            try {
                duration = FileUtil.getAudioDuration(originalFile);
            } catch (Exception exception) {
                log.error("Fail to extract track duration: ", exception);
            }
            lowQualityFile = FileUtil.reduceAudioQuality(originalFile);
            dto.setDuration(duration);
            Long trackId = trackRepository.insert(dto);
            highQualityKey = storageService.uploadMediaItem(originalFile, trackId.toString(), "track");
            storageService.uploadMediaItem(lowQualityFile, trackId.toString(), "track_low");
        } catch (Exception e) {
            log.error("Error processing track upload", e);
            if (highQualityKey != null) {
                try {
                    storageService.deleteMediaItem(highQualityKey);
                } catch (Exception exception) {
                    log.error("Fail to delete stale file on storage: ", exception);
                }
            }
            throw new RuntimeException("Failed to upload track", e);
        } finally {
            FileUtil.cleanupFile(originalFile);
            if (lowQualityFile != null) {
                FileUtil.cleanupFile(lowQualityFile);
            }
        }
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
}