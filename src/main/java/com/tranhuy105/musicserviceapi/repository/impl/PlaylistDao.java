package com.tranhuy105.musicserviceapi.repository.impl;

import com.tranhuy105.musicserviceapi.dto.PlaylistTrackDto;
import com.tranhuy105.musicserviceapi.mapper.PlaylistSummaryRowMapper;
import com.tranhuy105.musicserviceapi.mapper.PlaylistTrackRowMapper;
import com.tranhuy105.musicserviceapi.model.Playlist;
import com.tranhuy105.musicserviceapi.model.PlaylistTrack;
import com.tranhuy105.musicserviceapi.model.Track;
import com.tranhuy105.musicserviceapi.model.ref.AlbumArtist;
import com.tranhuy105.musicserviceapi.model.ref.TrackAlbum;
import com.tranhuy105.musicserviceapi.repository.api.PlaylistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.*;

@RequiredArgsConstructor
@Repository
public class PlaylistDao implements PlaylistRepository {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<PlaylistTrack> findPlaylistTracksById(@NonNull Long id) {
        String sql = "SELECT * FROM playlist_track_details WHERE playlist_id = ? ORDER BY position";
        List<PlaylistTrackDto> res = jdbcTemplate.query(sql, new PlaylistTrackRowMapper(), id);
        return processPlaylistTrack(res);
    }

    @Override
    public Optional<Playlist> findPlaylistById(@NonNull Long id) {
        String sql = "SELECT * FROM playlist_summary WHERE playlist_id = ?";
        return jdbcTemplate.query(sql, new PlaylistSummaryRowMapper(), id).stream().findFirst();
    }

    private List<PlaylistTrack> processPlaylistTrack(List<PlaylistTrackDto> res) {
        Map<Long, PlaylistTrack> playlistTrackMap = new HashMap<>();
        Map<Long, TrackAlbum> albumMap = new HashMap<>();

        for (PlaylistTrackDto dto : res) {
            long position = dto.getPosition();
            PlaylistTrack playlistTrack = playlistTrackMap.get(position);

            if (playlistTrack == null) {
                playlistTrack = new PlaylistTrack();
                playlistTrack.setPosition(dto.getPosition());
                playlistTrack.setAddedBy(dto.getAddedBy());
                playlistTrack.setAddedAt(dto.getAddedAt());

                Track track = new Track();
                track.setId(dto.getTrackId());
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
                playlistTrack.setTrack(track);
                playlistTrackMap.put(position, playlistTrack);
            }

            long artistId = dto.getArtistId();
            AlbumArtist albumArtist = new AlbumArtist();
            albumArtist.setId(artistId);
            albumArtist.setStageName(dto.getArtistStageName());
            albumArtist.setProfilePictureUrl(dto.getArtistProfilePictureUrl());
            albumArtist.setRole(dto.getArtistRole());

            TrackAlbum album = playlistTrack.getTrack().getAlbum();
            if (album.getArtists().stream().noneMatch(artist -> artist.getId().equals(artistId))) {
                album.getArtists().add(albumArtist);
            }
        }

        return new ArrayList<>(playlistTrackMap.values());
    }
}
