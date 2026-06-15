package com.agentmall.security;

import com.agentmall.common.exception.BusinessException;
import com.agentmall.module.user.entity.User;
import com.agentmall.module.user.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * @CurrentUser 注解解析器
 * <p>
 * 从 SecurityContext 获取当前登录用户ID，查询数据库返回完整 User 对象。
 */
@Component
public class CurrentUserMethodArgumentResolver implements HandlerMethodArgumentResolver {

    private final UserMapper userMapper;

    public CurrentUserMethodArgumentResolver(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && parameter.getParameterType().equals(User.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(401, "未登录");
        }

        Long userId = Long.valueOf(authentication.getName());
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getId, userId));
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }
        return user;
    }
}
