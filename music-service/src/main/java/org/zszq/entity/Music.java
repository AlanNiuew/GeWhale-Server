package org.zszq.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "music")
public class Music {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 50)
    private String artist;

    @Column(length = 50)
    private String album;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @Column(name = "cover_url")
    private String coverUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MusicStatus status = MusicStatus.PENDING;

    @Column(name = "upload_user_id", nullable = false)
    private Long uploadUserId;

    @Column(name = "play_count")
    private Long playCount = 0L;

    @Column(name = "like_count")
    private Long likeCount = 0L;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "bit_rate")
    private Integer bitRate;

    @Column(name = "sample_rate")
    private Integer sampleRate;

    @Column(length = 20)
    private String genre;

    @Column(name = "release_year")
    private Integer releaseYear;

    @Column(length = 1000)
    private String lyrics;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum MusicStatus {
        PENDING,    // 待审核
        APPROVED,   // 已审核通过
        REJECTED,   // 审核拒绝
        DELETED     // 已删除
    }
}