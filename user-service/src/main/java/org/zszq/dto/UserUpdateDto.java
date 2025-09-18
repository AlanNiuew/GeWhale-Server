package org.zszq.dto;

import lombok.Data;

@Data
public class UserUpdateDto {

    private String nickname;
    private String avatarUrl;
    private String phone;
}