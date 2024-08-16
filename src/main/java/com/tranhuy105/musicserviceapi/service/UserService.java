package com.tranhuy105.musicserviceapi.service;

import com.tranhuy105.musicserviceapi.dto.UserDto;
import com.tranhuy105.musicserviceapi.exception.ObjectNotFoundException;
import com.tranhuy105.musicserviceapi.model.Role;
import com.tranhuy105.musicserviceapi.model.User;
import com.tranhuy105.musicserviceapi.repository.api.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    public static UserDto convertToDto(User user) {
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

    public void updateUser(Long userId, UserDto userDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("user", userId.toString()));

        user.setFirstname(userDto.getFirstname());
        user.setLastname(userDto.getLastname());
        user.setDob(userDto.getDob());

        userRepository.update(user);
    }

    @Transactional
    public void assignRolesToUser(Long userId, List<Long> roleIds) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("user", userId.toString()));
        userRepository.removeAllRolesFromUser(userId);
        userRepository.addRolesToUser(userId, roleIds);
    }

    @Transactional
    public void revokeRolesFromUser(Long userId, List<Long> roleIds) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("user", userId.toString()));
        userRepository.deleteRolesFromUser(userId, roleIds);
    }
}
