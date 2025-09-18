package org.zszq.dto;

import lombok.Data;

@Data
public class AddMusicToPlaylistDto {

    private Long musicId;
    private Integer sortOrder; // 可选，如果不提供则添加到末尾
}