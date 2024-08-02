package com.tranhuy105.musicserviceapi.utils;

import lombok.Getter;

@Getter
public enum CachePrefix {
    PLAYLIST_TRACKS("playlist:tracks:"),
    PLAYLIST("playlist:"),
    USER("user:"),
    ARTIST_PROFILE("artist:profile:"),
    ALBUM("album:"),
    ALBUM_TRACKS("album:tracks:"),
    TRACK("track:"),
    STREAMING_SESSION("session:"),
    STREAMING_HISTORY("session:history:"),
    SESSION_LOCK("lock:session:");

    private final String prefix;

    CachePrefix(String prefix) {
        this.prefix = prefix;
    }
}

