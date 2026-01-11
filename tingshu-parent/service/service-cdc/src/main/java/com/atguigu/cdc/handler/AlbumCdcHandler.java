package com.atguigu.cdc.handler;

import com.atguigu.cdc.model.AlbumCdcEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import top.javatool.canal.client.annotation.CanalTable;
import top.javatool.canal.client.handler.EntryHandler;

@Component
@CanalTable("album_info")
public class AlbumCdcHandler implements EntryHandler<AlbumCdcEntity> {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 修改
     * @param before
     * @param after
     */
    @Override
    public void update(AlbumCdcEntity before, AlbumCdcEntity after) {

        System.out.println("专辑id："+after.getId());
        System.out.println("专辑标题："+after.getAlbumTitle());

        redisTemplate.delete("albumInfo:"+after.getId());
    }

    /**
     * 删除
     * @param albumCdcEntity
     */
    @Override
    public void delete(AlbumCdcEntity albumCdcEntity) {

        System.out.println("删除专辑");
        redisTemplate.delete("albumInfo:"+albumCdcEntity.getId());
    }
}