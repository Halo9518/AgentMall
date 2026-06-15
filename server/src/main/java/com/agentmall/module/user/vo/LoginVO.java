package com.agentmall.module.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 登录响应
 */
@Schema(description = "登录响应")
public class LoginVO {

    @Schema(description = "JWT令牌")
    private String token;

    @Schema(description = "用户信息")
    private UserVO user;

    public static LoginVO of(String token, UserVO user) {
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUser(user);
        return vo;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserVO getUser() {
        return user;
    }

    public void setUser(UserVO user) {
        this.user = user;
    }
}
