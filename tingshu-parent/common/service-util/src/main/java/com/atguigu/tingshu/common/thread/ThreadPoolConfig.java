package com.atguigu.tingshu.common.thread;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

/**
 * 自定义线程池配置类
 */
@Configuration
public class ThreadPoolConfig {

    @Bean
    public ThreadPoolExecutor executor(){
        // 核心线程数的取值--IO密集型  cpu逻辑核数*2
        // 获取系统的cpu逻辑核数
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        // 计算核心线程数
        int coreCount=availableProcessors*2;

        // 创建线程池
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                coreCount,  // 核心线程数
                coreCount,  // 最大线程数
                0,          // 非核心线程存活时间
                TimeUnit.SECONDS,  // 存活时间单位
                new ArrayBlockingQueue<>(200),  // 阻塞队列
                Executors.defaultThreadFactory(),  // 线程工厂
                new RejectedExecutionHandler() {   // 拒绝策略（自定义）
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                        //睡眠1秒
                        try {
                            Thread.sleep(1000);
                            //再次执行
                            executor.submit( r);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
        );

        // 在线程池初始化时先创建一个线程
        executor.prestartCoreThread();

        // 返回自定义的线程数
        return executor;
    }
}
