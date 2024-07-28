package com.tranhuy105.musicserviceapi.dto;

import com.tranhuy105.musicserviceapi.model.AlbumDetail;
import com.tranhuy105.musicserviceapi.model.TrackDetail;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AlbumDto extends AlbumDetail {
    List<TrackDetail> tracks;
}
