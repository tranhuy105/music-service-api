package com.tranhuy105.musicserviceapi.repository.api;

import com.tranhuy105.musicserviceapi.dto.CreateAlbumRequestDto;
import com.tranhuy105.musicserviceapi.dto.AlbumArtistCRUDRequestDto;
import com.tranhuy105.musicserviceapi.model.Album;
import com.tranhuy105.musicserviceapi.model.AlbumDetail;
import com.tranhuy105.musicserviceapi.model.Page;
import com.tranhuy105.musicserviceapi.model.QueryOptions;

import java.util.List;
import java.util.Optional;

public interface AlbumRepository {
    Optional<AlbumDetail> findAlbumDetailById(Long albumId);
    List<Album> findAllAlbum();
    Page<Album> findAllAlbum(QueryOptions queryOptions);
    List<Album> findRelatedAlbum(Long id, int limit);

    void insert(CreateAlbumRequestDto dto);
    void update(Album album);
    void delete(Long id);

    void linkNewArtist(AlbumArtistCRUDRequestDto dto);

    void unlinkArtist(AlbumArtistCRUDRequestDto dto);

    void updateLinkedArtist(AlbumArtistCRUDRequestDto dto);
}
