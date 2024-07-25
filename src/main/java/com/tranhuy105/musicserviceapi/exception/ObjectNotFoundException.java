package com.tranhuy105.musicserviceapi.exception;

public class ObjectNotFoundException extends RuntimeException {
    public ObjectNotFoundException(String message) {
        super(message);
    }

    public ObjectNotFoundException(String type, String objectId) {
        super(type + " not found for id: "+objectId);
    }
}
