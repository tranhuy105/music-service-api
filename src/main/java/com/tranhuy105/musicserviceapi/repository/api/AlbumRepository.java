package com.tranhuy105.musicserviceapi.repository.api;

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
}
