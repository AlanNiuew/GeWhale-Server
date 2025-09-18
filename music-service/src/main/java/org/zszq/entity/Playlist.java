package org.zszq.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "playlists")
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "cover_url")
    private String coverUrl;

    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlaylistType type = PlaylistType.USER_CREATED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlaylistVisibility visibility = PlaylistVisibility.PUBLIC;

    @Column(name = "play_count")
    private Long playCount = 0L;

    @Column(name = "like_count")
    private Long likeCount = 0L;

    @Column(name = "music_count")
    private Integer musicCount = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "playlist", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC")
    private List<PlaylistMusic> playlistMusics = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum PlaylistType {
        USER_CREATED,   // 用户创建
        SYSTEM,         // 系统播放列表
        FAVORITE,       // 收藏列表
        RECENTLY_PLAYED // 最近播放
    }

    public enum PlaylistVisibility {
        PUBLIC,    // 公开
        PRIVATE,   // 私有
        FRIENDS    // 仅好友可见
    }
}