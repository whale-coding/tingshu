package com.atguigu.tingshu.common.cache;

import com.atguigu.tingshu.common.constant.RedisConstant;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 自定义缓存注解 切面处理类
 */
@Aspect
@Component
@Slf4j
public class GuiGuCacheAspect {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 分布式锁+缓存业务实现
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("@annotation(guiGuCache)")
    public Object doConcurrentOperation(ProceedingJoinPoint joinPoint,GuiGuCache guiGuCache) throws Throwable{
        Object obj = null;
        try {
            // 获取前缀
            String prefix = guiGuCache.prefix();
            System.out.println("获取的前缀："+prefix);
            // 获取目标方法的参数列表
            Object[] args = joinPoint.getArgs();
            // 获取当前切点的返回值类型
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Class returnType = signature.getReturnType();
            // 定义变量，接收参数
            String paramVal ="none";
            // 判断  12  89     --> 12:89
            if(args!=null){
                paramVal = Arrays.stream(args).map(arg -> arg.toString()).collect(Collectors.joining(":"));
            }

            // 定义数据存储 key
            String dataKey=prefix+paramVal;
            // 先从缓存中尝试获取数据
            obj = redisTemplate.opsForValue().get(dataKey);
            // 判断
            if(obj!=null){
                log.info("从缓存获取数据");
                return obj;
            }
            // 定义锁 key
            String lockKey=prefix+paramVal+ RedisConstant.CACHE_LOCK_SUFFIX;
            // 尝试获取锁
            RLock lock = redissonClient.getLock(lockKey);
            // 加锁
            lock.lock();
            try {
                // 二次查询缓存
                obj = redisTemplate.opsForValue().get(dataKey);
                // 判断
                if(obj!=null){
                    log.info("二次从缓存获取数据");
                    return obj;
                }
                // 查询数据库
                obj = joinPoint.proceed();
                // 判断是否从数据库中获取数据
                if(obj==null){
                    // 反射创建具体的类型
                    obj=returnType.newInstance();
                    // 存储指定对象的空对象
                    redisTemplate.opsForValue().set(dataKey,obj,RedisConstant.ALBUM_TEMPORARY_TIMEOUT, TimeUnit.SECONDS);
                    return obj;
                }else{
                    // 从数据库中查询到数据
                    redisTemplate.opsForValue().set(dataKey,obj,RedisConstant.ALBUM_TIMEOUT, TimeUnit.SECONDS);
                    return obj;
                }
            } finally {
                lock.unlock();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        // 兜底方法
        return joinPoint.proceed();
    }
}
