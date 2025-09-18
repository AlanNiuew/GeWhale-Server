package org.zszq.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.zszq.dto.MusicResponseDto;
import org.zszq.dto.MusicUploadDto;
import org.zszq.entity.Music;
import org.zszq.repository.MusicRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MusicService {

    private final MusicRepository musicRepository;
    private final FileStorageService fileStorageService;

    public MusicResponseDto uploadMusic(MultipartFile musicFile, MultipartFile coverFile, MusicUploadDto uploadDto) {
        try {
            // 生成唯一文件名
            String musicFileName = generateFileName(musicFile.getOriginalFilename());
            String coverFileName = coverFile != null ? generateFileName(coverFile.getOriginalFilename()) : null;

            // 上传音乐文件
            String musicUrl = fileStorageService.uploadMusicFile(musicFile, musicFileName);
            
            // 上传封面图片（如果有）
            String coverUrl = null;
            if (coverFile != null) {
                coverUrl = fileStorageService.uploadCoverImage(coverFile, coverFileName);
            }

            // 创建音乐记录
            Music music = new Music();
            music.setTitle(uploadDto.getTitle());
            music.setArtist(uploadDto.getArtist());
            music.setAlbum(uploadDto.getAlbum());
            music.setGenre(uploadDto.getGenre());
            music.setReleaseYear(uploadDto.getReleaseYear());
            music.setLyrics(uploadDto.getLyrics());
            music.setUploadUserId(uploadDto.getUploadUserId());
            music.setFileUrl(musicUrl);
            music.setCoverUrl(coverUrl);
            music.setFileSize(musicFile.getSize());
            music.setStatus(Music.MusicStatus.PENDING);

            // 提取音频元数据（时长、比特率等）
            // TODO: 实现音频元数据提取
            
            Music savedMusic = musicRepository.save(music);
            return MusicResponseDto.fromMusic(savedMusic);
            
        } catch (Exception e) {
            throw new RuntimeException("音乐上传失败: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public MusicResponseDto findById(Long id) {
        Music music = musicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("音乐不存在"));
        return MusicResponseDto.fromMusic(music);
    }

    @Transactional(readOnly = true)
    public Page<MusicResponseDto> searchMusic(String keyword, String artist, String genre, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        Page<Music> musicPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            musicPage = musicRepository.searchByKeyword(keyword.trim(), pageable);
        } else {
            musicPage = musicRepository.searchByFilters(artist, genre, pageable);
        }
        
        return musicPage.map(MusicResponseDto::fromMusic);
    }

    public void incrementPlayCount(Long musicId) {
        Music music = musicRepository.findById(musicId)
                .orElseThrow(() -> new RuntimeException("音乐不存在"));
        music.setPlayCount(music.getPlayCount() + 1);
        musicRepository.save(music);
    }

    @Transactional(readOnly = true)
    public Page<MusicResponseDto> findByUploadUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Music> musicPage = musicRepository.findByUploadUserId(userId, pageable);
        return musicPage.map(MusicResponseDto::fromMusic);
    }

    public void approveMusic(Long musicId) {
        Music music = musicRepository.findById(musicId)
                .orElseThrow(() -> new RuntimeException("音乐不存在"));
        music.setStatus(Music.MusicStatus.APPROVED);
        musicRepository.save(music);
    }

    public void rejectMusic(Long musicId) {
        Music music = musicRepository.findById(musicId)
                .orElseThrow(() -> new RuntimeException("音乐不存在"));
        music.setStatus(Music.MusicStatus.REJECTED);
        musicRepository.save(music);
    }

    private String generateFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.lastIndexOf(".") > 0) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }
}