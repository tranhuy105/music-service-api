package com.tranhuy105.musicserviceapi.repository.impl;

import com.tranhuy105.musicserviceapi.dto.TrackDetailDto;
import com.tranhuy105.musicserviceapi.mapper.TrackDetailDTORowMapper;
import com.tranhuy105.musicserviceapi.model.Album;
import com.tranhuy105.musicserviceapi.model.ArtistProfile;
import com.tranhuy105.musicserviceapi.model.Track;
import com.tranhuy105.musicserviceapi.model.ref.AlbumArtist;
import com.tranhuy105.musicserviceapi.model.ref.TrackAlbum;
import com.tranhuy105.musicserviceapi.repository.api.MetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class MetadataDao implements MetadataRepository {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Optional<Track> findTrackById(Long trackId) {
        String sql = "SELECT * FROM track_details WHERE track_id = ?";
        List<TrackDetailDto> res = jdbcTemplate.query(sql, new TrackDetailDTORowMapper(), trackId);
        return processTrackDetails(res).stream().findFirst();
    }

    @Override
    public List<Track> findAllTrack() {
        String sql = "SELECT * FROM track_details";
        List<TrackDetailDto> res = jdbcTemplate.query(sql, new TrackDetailDTORowMapper());
        return processTrackDetails(res);
    }

    @Override
    public Optional<Album> findAlbumById(Long albumId) {
        return Optional.empty();
    }

    @Override
    public Optional<ArtistProfile> findArtistProfileById(Long id) {
        return Optional.empty();
    }

    @Override
    public Optional<ArtistProfile> findArtistProfileByUserId(Long userId) {
        return Optional.empty();
    }


    private List<Track> processTrackDetails(List<TrackDetailDto> dtoList) {
        Map<Long, Track> trackMap = new HashMap<>();
        Map<Long, TrackAlbum> albumMap = new HashMap<>();

        for (TrackDetailDto dto : dtoList) {
            long trackId = dto.getTrackId();
            Track track = trackMap.get(trackId);

            if (track == null) {
                track = new Track();
                track.setId(trackId);
                track.setTitle(dto.getTrackTitle());
                track.setDuration(dto.getTrackDuration());

                long albumId = dto.getAlbumId();
                TrackAlbum album = albumMap.get(albumId);
                if (album == null) {
                    album = new TrackAlbum();
                    album.setId(albumId);
                    album.setTitle(dto.getAlbumTitle());
                    album.setCoverUrl(dto.getAlbumCoverUrl());
                    album.setArtists(new ArrayList<>());
                    albumMap.put(albumId, album);
                }

                track.setAlbum(album);
                trackMap.put(trackId, track);
            }

            long artistId = dto.getArtistId();
            AlbumArtist albumArtist = new AlbumArtist();
            albumArtist.setId(artistId);
            albumArtist.setStageName(dto.getArtistStageName());
            albumArtist.setProfilePictureUrl(dto.getArtistProfilePictureUrl());
            albumArtist.setRole(dto.getRole());

            TrackAlbum album = track.getAlbum();
            if (album.getArtists().stream().noneMatch(artist -> artist.getId().equals(artistId))) {
                album.getArtists().add(albumArtist);
            }
        }

        return new ArrayList<>(trackMap.values());
    }
}
