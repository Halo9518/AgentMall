package com.agentmall.module.user.service;

import com.agentmall.module.user.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 用户 Service
 */
public interface UserService extends IService<User> {

    /**
     * 根据手机号和角色查询用户
     */
    User getByPhoneAndRole(String phone, String role);

    /**
     * 注册
     */
    User register(String phone, String password, String nickname, String role);

    /**
     * 登录（校验密码），返回用户
     */
    User login(String phone, String password, String role);

    /**
     * 修改密码
     */
    void updatePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 修改个人信息
     */
    void updateInfo(Long userId, String nickname, String avatar);
}
