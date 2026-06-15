package com.agentmall.module.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 修改个人信息请求
 */
@Schema(description = "修改个人信息请求")
public class UpdateUserDTO {

    @Schema(description = "昵称", example = "新昵称")
    private String nickname;

    @Schema(description = "头像URL")
    private String avatar;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
