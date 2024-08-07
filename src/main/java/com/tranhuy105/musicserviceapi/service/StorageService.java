package com.tranhuy105.musicserviceapi.service;

import com.tranhuy105.musicserviceapi.model.MediaItem;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;

public interface StorageService {
    URL generatePresignedUrl(MediaItem mediaItem);

    void uploadMediaItem(File file, String mediaId, String mediaType);
}
