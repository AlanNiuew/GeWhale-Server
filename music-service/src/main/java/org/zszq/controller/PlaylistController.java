package org.zszq.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zszq.dto.*;
import org.zszq.service.PlaylistService;

import java.util.List;

/**
 * 播放列表管理控制器
 * 提供播放列表的创建、更新、删除、音乐管理、搜索等功能
 * 
 * @author GeWhale Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
@Tag(name = "播放列表管理", description = "播放列表创建、编辑、音乐管理等相关接口")
public class PlaylistController {

    private final PlaylistService playlistService;

    /**
     * 创建播放列表
     * 用户可以创建新的播放列表，包括公开和私人列表
     * 
     * @param createDto 播放列表创建信息
     * @param creatorId 创建者用户ID
     * @return 创建成功的播放列表信息
     */
    @Operation(
        summary = "创建播放列表",
        description = "用户创建新的播放列表，可以设置为公开或私人"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "创建成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PlaylistResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "创建失败，参数错误或用户不存在",
            content = @Content(schema = @Schema(type = "string"))
        )
    })
    @PostMapping
    public ResponseEntity<?> createPlaylist(
            @Parameter(description = "播放列表创建信息", required = true)
            @Valid @RequestBody PlaylistCreateDto createDto,
            @Parameter(description = "创建者用户ID", required = true, example = "1")
            @RequestParam("creatorId") Long creatorId) {
        try {
            PlaylistResponseDto result = playlistService.createPlaylist(createDto, creatorId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("创建播放列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取播放列表基本信息
     * 获取指定播放列表的基本信息，不包含音乐列表
     * 
     * @param id 播放列表ID
     * @return 播放列表基本信息
     */
    @Operation(
        summary = "获取播放列表信息",
        description = "获取指定播放列表的基本信息，不包含音乐列表"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "获取成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PlaylistResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "播放列表不存在",
            content = @Content
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getPlaylist(
            @Parameter(description = "播放列表ID", required = true, example = "1")
            @PathVariable Long id) {
        try {
            PlaylistResponseDto result = playlistService.getPlaylistById(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 获取播放列表及其音乐
     * 获取指定播放列表的详细信息，包含所有音乐列表，支持分页
     * 
     * @param id 播放列表ID
     * @param page 页码，从0开始
     * @param size 每页大小
     * @return 播放列表详细信息及音乐列表
     */
    @Operation(
        summary = "获取播放列表及音乐",
        description = "获取播放列表详细信息和所有音乐，支持分页加载"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "获取成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PlaylistResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "参数错误或播放列表不存在",
            content = @Content(schema = @Schema(type = "string"))
        )
    })
    @GetMapping("/{id}/musics")
    public ResponseEntity<?> getPlaylistWithMusics(
            @Parameter(description = "播放列表ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "页码", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        try {
            PlaylistResponseDto result = playlistService.getPlaylistWithMusics(id, page, size);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取播放列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的播放列表
     * 获取指定用户创建的所有播放列表，支持分页
     * 
     * @param userId 用户ID
     * @param page 页码，从0开始
     * @param size 每页大小
     * @return 分页的用户播放列表
     */
    @Operation(
        summary = "获取用户播放列表",
        description = "获取指定用户创建的所有播放列表，支持分页查询"
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
    public ResponseEntity<?> getUserPlaylists(
            @Parameter(description = "用户ID", required = true, example = "1")
            @PathVariable Long userId,
            @Parameter(description = "页码", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        try {
            Page<PlaylistResponseDto> result = playlistService.getUserPlaylists(userId, page, size);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取用户播放列表失败: " + e.getMessage());
        }
    }

    /**
     * 搜索播放列表
     * 根据关键词在播放列表名称和描述中搜索，支持分页
     * 
     * @param keyword 搜索关键词
     * @param page 页码，从0开始
     * @param size 每页大小
     * @return 分页的搜索结果
     */
    @Operation(
        summary = "搜索播放列表",
        description = "根据关键词在播放列表名称和描述中搜索，支持模糊匹配"
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
    public ResponseEntity<?> searchPlaylists(
            @Parameter(description = "搜索关键词", required = true, example = "流行音乐")
            @RequestParam String keyword,
            @Parameter(description = "页码", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        try {
            Page<PlaylistResponseDto> result = playlistService.searchPlaylists(keyword, page, size);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("搜索播放列表失败: " + e.getMessage());
        }
    }

    /**
     * 更新播放列表
     * 用户可以更新自己创建的播放列表信息
     * 
     * @param id 播放列表ID
     * @param updateDto 更新的播放列表信息
     * @param userId 操作用户ID，用于权限验证
     * @return 更新后的播放列表信息
     */
    @Operation(
        summary = "更新播放列表",
        description = "用户更新自己创建的播放列表信息，如名称、描述、公开性等"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "更新成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PlaylistResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "更新失败，参数错误或权限不足",
            content = @Content(schema = @Schema(type = "string"))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "播放列表不存在",
            content = @Content(schema = @Schema(type = "string"))
        )
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePlaylist(
            @Parameter(description = "播放列表ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "更新的播放列表信息", required = true)
            @RequestBody PlaylistUpdateDto updateDto,
            @Parameter(description = "操作用户ID", required = true, example = "1")
            @RequestParam("userId") Long userId) {
        try {
            PlaylistResponseDto result = playlistService.updatePlaylist(id, updateDto, userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("更新播放列表失败: " + e.getMessage());
        }
    }

    /**
     * 删除播放列表
     * 用户可以删除自己创建的播放列表
     * 
     * @param id 播放列表ID
     * @param userId 操作用户ID，用于权限验证
     * @return 操作结果
     */
    @Operation(
        summary = "删除播放列表",
        description = "用户删除自己创建的播放列表，该操作不可逆转"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "删除成功",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "删除失败，权限不足或播放列表不存在",
            content = @Content(schema = @Schema(type = "string"))
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePlaylist(
            @Parameter(description = "播放列表ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "操作用户ID", required = true, example = "1")
            @RequestParam("userId") Long userId) {
        try {
            playlistService.deletePlaylist(id, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("删除播放列表失败: " + e.getMessage());
        }
    }

    /**
     * 添加音乐到播放列表
     * 将指定的音乐添加到播放列表中
     * 
     * @param id 播放列表ID
     * @param addDto 要添加的音乐信息
     * @param userId 操作用户ID，用于权限验证
     * @return 操作结果
     */
    @Operation(
        summary = "添加音乐到播放列表",
        description = "将指定的音乐添加到播放列表中，需要用户有播放列表的编辑权限"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "添加成功",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "添加失败，音乐或播放列表不存在，或权限不足",
            content = @Content(schema = @Schema(type = "string"))
        )
    })
    @PostMapping("/{id}/musics")
    public ResponseEntity<?> addMusicToPlaylist(
            @Parameter(description = "播放列表ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "要添加的音乐信息", required = true)
            @RequestBody AddMusicToPlaylistDto addDto,
            @Parameter(description = "操作用户ID", required = true, example = "1")
            @RequestParam("userId") Long userId) {
        try {
            playlistService.addMusicToPlaylist(id, addDto, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("添加音乐失败: " + e.getMessage());
        }
    }

    /**
     * 从播放列表移除音乐
     * 从指定的播放列表中移除某个音乐
     * 
     * @param id 播放列表ID
     * @param musicId 要移除的音乐ID
     * @param userId 操作用户ID，用于权限验证
     * @return 操作结果
     */
    @Operation(
        summary = "移除播放列表中的音乐",
        description = "从指定的播放列表中移除某个音乐，需要用户有播放列表的编辑权限"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "移除成功",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "移除失败，音乐或播放列表不存在，或权限不足",
            content = @Content(schema = @Schema(type = "string"))
        )
    })
    @DeleteMapping("/{id}/musics/{musicId}")
    public ResponseEntity<?> removeMusicFromPlaylist(
            @Parameter(description = "播放列表ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "要移除的音乐ID", required = true, example = "1")
            @PathVariable Long musicId,
            @Parameter(description = "操作用户ID", required = true, example = "1")
            @RequestParam("userId") Long userId) {
        try {
            playlistService.removeMusicFromPlaylist(id, musicId, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("移除音乐失败: " + e.getMessage());
        }
    }

    /**
     * 记录播放列表播放
     * 用户播放播放列表时调用，用于统计播放次数
     * 
     * @param id 播放列表ID
     * @return 操作结果
     */
    @Operation(
        summary = "记录播放列表播放",
        description = "记录播放列表的播放次数，用于统计和推荐"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "记录成功",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "记录失败，播放列表不存在",
            content = @Content(schema = @Schema(type = "string"))
        )
    })
    @PostMapping("/{id}/play")
    public ResponseEntity<?> recordPlaylistPlay(
            @Parameter(description = "播放列表ID", required = true, example = "1")
            @PathVariable Long id) {
        try {
            playlistService.incrementPlayCount(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("记录播放失败: " + e.getMessage());
        }
    }

    /**
     * 点赞/取消点赞播放列表
     * 用户可以对播放列表进行点赞或取消点赞操作
     * 
     * @param id 播放列表ID
     * @param userId 操作用户ID
     * @param isLike true表示点赞，false表示取消点赞
     * @return 操作结果
     */
    @Operation(
        summary = "点赞/取消点赞播放列表",
        description = "用户对播放列表进行点赞或取消点赞操作"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "操作成功",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "操作失败，播放列表或用户不存在",
            content = @Content(schema = @Schema(type = "string"))
        )
    })
    @PostMapping("/{id}/like")
    public ResponseEntity<?> toggleLike(
            @Parameter(description = "播放列表ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "操作用户ID", required = true, example = "1")
            @RequestParam("userId") Long userId,
            @Parameter(description = "是否点赞", required = true, example = "true")
            @RequestParam("isLike") boolean isLike) {
        try {
            playlistService.toggleLike(id, userId, isLike);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("操作失败: " + e.getMessage());
        }
    }

    /**
     * 获取热门播放列表
     * 根据播放次数和点赞数排序，获取热门播放列表
     * 
     * @param limit 返回数量限制
     * @return 热门播放列表列表
     */
    @Operation(
        summary = "获取热门播放列表",
        description = "根据播放量和点赞数获取热门播放列表"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "获取成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PlaylistResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "获取失败",
            content = @Content(schema = @Schema(type = "string"))
        )
    })
    @GetMapping("/top")
    public ResponseEntity<?> getTopPlaylists(
            @Parameter(description = "返回数量限制", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<PlaylistResponseDto> result = playlistService.getTopPlaylists(limit);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取热门播放列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取最新播放列表
     * 按创建时间排序，获取最新创建的播放列表
     * 
     * @param limit 返回数量限制
     * @return 最新播放列表列表
     */
    @Operation(
        summary = "获取最新播放列表",
        description = "按创建时间降序获取最新创建的播放列表"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "获取成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PlaylistResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "获取失败",
            content = @Content(schema = @Schema(type = "string"))
        )
    })
    @GetMapping("/latest")
    public ResponseEntity<?> getLatestPlaylists(
            @Parameter(description = "返回数量限制", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<PlaylistResponseDto> result = playlistService.getLatestPlaylists(limit);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取最新播放列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户收藏列表
     * 获取指定用户的个人收藏播放列表
     * 
     * @param userId 用户ID
     * @return 用户收藏列表
     */
    @Operation(
        summary = "获取用户收藏列表",
        description = "获取指定用户的个人收藏播放列表"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "获取成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PlaylistResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "用户不存在或获取失败",
            content = @Content(schema = @Schema(type = "string"))
        )
    })
    @GetMapping("/user/{userId}/favorites")
    public ResponseEntity<?> getFavoritePlaylist(
            @Parameter(description = "用户ID", required = true, example = "1")
            @PathVariable Long userId) {
        try {
            PlaylistResponseDto result = playlistService.getFavoritePlaylist(userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取收藏列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户最近播放列表
     * 获取指定用户的最近播放播放列表
     * 
     * @param userId 用户ID
     * @return 用户最近播放列表
     */
    @Operation(
        summary = "获取用户最近播放列表",
        description = "获取指定用户的最近播放播放列表记录"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "获取成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PlaylistResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "用户不存在或获取失败",
            content = @Content(schema = @Schema(type = "string"))
        )
    })
    @GetMapping("/user/{userId}/recent")
    public ResponseEntity<?> getRecentlyPlayedPlaylist(
            @Parameter(description = "用户ID", required = true, example = "1")
            @PathVariable Long userId) {
        try {
            PlaylistResponseDto result = playlistService.getRecentlyPlayedPlaylist(userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("获取最近播放列表失败: " + e.getMessage());
        }
    }
}