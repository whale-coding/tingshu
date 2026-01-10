package com.atguigu.tingshu.common.thread;

import com.atguigu.tingshu.common.zipkinConfig.ZipkinHelper;
import com.atguigu.tingshu.common.zipkinConfig.ZipkinTaskDecorator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.*;

/**
 * 自定义线程池配置类
 */
@Configuration
public class ThreadPoolConfig {

    @Autowired
    private ZipkinHelper zipkinHelper;


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


    /**
     * 项目中选择spring线程池
     * Spring提供线程池threadPoolTaskExecutor
     *
     * @return
     */
    @Bean
    public Executor threadPoolTaskExecutor() {
        int count = Runtime.getRuntime().availableProcessors();
        int threadCount = count*2+1;
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        // 核心池大小
        taskExecutor.setCorePoolSize(threadCount);
        // 最大线程数
        taskExecutor.setMaxPoolSize(threadCount);
        // 队列程度
        taskExecutor.setQueueCapacity(300);
        // 线程空闲时间
        taskExecutor.setKeepAliveSeconds(0);
        // 线程前缀名称
        taskExecutor.setThreadNamePrefix("sync-tingshu-Executor--");
        // 该方法用来设置 线程池关闭 的时候 等待 所有任务都完成后，再继续 销毁 其他的 Bean，
        // 这样这些 异步任务 的 销毁 就会先于 数据库连接池对象 的销毁。
        taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        // 任务的等待时间 如果超过这个时间还没有销毁就 强制销毁，以确保应用最后能够被关闭，而不是阻塞住。
        taskExecutor.setAwaitTerminationSeconds(300);
        // 设置解决zipkin 链路追踪不完整装饰器对象
        taskExecutor.setTaskDecorator(new ZipkinTaskDecorator(zipkinHelper));
        // 线程不够用时由调用的线程处理该任务
        taskExecutor.setRejectedExecutionHandler((task, executor)->{
            // 将拒绝任务再次提交给线程池执行
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            executor.submit(task);
        });
        return taskExecutor;
    }
}
