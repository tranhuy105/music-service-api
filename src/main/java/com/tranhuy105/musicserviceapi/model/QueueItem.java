package com.tranhuy105.musicserviceapi.model;

public record QueueItem(Long id, ItemType itemType) {
    public enum ItemType {
        TRACK,
        AD
    }
}
