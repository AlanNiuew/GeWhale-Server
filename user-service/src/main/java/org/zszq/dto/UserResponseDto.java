package org.zszq.dto;

import lombok.Data;
import org.zszq.entity.User;

import java.time.LocalDateTime;

@Data
public class UserResponseDto {

    private Long id;
    private String username;
    private String email;
    private String nickname;
    private String avatarUrl;
    private String phone;
    private User.UserRole role;
    private User.UserStatus status;
    private LocalDateTime lastLoginTime;
    private LocalDateTime createdAt;

    public static UserResponseDto fromUser(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setNickname(user.getNickname());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setLastLoginTime(user.getLastLoginTime());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}