package org.zszq.dto;

import lombok.Data;
import org.zszq.entity.Playlist;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PlaylistResponseDto {

    private Long id;
    private String name;
    private String description;
    private String coverUrl;
    private Long creatorId;
    private Playlist.PlaylistType type;
    private Playlist.PlaylistVisibility visibility;
    private Long playCount;
    private Long likeCount;
    private Integer musicCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<MusicResponseDto> musics;

    public static PlaylistResponseDto fromPlaylist(Playlist playlist) {
        PlaylistResponseDto dto = new PlaylistResponseDto();
        dto.setId(playlist.getId());
        dto.setName(playlist.getName());
        dto.setDescription(playlist.getDescription());
        dto.setCoverUrl(playlist.getCoverUrl());
        dto.setCreatorId(playlist.getCreatorId());
        dto.setType(playlist.getType());
        dto.setVisibility(playlist.getVisibility());
        dto.setPlayCount(playlist.getPlayCount());
        dto.setLikeCount(playlist.getLikeCount());
        dto.setMusicCount(playlist.getMusicCount());
        dto.setCreatedAt(playlist.getCreatedAt());
        dto.setUpdatedAt(playlist.getUpdatedAt());
        return dto;
    }

    public static PlaylistResponseDto fromPlaylistWithMusics(Playlist playlist, List<MusicResponseDto> musics) {
        PlaylistResponseDto dto = fromPlaylist(playlist);
        dto.setMusics(musics);
        return dto;
    }
}