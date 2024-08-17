package com.tranhuy105.musicserviceapi.service;

import com.tranhuy105.musicserviceapi.model.MediaItem;

import java.io.File;
import java.net.URL;

public interface StorageService {
    URL generateUrl(MediaItem mediaItem, boolean isPremium);

    String uploadMediaItem(File file, String mediaId, String mediaType);
    void deleteMediaItem(String s3Key);
}
