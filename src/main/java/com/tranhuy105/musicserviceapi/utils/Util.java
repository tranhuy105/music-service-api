package com.tranhuy105.musicserviceapi.utils;

import com.tranhuy105.musicserviceapi.model.User;
import org.springframework.security.core.Authentication;

public class Util {
    public static Long extractUserIdFromAuthentication(Authentication authentication) {
        return ((User) authentication.getPrincipal()).getId();
    }
}
