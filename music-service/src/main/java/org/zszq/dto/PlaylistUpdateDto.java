package org.zszq.dto;

import lombok.Data;

@Data
public class PlaylistUpdateDto {

    private String name;
    private String description;
    private String coverUrl;
    private String visibility;
}