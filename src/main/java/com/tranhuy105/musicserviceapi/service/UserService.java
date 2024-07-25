package com.tranhuy105.musicserviceapi.service;

import com.tranhuy105.musicserviceapi.dto.UserDto;
import com.tranhuy105.musicserviceapi.exception.ObjectNotFoundException;
import com.tranhuy105.musicserviceapi.model.Role;
import com.tranhuy105.musicserviceapi.model.User;
import com.tranhuy105.musicserviceapi.repository.api.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(
                        () -> new ObjectNotFoundException("user", id.toString())
                );
        return convertToDto(user);
    }

    private UserDto convertToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .dob(user.getDob())
                .email(user.getEmail())
                .isPremium(user.getIsPremium())
                .roles(user.getRoles().stream().map(Role::getName).toList())
                .build();
    }
}
