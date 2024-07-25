package com.tranhuy105.musicserviceapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationRequestDto {
    @NotEmpty(message = "Email must not be empty")
    @NotBlank(message = "Email must not be empty")
    @Email(message = "Invalid email format")
    private String email;
    @Size(min = 6, message = "Password should be 6 characters long minimum")
    private String password;
}
