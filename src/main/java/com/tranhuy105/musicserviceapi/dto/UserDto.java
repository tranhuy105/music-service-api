package com.tranhuy105.musicserviceapi.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
public class UserDto {
    private Long id;
    private String firstname;
    private String lastname;
    private LocalDate dob;
    private String email;
    private Boolean isPremium;
    private List<String> roles;
}
