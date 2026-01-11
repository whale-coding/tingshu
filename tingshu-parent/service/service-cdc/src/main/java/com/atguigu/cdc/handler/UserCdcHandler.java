package com.atguigu.cdc.handler;

import com.atguigu.cdc.model.CDCEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import top.javatool.canal.client.annotation.CanalTable;
import top.javatool.canal.client.handler.EntryHandler;

@Component
@CanalTable("user_info") // 监听变更表
public class UserCdcHandler implements EntryHandler<CDCEntity> {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     *  监听修改后的数据
     * @param before
     * @param after
     */
    @Override
    public void update(CDCEntity before, CDCEntity after) {

        System.out.println("变更的数据："+after.getId());
        redisTemplate.delete("userInfoVo:"+after.getId());

    }

    /**
     * 监听删除的数据
     * @param cdcEntity
     */
    @Override
    public void delete(CDCEntity cdcEntity) {
        System.out.println("变更的数据："+cdcEntity.getId());
        redisTemplate.delete("userInfoVo:"+cdcEntity.getId());
    }
}