package com.tranhuy105.musicserviceapi.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class StreamingSource {
    private Long sourceId;
    private SourceType sourceType;

}
