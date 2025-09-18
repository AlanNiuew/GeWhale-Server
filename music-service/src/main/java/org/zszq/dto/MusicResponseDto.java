package org.zszq.dto;

import lombok.Data;
import org.zszq.entity.Music;

import java.time.LocalDateTime;

@Data
public class MusicResponseDto {

    private Long id;
    private String title;
    private String artist;
    private String album;
    private Integer durationSeconds;
    private String fileUrl;
    private String coverUrl;
    private Music.MusicStatus status;
    private Long uploadUserId;
    private Long playCount;
    private Long likeCount;
    private String genre;
    private Integer releaseYear;
    private LocalDateTime createdAt;

    public static MusicResponseDto fromMusic(Music music) {
        MusicResponseDto dto = new MusicResponseDto();
        dto.setId(music.getId());
        dto.setTitle(music.getTitle());
        dto.setArtist(music.getArtist());
        dto.setAlbum(music.getAlbum());
        dto.setDurationSeconds(music.getDurationSeconds());
        dto.setFileUrl(music.getFileUrl());
        dto.setCoverUrl(music.getCoverUrl());
        dto.setStatus(music.getStatus());
        dto.setUploadUserId(music.getUploadUserId());
        dto.setPlayCount(music.getPlayCount());
        dto.setLikeCount(music.getLikeCount());
        dto.setGenre(music.getGenre());
        dto.setReleaseYear(music.getReleaseYear());
        dto.setCreatedAt(music.getCreatedAt());
        return dto;
    }
}