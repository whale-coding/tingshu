package com.atguigu.tingshu;

import com.atguigu.tingshu.common.constant.RedisConstant;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class ServiceAlbumApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(ServiceAlbumApplication.class, args);
    }

    @Autowired
    private RedissonClient redissonClient;

    /**
     * CommandLineRunner 在springBoot工程启动之后，自动触发一次
     * @param args
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {
        // 获取布隆过滤器
        RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter(RedisConstant.ALBUM_BLOOM_FILTER);
        // 初始化
        bloomFilter.tryInit(100000L,0.01);
    }
}
