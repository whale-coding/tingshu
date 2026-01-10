package com.atguigu.tingshu.common.cache;

import java.lang.annotation.*;

/**
 * 自定义缓存注解
 */
@Target({ ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface GuiGuCache {
    /**
     * 缓存key 的前缀
     */
    String prefix() default "cache:";

    /**
     * 缓存key 的后缀
     */
    String suffix() default ":data";
}