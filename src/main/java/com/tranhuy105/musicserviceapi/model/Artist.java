package com.tranhuy105.musicserviceapi.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Artist {
    private Long id;
    private String stageName;
    private String bio;
    private String profilePictureUrl;
    @NotNull
    private Integer followerCount;
}
