package org.zszq.dto;

import lombok.Data;

@Data
public class MusicUploadDto {

    private String title;
    private String artist;
    private String album;
    private String genre;
    private Integer releaseYear;
    private String lyrics;
    private Long uploadUserId;
}