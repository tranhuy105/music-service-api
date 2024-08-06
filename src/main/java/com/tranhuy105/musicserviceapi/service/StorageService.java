package com.tranhuy105.musicserviceapi.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;

public interface StorageService {
    URL generatePresignedUrl(String trackId);

    void uploadTrack(File file, String trackId);
}
