package org.zszq.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.zszq.dto.MusicResponseDto;
import org.zszq.dto.MusicUploadDto;
import org.zszq.service.MusicService;

/**
 * 音乐管理控制器
 * 提供音乐文件上传、搜索、播放记录、审核等功能
 * 
 * @author GeWhale Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/music")
@RequiredArgsConstructor
@Tag(name = "音乐管理", description = "音乐文件上传、播放、搜索、管理等相关接口")
public class MusicController {

    private final MusicService musicService;

    /**
     * 上传音乐文件
     * 支持上传音频文件及其封面图，同时填写音乐的元数据信息
     * 
     * @param musicFile 音乐文件，支持MP3、WAV、FLAC等格式
     * @param coverFile 封面图片文件（可选）
     * @param title 音乐标题
     * @param artist 艺术家/歌手名称（可选）
     * @param album 专辑名称（可选）
     * @param genre 音乐类型（可选）
     * @param releaseYear 发行年份（可选）
     * @param lyrics 歌词（可选）
     * @param uploadUserId 上传用户ID
     * @return 上传成功的音乐信息
     */
    @Operation(
        summary = "上传音乐",
        description = "上传音乐文件及其元数据信息，支持同时上传封面图"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "上传成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = MusicResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "上传失败，文件格式不支持或参数错误",
            content = @Content(schema = @Schema(type = "string"))
        )
    })
    @PostMapping("/upload")
    public ResponseEntity<?> uploadMusic(
            @Parameter(description = "音乐文件", required = true)
            @RequestParam("musicFile") MultipartFile musicFile,
            @Parameter(description = "封面图片文件")
            @RequestParam(value = "coverFile", required = false) MultipartFile coverFile,
            @Parameter(description = "音乐标题", required = true, example = "蓝色多瑙河")
            @RequestParam("title") String title,
            @Parameter(description = "艺术家/歌手名称", example = "周杰伦")
            @RequestParam(value = "artist", required = false) String artist,
            @Parameter(description = "专辑名称", example = "叶惠美")
            @RequestParam(value = "album", required = false) String album,
            @Parameter(description = "音乐类型", example = "Pop")
            @RequestParam(value = "genre", required = false) String genre,
            @Parameter(description = "发行年份", example = "2003")
            @RequestParam(value = "releaseYear", required = false) Integer releaseYear,
            @Parameter(description = "歌词")
            @RequestParam(value = "lyrics", required = false) String lyrics,
            @Parameter(description = "上传用户ID", required = true, example = "1")
            @RequestParam("uploadUserId") Long uploadUserId) {
        
        MusicUploadDto uploadDto = new MusicUploadDto();
        uploadDto.setTitle(title);
        uploadDto.setArtist(artist);
        uploadDto.setAlbum(album);
        uploadDto.setGenre(genre);
        uploadDto.setReleaseYear(releaseYear);
        uploadDto.setLyrics(lyrics);
        uploadDto.setUploadUserId(uploadUserId);
        
        try {
            MusicResponseDto result = musicService.uploadMusic(musicFile, coverFile, uploadDto);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("上传失败: " + e.getMessage());
        }
    }

    /**
     * 通过ID获取音乐信息
     * 获取指定音乐的详细信息，包括元数据和播放统计
     * 
     * @param id 音乐ID
     * @return 音乐详细信息
     */
    @Operation(
        summary = "获取音乐信息",
        description = "通过音乐ID获取音乐的详细信息和元数据"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "获取成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = MusicResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "音乐不存在",
            content = @Content
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getMusicById(
            @Parameter(description = "音乐ID", required = true, example = "1")
            @PathVariable Long id) {
        try {
            MusicResponseDto music = musicService.findById(id);
            return ResponseEntity.ok(music);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 搜索音乐
     * 支持根据关键词、艺术家、音乐类型等条件搜索音乐
     * 
     * @param keyword 搜索关键词，在标题、艺术家、专辑中搜索
     * @param artist 艺术家名称筛选
     * @param genre 音乐类型筛选
     * @param page 页码，从0开始
     * @param size 每页大小
     * @return 分页的音乐搜索结果
     */
    @Operation(
        summary = "搜索音乐",
        description = "根据关键词、艺术家、音乐类型等条件搜索音乐，支持分页"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "搜索成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Page.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "搜索参数错误",
            content = @Content(schema = @Schema(type = "string"))
        )
    })
    @GetMapping("/search")
    public ResponseEntity<?> searchMusic(
            @Parameter(description = "搜索关键词", example = "蓝色")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "艺术家名称", example = "周杰伦")
            @RequestParam(required = false) String artist,
            @Parameter(description = "音乐类型", example = "Pop")
            @RequestParam(required = false) String genre,
            @Parameter(description = "页码", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Page<MusicResponseDto> result = musicService.searchMusic(keyword, artist, genre, page, size);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("搜索失败: " + e.getMessage());
        }
    }

    /**
     * 记录音乐播放
     * 用户播放音乐时调用，用于统计播放次数
     * 
     * @param id 音乐ID
     * @return 操作结果
     */
    @Operation(
        summary = "记录播放",
        description = "记录音乐播放次数，用于统计和推荐"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "记录成功",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "记录失败，音乐不存在",
            content = @Content(schema = @Schema(type = "string"))
        )
    })
    @PostMapping("/{id}/play")
    public ResponseEntity<?> recordPlay(
            @Parameter(description = "音乐ID", required = true, example = "1")
            @PathVariable Long id) {
        try {
            musicService.incrementPlayCount(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("播放记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户上传的音乐
     * 查询指定用户上传的所有音乐，支持分页
     * 
     * @param userId 用户ID
     * @param page 页码，从0开始
     * @param size 每页大小
     * @return 分页的用户音乐列表
     */
    @Operation(
        summary = "获取用户音乐",
        description = "获取指定用户上传的所有音乐列表，支持分页"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "获取成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Page.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "用户不存在或参数错误",
            content = @Content(schema = @Schema(type = "string"))
        )
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserMusic(
            @Parameter(description = "用户ID", required = true, example = "1")
            @PathVariable Long userId,
            @Parameter(description = "页码", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Page<MusicResponseDto> result = musicService.findByUploadUser(userId, page, size);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取用户音乐失败: " + e.getMessage());
        }
    }

    /**
     * 审核通过音乐
     * 管理员审核通过用户上传的音乐，通过后可公开播放
     * 
     * @param id 音乐ID
     * @return 操作结果
     */
    @Operation(
        summary = "审核通过音乐",
        description = "管理员审核通过音乐，通过后的音乐可公开播放",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "审核成功",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "审核失败，音乐不存在或状态不符",
            content = @Content(schema = @Schema(type = "string"))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "权限不足，仅管理员可操作",
            content = @Content(schema = @Schema(type = "string"))
        )
    })
    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approveMusic(
            @Parameter(description = "音乐ID", required = true, example = "1")
            @PathVariable Long id) {
        try {
            musicService.approveMusic(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("审核失败: " + e.getMessage());
        }
    }

    /**
     * 审核拒绝音乐
     * 管理员审核拒绝用户上传的音乐，拒绝后不可公开播放
     * 
     * @param id 音乐ID
     * @return 操作结果
     */
    @Operation(
        summary = "审核拒绝音乐",
        description = "管理员审核拒绝音乐，拒绝后的音乐不可公开播放",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "审核成功",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "审核失败，音乐不存在或状态不符",
            content = @Content(schema = @Schema(type = "string"))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "权限不足，仅管理员可操作",
            content = @Content(schema = @Schema(type = "string"))
        )
    })
    @PostMapping("/{id}/reject")
    public ResponseEntity<?> rejectMusic(
            @Parameter(description = "音乐ID", required = true, example = "1")
            @PathVariable Long id) {
        try {
            musicService.rejectMusic(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("审核失败: " + e.getMessage());
        }
    }
}