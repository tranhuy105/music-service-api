package com.tranhuy105.musicserviceapi.service;

import com.tranhuy105.musicserviceapi.dto.CreateTrackRequestDto;
import com.tranhuy105.musicserviceapi.dto.UpdateTrackRequestDto;
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
import org.springframework.lang.NonNull;
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

    public void deleteTrack(Long id, Authentication authentication) {
        Track track = trackRepository.findRawTrackById(id)
                .orElseThrow(() -> new ObjectNotFoundException("track", id.toString()));

        artistValidator(track.getAlbumId(), authentication);
        trackRepository.deleteTrack(id);

        boolean lowQualityDeleted = false;
        try {
            storageService.deleteMediaItem(String.format("%s/%s", getLowQualityKeyPrefix(), id));
            lowQualityDeleted = true;
        } catch (Exception e) {
            log.error("Failed to delete low-quality file for track ID: " + id, e);
        }

        try {
            storageService.deleteMediaItem(String.format("%s/%s", getHighQualityKeyPrefix(), id));
        } catch (Exception e) {
            if (lowQualityDeleted) {
                log.error("Low-quality file deleted but failed to delete high-quality file for track ID: " + id, e);
            } else {
                log.error("Failed to delete high-quality file for track ID: " + id, e);
            }
        }
    }

    public void updateTrack(Long id, UpdateTrackRequestDto dto, Authentication authentication) {
        Track track = trackRepository.findRawTrackById(id)
                .orElseThrow(() -> new ObjectNotFoundException("track", id.toString()));

        artistValidator(track.getAlbumId(), authentication);

        track.setTitle(dto.getTitle());
        trackRepository.updateTrack(track);
    }

    @Transactional
    public void uploadNewTrack(CreateTrackRequestDto dto,
                               MultipartFile multipartFile,
                               Authentication authentication) throws IOException {
        artistValidator(dto.getAlbumId(), authentication);

        File originalFile = FileUtil.convertMultipartFileToFile(multipartFile);
        File lowQualityFile = null;

        try {
            dto.setDuration(extractTrackDuration(originalFile));
            Long trackId = insertTrack(dto);
            lowQualityFile = FileUtil.reduceAudioQuality(originalFile);
            uploadFilesToS3(originalFile, lowQualityFile, trackId);
        } catch (Exception e) {
            log.error("Error processing track upload", e);
            throw new RuntimeException("Failed to upload track", e);
        } finally {
            FileUtil.cleanupFile(originalFile);
            if (lowQualityFile != null) {
                FileUtil.cleanupFile(lowQualityFile);
            }
        }
    }

    public void updateTrackFile(Long trackId, MultipartFile multipartFile, Authentication authentication) throws IOException {
        Track track = trackRepository.findRawTrackById(trackId).orElseThrow(
                () -> new ObjectNotFoundException("track", trackId.toString())
        );
        artistValidator(track.getAlbumId(), authentication);
        File originalFile = FileUtil.convertMultipartFileToFile(multipartFile);
        File lowQualityFile = null;

        try {
            lowQualityFile = FileUtil.reduceAudioQuality(originalFile);
            uploadFilesToS3(originalFile, lowQualityFile, trackId);
        } catch (Exception e) {
            log.error("Error update track file", e);
            throw new RuntimeException("Failed to update track file", e);
        } finally {
            FileUtil.cleanupFile(originalFile);
            if (lowQualityFile != null) {
                FileUtil.cleanupFile(lowQualityFile);
            }
        }
    }

    private int extractTrackDuration(File originalFile) {
        int duration = 0;
        try {
            duration = FileUtil.getAudioDuration(originalFile);
        } catch (Exception e) {
            log.error("Failed to extract track duration: ", e);
        }
        return duration;
    }

    private Long insertTrack(CreateTrackRequestDto dto) {
        try {
            return trackRepository.insert(dto);
        } catch (Exception e) {
            log.error("Failed to insert track to database: ", e);
            throw new RuntimeException("Failed to insert track to database", e);
        }
    }

    private void uploadFilesToS3(@NonNull File originalFile,
                                 @NonNull File lowQualityFile,
                                 @NonNull Long trackId) {
        String highQualityKey = null;

        try {
            highQualityKey = storageService.uploadMediaItem(originalFile, trackId.toString(), getHighQualityKeyPrefix());
            storageService.uploadMediaItem(lowQualityFile, trackId.toString(), getLowQualityKeyPrefix());
        } catch (Exception e) {
            log.error("Error uploading files to S3: ", e);
            if (highQualityKey != null) {
                try {
                    storageService.deleteMediaItem(highQualityKey);
                } catch (Exception deleteException) {
                    log.error("Failed to delete stale file from storage: ", deleteException);
                }
            }
            throw new RuntimeException("Failed to upload files to S3", e);
        }
    }

    private String getHighQualityKeyPrefix() {
        return "track";
    }

    private String getLowQualityKeyPrefix() {
        return "track_low";
    }

    private void artistValidator(Long albumId, Authentication authentication) {
        Artist artist = checkArtistProfile(authentication);
        if (artist == null) {
            // admin
            return;
        }
        AlbumDetail albumDetail = albumRepository.findAlbumDetailById(albumId)
                .orElseThrow(() -> new ObjectNotFoundException("album", albumId.toString()));

        if (albumDetail.getArtists().stream().noneMatch(
                albumArtist -> albumArtist.getId().equals(artist.getId())
        )) {
            throw new AccessDeniedException("This artist is not authorized to perform action with this album");
        }
    }

    private Artist checkArtistProfile(Authentication authentication)  {
        User user = Util.extractUserFromAuthentication(authentication);
        Long userId = user.getId();
        if (user.getRoles().stream().anyMatch(role -> role.getName().equalsIgnoreCase("ROLE_ADMIN"))) {
            return null;
        }

        return artistRepository.findArtistByUserId(userId).orElseThrow(
                () -> new AccessDeniedException("This artist does not associated with any artist profile!")
        );
    }
}