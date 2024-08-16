package com.tranhuy105.musicserviceapi.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArtistRequest {
    private Long id;
    private Long userId;
    @NotNull @NotEmpty
    private String artistName;
    private String genre;
    private String portfolioUrl;
    private String bio;
    private String socialMediaLinks;
    private LocalDateTime requestDate;
    private String status; // PENDING, APPROVED, DENIED
    private LocalDateTime reviewDate;
    private Long reviewedBy;
    private String reason;
}
