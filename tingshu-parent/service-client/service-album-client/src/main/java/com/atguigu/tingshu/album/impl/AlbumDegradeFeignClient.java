package com.atguigu.tingshu.album.impl;


import com.atguigu.tingshu.album.AlbumFeignClient;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.BaseCategory3;
import com.atguigu.tingshu.model.album.BaseCategoryView;
import com.atguigu.tingshu.vo.album.AlbumStatVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class AlbumDegradeFeignClient implements AlbumFeignClient {

    /**
     *  根据ID查询专辑信息
     * @param id
     * @return
     */
    @Override
    public Result<AlbumInfo> getAlbumInfo(Long id) {
        log.error("[专辑模块]提供远程调用getAlbumInfo服务降级");
        return Result.fail();
    }

    /**
     * 根据三级分类Id 获取到分类信息
     * @param category3Id
     * @return
     */
    @Override
    public Result<BaseCategoryView> getCategoryView(Long category3Id) {
        log.error("[专辑模块]提供远程调用getCategoryView服务降级");
        return Result.fail();
    }

    /**
     * 根据一级分类Id查询三级分类列表
     * @param category1Id
     * @return
     */
    @Override
    public Result<List<BaseCategory3>> findTopBaseCategory3(Long category1Id) {
        log.error("[专辑模块]提供远程调用findTopBaseCategory3服务降级");
        return Result.fail();
    }

    /**
     * 根据专辑ID获取专辑统计信息
     * @param albumId
     * @return
     */
    @Override
    public Result<AlbumStatVo> getAlbumStatVo(Long albumId) {
        log.error("[专辑模块]提供远程调用getAlbumStatVo服务降级");
        return Result.fail();
    }
}
