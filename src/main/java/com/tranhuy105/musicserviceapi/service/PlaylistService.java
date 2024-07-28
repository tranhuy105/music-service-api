package com.tranhuy105.musicserviceapi.service;

import com.tranhuy105.musicserviceapi.dto.PlaylistTrackDto;
import com.tranhuy105.musicserviceapi.exception.ObjectNotFoundException;
import com.tranhuy105.musicserviceapi.model.Page;
import com.tranhuy105.musicserviceapi.model.Playlist;
import com.tranhuy105.musicserviceapi.model.PlaylistTrack;
import com.tranhuy105.musicserviceapi.model.QueryOptions;
import com.tranhuy105.musicserviceapi.repository.api.PlaylistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaylistService {
    private final PlaylistRepository playlistRepository;
    private static final int PLAYLIST_PAGE_SIZE = 200;
    private static final int SEARCH_PAGE_SIZE = 20;

    public PlaylistTrackDto findPlaylistById(Long id, Integer page) {
        Playlist playlist = playlistRepository.findPlaylistById(id).orElseThrow(
                () -> new ObjectNotFoundException("playlist", id.toString())
        );

        Page<PlaylistTrack> tracks = playlistRepository.findPlaylistTracksById(id,
                QueryOptions
                        .of(page != null ? page : 1, PLAYLIST_PAGE_SIZE)
                        .sortBy("position")
                        .build()
        );

        return new PlaylistTrackDto(playlist, tracks);
    }

    public Page<PlaylistTrack> findPlaylistTracks(Long id, Integer page) {
        return playlistRepository.findPlaylistTracksById(id,
                QueryOptions
                        .of(page != null ? page : 1, PLAYLIST_PAGE_SIZE)
                        .sortBy("position")
                        .build()
        );
    }

    public Page<Playlist> searchPlaylist(Integer page, String searchQuery) {
        return playlistRepository.findAllPlaylist(QueryOptions
                .of(page != null ? page : 1, SEARCH_PAGE_SIZE)
                .search(searchQuery)
                .build());
    }
}
