package com.agentmall.module.user.controller;

import com.agentmall.common.Result;
import com.agentmall.module.user.dto.LoginDTO;
import com.agentmall.module.user.dto.RegisterDTO;
import com.agentmall.module.user.dto.UpdatePasswordDTO;
import com.agentmall.module.user.dto.UpdateUserDTO;
import com.agentmall.module.user.entity.User;
import com.agentmall.module.user.service.UserService;
import com.agentmall.module.user.vo.LoginVO;
import com.agentmall.module.user.vo.UserVO;
import com.agentmall.security.CurrentUser;
import com.agentmall.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器
 */
@Tag(name = "认证", description = "注册、登录、个人信息")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(UserService userService, JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Operation(summary = "注册")
    @PostMapping("/register")
    public Result<LoginVO> register(@Valid @RequestBody RegisterDTO dto) {
        User user = userService.register(dto.getPhone(), dto.getPassword(), dto.getNickname(), dto.getRole());
        String token = jwtTokenProvider.generateToken(user.getId(), user.getPhone(), user.getRole());
        return Result.success(LoginVO.of(token, UserVO.fromEntity(user)));
    }

    @Operation(summary = "登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        User user = userService.login(dto.getPhone(), dto.getPassword(), dto.getRole());
        String token = jwtTokenProvider.generateToken(user.getId(), user.getPhone(), user.getRole());
        return Result.success(LoginVO.of(token, UserVO.fromEntity(user)));
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public Result<UserVO> info(@CurrentUser User user) {
        return Result.success(UserVO.fromEntity(user));
    }

    @Operation(summary = "修改个人信息")
    @PutMapping("/info")
    public Result<Void> updateInfo(@CurrentUser User user, @Valid @RequestBody UpdateUserDTO dto) {
        userService.updateInfo(user.getId(), dto.getNickname(), dto.getAvatar());
        return Result.success();
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> updatePassword(@CurrentUser User user, @Valid @RequestBody UpdatePasswordDTO dto) {
        userService.updatePassword(user.getId(), dto.getOldPassword(), dto.getNewPassword());
        return Result.success();
    }
}
