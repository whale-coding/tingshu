package com.atguigu.tingshu;

import com.atguigu.tingshu.common.constant.RedisConstant;
import com.atguigu.tingshu.search.service.SearchService;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 专辑上架全量导入
 */
@SpringBootTest
public class BatchImportTest {

    @Autowired
    private SearchService searchService;

    @Autowired
    private RedissonClient redissonClient;

    @Test
    public void importAlbum(){
        for (int i = 0; i < 1602; i++) {
            try {
                searchService.upperAlbum(i+ 1L);
                System.out.println("导入专辑ID"+(i+1));
            } catch (Exception e) {
                continue;
            }
        }
    }

    /**
     * 专辑ID 导入布隆过滤器
     */
    @Test
    public void importAlbumId2bloom(){
        for (int i = 0; i < 1603; i++) {
            RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConstant.ALBUM_BLOOM_FILTER);
            bloomFilter.add((long) i);
            System.out.println("导入布隆过滤器专辑 ID"+i);
        }
    }
}
