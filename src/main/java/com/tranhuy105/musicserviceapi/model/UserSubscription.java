package com.tranhuy105.musicserviceapi.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserSubscription {
    private Long userId;
    private String email;
    private Short planId;
    private String planName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
