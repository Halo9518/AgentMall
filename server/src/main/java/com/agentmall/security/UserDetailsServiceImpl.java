package com.agentmall.security;

import com.agentmall.module.user.entity.User;
import com.agentmall.module.user.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Spring Security UserDetailsService 实现
 * <p>
 * 根据 userId 从数据库加载用户信息，构建 SecurityContext 中的认证主体。
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;

    public UserDetailsServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getId, Long.valueOf(userId)));
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + userId);
        }

        // 用户状态为 0（禁用）时，禁止登录
        boolean enabled = user.getStatus() != null && user.getStatus() == 1;
        String role = "ROLE_" + user.getRole();

        return new org.springframework.security.core.userdetails.User(
                String.valueOf(user.getId()),
                user.getPassword(),
                enabled,
                true,  // accountNonExpired
                true,  // credentialsNonExpired
                true,  // accountNonLocked
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }
}
