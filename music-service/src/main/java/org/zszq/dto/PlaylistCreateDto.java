package org.zszq.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.zszq.entity.Playlist;

@Data
public class PlaylistCreateDto {

    @NotBlank(message = "播放列表名称不能为空")
    @Size(max = 100, message = "播放列表名称不能超过100个字符")
    private String name;

    @Size(max = 500, message = "描述不能超过500个字符")
    private String description;

    private String coverUrl;

    private Playlist.PlaylistVisibility visibility = Playlist.PlaylistVisibility.PUBLIC;
}