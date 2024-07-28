package com.tranhuy105.musicserviceapi.service;

import com.tranhuy105.musicserviceapi.dto.AlbumDto;
import com.tranhuy105.musicserviceapi.exception.ObjectNotFoundException;
import com.tranhuy105.musicserviceapi.model.*;
import com.tranhuy105.musicserviceapi.model.ref.AlbumArtist;
import com.tranhuy105.musicserviceapi.model.ref.TrackAlbum;
import com.tranhuy105.musicserviceapi.repository.api.MetadataRepository;
import com.tranhuy105.musicserviceapi.utils.CachePrefix;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MetadataService {
    private final MetadataRepository metadataRepository;
    private final CacheService cacheService;
    private static final int SEARCH_PAGE_SIZE = 20;

    public Page<Album> searchAlbum(Integer page, String searchQuery) {
        return metadataRepository.findAllAlbum(
                QueryOptions.of(page != null ? page : 1,SEARCH_PAGE_SIZE).search(searchQuery).build()
        );
    }

    public Page<Artist> searchArtist(Integer page, String searchQuery) {
        return metadataRepository.findAllArtist(
                QueryOptions.of(page != null ? page : 1,SEARCH_PAGE_SIZE).search(searchQuery).build()
        );
    }


    public Page<TrackDetail> searchTrack(Integer page, String searchQuery) {
        return metadataRepository.findAllTrack(
                QueryOptions.of(page != null ? page : 1,SEARCH_PAGE_SIZE).search(searchQuery).build()
        );
    }

    public ArtistProfile findArtistProfileById(Long id) {
        String cacheKey = cacheService.getCacheKey(CachePrefix.ARTIST_PROFILE, id);
        return cacheService.cacheOrFetch(cacheKey, () ->
            metadataRepository.findArtistProfileById(id).orElseThrow(
                    () -> new ObjectNotFoundException("artist", id.toString())
            )
        );

    }

    public TrackDetail findTrackById(Long id) {
        String cacheKey = cacheService.getCacheKey(CachePrefix.TRACK, id);
        return cacheService.cacheOrFetch(cacheKey, () ->
                metadataRepository.findTrackById(id).orElseThrow(
                        () -> new ObjectNotFoundException("track", id.toString())
        ));
    }

    public AlbumDto findAlbumById(Long albumId) {
        String cacheKey = cacheService.getCacheKey(CachePrefix.ALBUM, albumId);
        return cacheService.cacheOrFetch(cacheKey, () -> {
            AlbumDetail albumDetail = metadataRepository.findAlbumById(albumId).orElseThrow(
                    () -> new ObjectNotFoundException("Album Not Exists")
            );

            List<Track> tracks = metadataRepository.findAllTrackByAlbumId(albumId);

            return AlbumDtoBuilder(albumDetail, tracks);
        });
    }

    public List<TrackDetail> findAlbumTracks(Long albumId) {
        String cacheKey = cacheService.getCacheKey(CachePrefix.ALBUM_TRACKS, albumId);
        return cacheService.cacheOrFetch(cacheKey, () ->
            metadataRepository.findTrackByAlbumId(albumId)
        );
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
