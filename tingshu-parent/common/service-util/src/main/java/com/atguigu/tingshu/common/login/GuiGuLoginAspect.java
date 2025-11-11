package com.atguigu.tingshu.common.login;

import com.atguigu.tingshu.common.constant.RedisConstant;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.result.ResultCodeEnum;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.vo.album.AlbumListVo;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 自定义登录拦截注解 的切面类
 */
@Slf4j
@Aspect  // 切面类注解
@Component
public class GuiGuLoginAspect {
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 用户登录拦截实现
     *
     * @param joinPoint
     * @param guiguLogin
     * @return
     * @throws Throwable execution(* com.atguigu.tingshu.*.api.*.*(..))&& @annotation(guiguLogin)
     *                   * com.atguigu.tingshu.*.api.*.*(..))
     *                   返回值  包名.类名.方法（参数列表） 并且 添加了指定注解
     */
    @Around("execution(* com.atguigu.tingshu.*.api.*.*(..))&& @annotation(guiguLogin)")  // 环绕通知
    public Object doConcurrentOperation(ProceedingJoinPoint joinPoint, GuiguLogin guiguLogin) throws Throwable {
        // 定义返回值结果
        Object object = new Object();

        // 通过请求上下文对象，获取请求
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        // 判断
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
        // 获取请求对象
        HttpServletRequest request = servletRequestAttributes.getRequest();

        // 获取token
        String token = request.getHeader("token");

        // 定义用户登录存储key
        String loginKey= RedisConstant.USER_LOGIN_KEY_PREFIX+token;
        // 尝试从redis中获取用户信息
        UserInfoVo userInfo = (UserInfoVo) redisTemplate.opsForValue().get(loginKey);
        // 认证且拦截
        if(guiguLogin.required()&&userInfo==null){
            // 返回状态码为208，前端会跳转到登录页面
            throw new GuiguException(ResultCodeEnum.LOGIN_AUTH);
        }

        // 存储用户ID
        if(userInfo!=null){
            // 设置userId,在业务中可以随时获取使用
            AuthContextHolder.setUserId(userInfo.getId());
        }

        // 执行目标方法
        object= joinPoint.proceed();

        // 清楚线程标量中的userId,避免OOM  防止用户信息泄露
        AuthContextHolder.removeUserId();

        return object;
    }
}



