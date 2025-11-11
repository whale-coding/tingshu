package com.atguigu.tingshu.common.login;

import java.lang.annotation.*;


/**
 * 自定义登录拦截注解
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface GuiguLogin {
    
    /**
     * 是否需要拦截
     * @return true or false
     *        true:认证拦截
     *        false:认证不拦截
     */
    boolean required() default true;
}
