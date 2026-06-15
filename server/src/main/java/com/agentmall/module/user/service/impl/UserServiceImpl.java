package com.agentmall.module.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.agentmall.common.exception.BusinessException;
import com.agentmall.module.user.entity.User;
import com.agentmall.module.user.mapper.UserMapper;
import com.agentmall.module.user.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户 Service 实现
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final BCryptPasswordEncoder passwordEncoder;

    public UserServiceImpl(BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User getByPhoneAndRole(String phone, String role) {
        // uk_phone_role 唯一约束保证单条结果，getOne 安全
        return getOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, phone)
                .eq(User::getRole, role));
    }

    @Override
    @Transactional
    public User register(String phone, String password, String nickname, String role) {
        // 校验手机号格式
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException("手机号格式不正确");
        }
        // 校验密码长度
        if (password.length() < 6 || password.length() > 20) {
            throw new BusinessException("密码长度需为6-20位");
        }
        // 校验角色
        if (!"CUSTOMER".equals(role) && !"MERCHANT".equals(role)) {
            throw new BusinessException("角色类型无效");
        }
        // 检查是否已注册
        User exist = getByPhoneAndRole(phone, role);
        if (exist != null) {
            throw new BusinessException("该手机号已注册");
        }

        User user = new User();
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(StrUtil.isNotBlank(nickname) ? nickname : "用户" + phone.substring(7));
        user.setRole(role);
        user.setStatus(1);
        save(user);
        return user;
    }

    @Override
    public User login(String phone, String password, String role) {
        User user = getByPhoneAndRole(phone, role);
        if (user == null) {
            throw new BusinessException("账号不存在");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("密码错误");
        }
        return user;
    }

    @Override
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        if (newPassword.length() < 6 || newPassword.length() > 20) {
            throw new BusinessException("新密码长度需为6-20位");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        updateById(user);
    }

    @Override
    public void updateInfo(Long userId, String nickname, String avatar) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (StrUtil.isNotBlank(nickname)) {
            user.setNickname(nickname);
        }
        if (StrUtil.isNotBlank(avatar)) {
            user.setAvatar(avatar);
        }
        updateById(user);
    }
}
