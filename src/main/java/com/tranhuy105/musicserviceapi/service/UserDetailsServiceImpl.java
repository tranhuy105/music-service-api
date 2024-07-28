package com.tranhuy105.musicserviceapi.service;

import com.tranhuy105.musicserviceapi.repository.api.UserRepository;
import com.tranhuy105.musicserviceapi.utils.CachePrefix;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;
    private final CacheService cacheService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String cacheKey = cacheService.getCacheKey(CachePrefix.USER, username);
        return cacheService.cacheOrFetch(cacheKey, () ->
            userRepository.findByEmail(username).orElseThrow(
                    () -> new UsernameNotFoundException("User not found")
            )
        );
    }
}
