package com.tranhuy105.musicserviceapi.service;

import com.tranhuy105.musicserviceapi.dto.AlbumDto;
import com.tranhuy105.musicserviceapi.dto.CreateAlbumRequestDto;
import com.tranhuy105.musicserviceapi.dto.AlbumArtistCRUDRequestDto;
import com.tranhuy105.musicserviceapi.exception.ObjectNotFoundException;
import com.tranhuy105.musicserviceapi.model.*;
import com.tranhuy105.musicserviceapi.model.ref.AlbumArtist;
import com.tranhuy105.musicserviceapi.model.ref.TrackAlbum;
import com.tranhuy105.musicserviceapi.repository.api.AlbumRepository;
import com.tranhuy105.musicserviceapi.repository.api.ArtistRepository;
import com.tranhuy105.musicserviceapi.repository.api.TrackRepository;
import com.tranhuy105.musicserviceapi.utils.CachePrefix;
import com.tranhuy105.musicserviceapi.utils.Util;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlbumService {
    private final AlbumRepository albumRepository;
    private final TrackRepository trackRepository;
    private final ArtistRepository artistRepository;
    private final CacheService cacheService;
    private static final int SEARCH_PAGE_SIZE = 20;

    public Page<Album> searchAlbum(Integer page, String searchQuery) {
        return albumRepository.findAllAlbum(
                QueryOptions.of(page != null ? page : 1,SEARCH_PAGE_SIZE).search(searchQuery).build()
        );
    }

    public AlbumDto findAlbumById(Long albumId) {
        String cacheKey = cacheService.getCacheKey(CachePrefix.ALBUM, albumId);
        return cacheService.cacheOrFetch(cacheKey, () -> {
            AlbumDetail albumDetail = albumRepository.findAlbumDetailById(albumId).orElseThrow(
                    () -> new ObjectNotFoundException("Album Not Exists")
            );

            List<Track> tracks = trackRepository.findTrackRawByAlbumId(albumId);

            return AlbumDtoBuilder(albumDetail, tracks);
        });
    }

    public List<TrackDetail> findAlbumTracks(Long albumId) {
        String cacheKey = cacheService.getCacheKey(CachePrefix.ALBUM_TRACKS, albumId);
        return cacheService.cacheOrFetch(cacheKey, () ->
                trackRepository.findTrackDetailByAlbumId(albumId)
        );
    }

    public void createAlbum(CreateAlbumRequestDto dto, Authentication authentication) {
        Artist artist = checkArtistProfile(authentication);
        if (dto.getArtistRoles().stream().noneMatch(
                requestArtist -> requestArtist.getArtistId().equals(artist.getId()))
        ) {
            throw new RuntimeException("You must include yourself as an artist.");
        }

        albumRepository.insert(dto);
    }

    public void addAlbumArtist(AlbumArtistCRUDRequestDto dto, Authentication authentication) {
        artistValidator(dto, authentication);
        albumRepository.linkNewArtist(dto);
        evictAlbumCache(dto.getAlbumId());
    }

    public void removeAlbumArtist(AlbumArtistCRUDRequestDto dto, Authentication authentication) {
        AlbumDto albumDto = artistValidator(dto, authentication);
        if (albumDto.getArtists().size() <= 1) {
            throw new RuntimeException("Album must have at least one artist");
        }
        albumRepository.unlinkArtist(dto);
        evictAlbumCache(dto.getAlbumId());
    }

    public void updateAlbumArtist(AlbumArtistCRUDRequestDto dto, Authentication authentication) {
        artistValidator(dto, authentication);
        albumRepository.updateLinkedArtist(dto);
        evictAlbumCache(dto.getAlbumId());
    }

    private AlbumDto artistValidator(AlbumArtistCRUDRequestDto dto, Authentication authentication) {
        Artist artist = checkArtistProfile(authentication);
        AlbumDto albumDto = findAlbumById(dto.getAlbumId());
        boolean isValidArtist = albumDto.getArtists().stream()
                .anyMatch(existingArtist -> existingArtist.getId().equals(artist.getId()));

        if (!isValidArtist) {
            throw new AccessDeniedException("This artist is not authorized to perform action with this album");
        }

        return albumDto;
    }

    private Artist checkArtistProfile(Authentication authentication)  {
        Long userId = Util.extractUserIdFromAuthentication(authentication);

       return artistRepository.findArtistByUserId(userId).orElseThrow(
                () -> new AccessDeniedException("This artist doesnt associated with any artist profile")
        );
    }

    private void evictAlbumCache(Long albumId) {
        cacheService.evictCache(CachePrefix.ALBUM, albumId);
        cacheService.evictCache(CachePrefix.ALBUM_TRACKS, albumId);
    }

    private AlbumDto AlbumDtoBuilder(AlbumDetail albumDetail, List<Track> tracks) {
        AlbumDto albumDto = buildAlbumDto(albumDetail);
        List<TrackDetail> trackDetails = buildTrackDetails(tracks, albumDetail);
        albumDto.setTracks(trackDetails);
        return albumDto;
    }

    private AlbumDto buildAlbumDto(AlbumDetail albumDetail) {
        AlbumDto albumDto = new AlbumDto();
        albumDto.setId(albumDetail.getId());
        albumDto.setTitle(albumDetail.getTitle());
        albumDto.setIsSingle(albumDetail.getIsSingle());
        albumDto.setCoverUrl(albumDetail.getCoverUrl());
        albumDto.setReleaseDate(albumDetail.getReleaseDate());
        albumDto.setArtists(albumDetail.getArtists());
        return albumDto;
    }

    private List<TrackDetail> buildTrackDetails(List<Track> tracks, AlbumDetail albumDetail) {
        return tracks.stream()
                .map(track -> {
                    TrackDetail trackDetail = new TrackDetail();
                    trackDetail.setId(track.getId());
                    trackDetail.setTitle(track.getTitle());
                    trackDetail.setDuration(track.getDuration());
                    trackDetail.setStreamCount(track.getStreamCount());

                    TrackAlbum trackAlbum = buildTrackAlbum(albumDetail);
                    trackDetail.setAlbum(trackAlbum);

                    return trackDetail;
                })
                .toList();
    }

    private TrackAlbum buildTrackAlbum(AlbumDetail albumDetail) {
        TrackAlbum trackAlbum = new TrackAlbum();
        trackAlbum.setId(albumDetail.getId());
        trackAlbum.setTitle(albumDetail.getTitle());
        trackAlbum.setCoverUrl(albumDetail.getCoverUrl());

        List<AlbumArtist> albumArtists = albumDetail.getArtists().stream()
                .map(artist -> new AlbumArtist(
                        artist.getId(),
                        artist.getStageName(),
                        artist.getProfilePictureUrl(),
                        artist.getRole()
                ))
                .toList();

        trackAlbum.setArtists(albumArtists);
        return trackAlbum;
    }
}
