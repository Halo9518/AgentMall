package com.agentmall.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 当前登录用户注解
 * <p>
 * 在 Controller 方法参数上使用，自动注入当前登录的 User 实体。
 *
 * <pre>
 * public Result&lt;UserVO&gt; info(@CurrentUser User user) { ... }
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUser {
}
