package org.zszq.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zszq.dto.*;
import org.zszq.entity.Music;
import org.zszq.entity.Playlist;
import org.zszq.entity.PlaylistMusic;
import org.zszq.repository.MusicRepository;
import org.zszq.repository.PlaylistMusicRepository;
import org.zszq.repository.PlaylistRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistMusicRepository playlistMusicRepository;
    private final MusicRepository musicRepository;

    public PlaylistResponseDto createPlaylist(PlaylistCreateDto createDto, Long creatorId) {
        Playlist playlist = new Playlist();
        playlist.setName(createDto.getName());
        playlist.setDescription(createDto.getDescription());
        playlist.setCoverUrl(createDto.getCoverUrl());
        playlist.setCreatorId(creatorId);
        playlist.setVisibility(createDto.getVisibility());
        playlist.setType(Playlist.PlaylistType.USER_CREATED);

        Playlist savedPlaylist = playlistRepository.save(playlist);
        return PlaylistResponseDto.fromPlaylist(savedPlaylist);
    }

    @Transactional(readOnly = true)
    public PlaylistResponseDto getPlaylistById(Long id) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("播放列表不存在"));
        return PlaylistResponseDto.fromPlaylist(playlist);
    }

    @Transactional(readOnly = true)
    public PlaylistResponseDto getPlaylistWithMusics(Long id, int page, int size) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("播放列表不存在"));

        Pageable pageable = PageRequest.of(page, size);
        Page<PlaylistMusic> playlistMusics = playlistMusicRepository
                .findByPlaylistIdOrderBySortOrder(id, pageable);

        List<MusicResponseDto> musics = playlistMusics.getContent().stream()
                .map(pm -> MusicResponseDto.fromMusic(pm.getMusic()))
                .collect(Collectors.toList());

        return PlaylistResponseDto.fromPlaylistWithMusics(playlist, musics);
    }

    @Transactional(readOnly = true)
    public Page<PlaylistResponseDto> getUserPlaylists(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Playlist> playlists = playlistRepository.findByCreatorId(userId, pageable);
        return playlists.map(PlaylistResponseDto::fromPlaylist);
    }

    @Transactional(readOnly = true)
    public Page<PlaylistResponseDto> searchPlaylists(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("playCount").descending());
        Page<Playlist> playlists = playlistRepository.searchPublicPlaylists(keyword, pageable);
        return playlists.map(PlaylistResponseDto::fromPlaylist);
    }

    public PlaylistResponseDto updatePlaylist(Long id, PlaylistUpdateDto updateDto, Long userId) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("播放列表不存在"));

        if (!playlist.getCreatorId().equals(userId)) {
            throw new RuntimeException("无权限修改此播放列表");
        }

        if (updateDto.getName() != null) {
            playlist.setName(updateDto.getName());
        }
        if (updateDto.getDescription() != null) {
            playlist.setDescription(updateDto.getDescription());
        }
        if (updateDto.getCoverUrl() != null) {
            playlist.setCoverUrl(updateDto.getCoverUrl());
        }
        if (updateDto.getVisibility() != null) {
            playlist.setVisibility(Playlist.PlaylistVisibility.valueOf(updateDto.getVisibility()));
        }

        Playlist savedPlaylist = playlistRepository.save(playlist);
        return PlaylistResponseDto.fromPlaylist(savedPlaylist);
    }

    public void deletePlaylist(Long id, Long userId) {
        Playlist playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("播放列表不存在"));

        if (!playlist.getCreatorId().equals(userId)) {
            throw new RuntimeException("无权限删除此播放列表");
        }

        playlistRepository.delete(playlist);
    }

    public void addMusicToPlaylist(Long playlistId, AddMusicToPlaylistDto addDto, Long userId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("播放列表不存在"));

        if (!playlist.getCreatorId().equals(userId)) {
            throw new RuntimeException("无权限修改此播放列表");
        }

        Music music = musicRepository.findById(addDto.getMusicId())
                .orElseThrow(() -> new RuntimeException("音乐不存在"));

        // 检查音乐是否已在播放列表中
        if (playlistMusicRepository.findByPlaylistIdAndMusicId(playlistId, addDto.getMusicId()).isPresent()) {
            throw new RuntimeException("音乐已在播放列表中");
        }

        PlaylistMusic playlistMusic = new PlaylistMusic();
        playlistMusic.setPlaylist(playlist);
        playlistMusic.setMusic(music);
        playlistMusic.setAddedById(userId);

        // 设置排序顺序
        if (addDto.getSortOrder() != null) {
            playlistMusic.setSortOrder(addDto.getSortOrder());
        } else {
            Integer maxOrder = playlistMusicRepository.findMaxSortOrderByPlaylistId(playlistId).orElse(0);
            playlistMusic.setSortOrder(maxOrder + 1);
        }

        playlistMusicRepository.save(playlistMusic);

        // 更新播放列表的音乐数量
        int musicCount = playlistMusicRepository.countByPlaylistId(playlistId);
        playlistRepository.updateMusicCount(playlistId, musicCount);
    }

    public void removeMusicFromPlaylist(Long playlistId, Long musicId, Long userId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new RuntimeException("播放列表不存在"));

        if (!playlist.getCreatorId().equals(userId)) {
            throw new RuntimeException("无权限修改此播放列表");
        }

        PlaylistMusic playlistMusic = playlistMusicRepository
                .findByPlaylistIdAndMusicId(playlistId, musicId)
                .orElseThrow(() -> new RuntimeException("音乐不在播放列表中"));

        Integer sortOrder = playlistMusic.getSortOrder();
        playlistMusicRepository.deleteByPlaylistIdAndMusicId(playlistId, musicId);

        // 更新后续音乐的排序顺序
        playlistMusicRepository.updateSortOrderAfterRemoval(playlistId, sortOrder);

        // 更新播放列表的音乐数量
        int musicCount = playlistMusicRepository.countByPlaylistId(playlistId);
        playlistRepository.updateMusicCount(playlistId, musicCount);
    }

    public void incrementPlayCount(Long playlistId) {
        playlistRepository.incrementPlayCount(playlistId);
    }

    public void toggleLike(Long playlistId, Long userId, boolean isLike) {
        if (isLike) {
            playlistRepository.incrementLikeCount(playlistId);
        } else {
            playlistRepository.decrementLikeCount(playlistId);
        }
    }

    @Transactional(readOnly = true)
    public List<PlaylistResponseDto> getTopPlaylists(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Playlist> playlists = playlistRepository.findTopPublicPlaylists(pageable);
        return playlists.stream()
                .map(PlaylistResponseDto::fromPlaylist)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PlaylistResponseDto> getLatestPlaylists(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Playlist> playlists = playlistRepository.findLatestPublicPlaylists(pageable);
        return playlists.stream()
                .map(PlaylistResponseDto::fromPlaylist)
                .collect(Collectors.toList());
    }

    /**
     * 获取或创建用户的收藏列表
     */
    public PlaylistResponseDto getFavoritePlaylist(Long userId) {
        return playlistRepository.findByCreatorIdAndType(userId, Playlist.PlaylistType.FAVORITE)
                .map(PlaylistResponseDto::fromPlaylist)
                .orElseGet(() -> createSystemPlaylist(userId, "我的收藏", Playlist.PlaylistType.FAVORITE));
    }

    /**
     * 获取或创建用户的最近播放列表
     */
    public PlaylistResponseDto getRecentlyPlayedPlaylist(Long userId) {
        return playlistRepository.findByCreatorIdAndType(userId, Playlist.PlaylistType.RECENTLY_PLAYED)
                .map(PlaylistResponseDto::fromPlaylist)
                .orElseGet(() -> createSystemPlaylist(userId, "最近播放", Playlist.PlaylistType.RECENTLY_PLAYED));
    }

    private PlaylistResponseDto createSystemPlaylist(Long userId, String name, Playlist.PlaylistType type) {
        Playlist playlist = new Playlist();
        playlist.setName(name);
        playlist.setCreatorId(userId);
        playlist.setType(type);
        playlist.setVisibility(Playlist.PlaylistVisibility.PRIVATE);

        Playlist savedPlaylist = playlistRepository.save(playlist);
        return PlaylistResponseDto.fromPlaylist(savedPlaylist);
    }
}