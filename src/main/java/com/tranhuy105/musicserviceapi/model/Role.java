package com.tranhuy105.musicserviceapi.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Role {
    private Short id;
    // unique
    private String name;
}
