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
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.zszq.dto.UserRegistrationDto;
import org.zszq.dto.UserResponseDto;
import org.zszq.dto.UserUpdateDto;
import org.zszq.entity.User;
import org.zszq.service.UserService;

/**
 * 用户管理控制器
 * 提供用户注册、登录、个人信息管理、用户查询等功能
 * 
 * @author GeWhale Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户注册、登录、个人信息管理等相关接口")
public class UserController {

    private final UserService userService;

    /**
     * 用户注册
     * 新用户通过提供用户名、邮箱、密码等信息进行注册
     * 
     * @param registrationDto 用户注册信息，包含用户名、邮箱、密码等
     * @return 注册成功后的用户信息
     */
    @Operation(
        summary = "用户注册",
        description = "新用户通过提供用户名、邮箱、密码等信息进行注册"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "注册成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "请求参数错误或用户名/邮箱已存在",
            content = @Content(schema = @Schema(type = "string"))
        )
    })
    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(
            @Parameter(description = "用户注册信息", required = true)
            @Valid @RequestBody UserRegistrationDto registrationDto) {
        User user = userService.registerUser(registrationDto);
        return ResponseEntity.ok(UserResponseDto.fromUser(user));
    }

    /**
     * 获取当前用户个人信息
     * 通过JWT Token获取当前登录用户的个人信息
     * 
     * @param authentication Spring Security提供的认证信息
     * @return 当前用户的个人信息
     */
    @Operation(
        summary = "获取个人信息",
        description = "获取当前登录用户的个人详细信息",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "获取成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "未授权或Token无效",
            content = @Content(schema = @Schema(type = "string"))
        )
    })
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponseDto> getProfile(
            @Parameter(hidden = true) Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        User currentUser = userService.findById(user.getId());
        return ResponseEntity.ok(UserResponseDto.fromUser(currentUser));
    }

    /**
     * 更新个人信息
     * 用户可以更新自己的姓名、邮箱、个人简介等信息
     * 
     * @param updateDto 需要更新的用户信息
     * @param authentication Spring Security提供的认证信息
     * @return 更新后的用户信息
     */
    @Operation(
        summary = "更新个人信息",
        description = "用户更新自己的个人信息，如姓名、邮箱、个人简介等",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "更新成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "请求参数错误",
            content = @Content(schema = @Schema(type = "string"))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "未授权或Token无效",
            content = @Content(schema = @Schema(type = "string"))
        )
    })
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponseDto> updateProfile(
            @Parameter(description = "更新的用户信息", required = true)
            @Valid @RequestBody UserUpdateDto updateDto,
            @Parameter(hidden = true) Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        User updatedUser = userService.updateUser(user.getId(), updateDto);
        return ResponseEntity.ok(UserResponseDto.fromUser(updatedUser));
    }

    /**
     * 通过ID获取用户信息
     * 仅限管理员使用，可以查询任意用户的详细信息
     * 
     * @param id 用户ID
     * @return 指定用户的详细信息
     */
    @Operation(
        summary = "获取指定用户信息",
        description = "管理员通过用户ID查询指定用户的详细信息",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "查询成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "权限不足，仅管理员可访问",
            content = @Content(schema = @Schema(type = "string"))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "用户不存在",
            content = @Content(schema = @Schema(type = "string"))
        )
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> getUserById(
            @Parameter(description = "用户ID", required = true, example = "1")
            @PathVariable Long id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(UserResponseDto.fromUser(user));
    }

    /**
     * 获取所有用户列表
     * 仅限管理员使用，支持分页查询所有用户信息
     * 
     * @param pageable 分页参数，包含页码、每页大小、排序等
     * @return 分页的用户信息列表
     */
    @Operation(
        summary = "获取所有用户",
        description = "管理员分页查询所有用户信息，支持排序和分页",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "查询成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Page.class)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "权限不足，仅管理员可访问",
            content = @Content(schema = @Schema(type = "string"))
        )
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(
            @Parameter(description = "分页参数") Pageable pageable) {
        Page<User> users = userService.findAllUsers(pageable);
        Page<UserResponseDto> userDtos = users.map(UserResponseDto::fromUser);
        return ResponseEntity.ok(userDtos);
    }

    /**
     * 删除用户
     * 仅限管理员使用，删除指定的用户账号
     * 
     * @param id 要删除的用户ID
     * @return 无内容返回，仅返回204状态码
     */
    @Operation(
        summary = "删除用户",
        description = "管理员删除指定的用户账号，该操作不可逆转",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "删除成功",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403",
            description = "权限不足，仅管理员可访问",
            content = @Content(schema = @Schema(type = "string"))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "用户不存在",
            content = @Content(schema = @Schema(type = "string"))
        )
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "要删除的用户ID", required = true, example = "1")
            @PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 检查用户名是否已存在
     * 公开接口，用于注册时实时检查用户名是否可用
     * 
     * @param username 要检查的用户名
     * @return true表示用户名已存在，false表示可用
     */
    @Operation(
        summary = "检查用户名是否存在",
        description = "检查指定的用户名是否已经被使用，用于注册时的实时验证"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "检查成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = "boolean", description = "true:已存在, false:可用")
            )
        )
    })
    @GetMapping("/check-username/{username}")
    public ResponseEntity<Boolean> checkUsername(
            @Parameter(description = "要检查的用户名", required = true, example = "john_doe")
            @PathVariable String username) {
        boolean exists = userService.existsByUsername(username);
        return ResponseEntity.ok(exists);
    }

    /**
     * 检查邮箱是否已存在
     * 公开接口，用于注册时实时检查邮箱是否可用
     * 
     * @param email 要检查的邮箱地址
     * @return true表示邮箱已存在，false表示可用
     */
    @Operation(
        summary = "检查邮箱是否存在",
        description = "检查指定的邮箱地址是否已经被使用，用于注册时的实时验证"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "检查成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = "boolean", description = "true:已存在, false:可用")
            )
        )
    })
    @GetMapping("/check-email/{email}")
    public ResponseEntity<Boolean> checkEmail(
            @Parameter(description = "要检查的邮箱地址", required = true, example = "user@example.com")
            @PathVariable String email) {
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }
}