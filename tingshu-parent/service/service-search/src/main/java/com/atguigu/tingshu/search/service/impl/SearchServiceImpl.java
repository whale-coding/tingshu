package com.atguigu.tingshu.search.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.RandomUtil;
import com.atguigu.tingshu.album.AlbumFeignClient;
import com.atguigu.tingshu.model.album.AlbumAttributeValue;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.BaseCategoryView;
import com.atguigu.tingshu.model.search.AlbumInfoIndex;
import com.atguigu.tingshu.model.search.AttributeValueIndex;
import com.atguigu.tingshu.search.repository.AlbumInfoIndexRepository;
import com.atguigu.tingshu.search.service.SearchService;
import com.atguigu.tingshu.user.client.UserFeignClient;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;


@Slf4j
@Service
@SuppressWarnings({"all"})
public class SearchServiceImpl implements SearchService {
    @Autowired
    private AlbumInfoIndexRepository repository;

    @Autowired
    private AlbumFeignClient albumFeignClient;

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private ThreadPoolExecutor executor;

    /**
     * 上架专辑-导入索引库（优化版）
     * @param albumId
     */
    @Override
    public void upperAlbum(Long albumId) {
        // 创建封装数据实体
        AlbumInfoIndex albumInfoIndex =new AlbumInfoIndex();

        // 创建异步对象-封装专辑信息
        CompletableFuture<AlbumInfo> albumInfoCompletableFuture = CompletableFuture.supplyAsync(() -> {
            // 根据专辑ID查询专辑信息
            AlbumInfo albumInfo = albumFeignClient.getAlbumInfo(albumId).getData();
            // 判断
            Assert.notNull(albumInfo, "专辑不存在，专辑ID{}", albumId);
            // 封装数据
            BeanUtils.copyProperties(albumInfo, albumInfoIndex);
            // 封装数据
            // 设置上架时间
            albumInfoIndex.setCreateTime(new Date());
            // 封装专辑属性信息
            List<AlbumAttributeValue> attributeValueVoList = albumInfo.getAlbumAttributeValueVoList();
            // 判断
            if(CollectionUtil.isNotEmpty(attributeValueVoList)){
                // 转换数据
                List<AttributeValueIndex> attributeValueIndexList = attributeValueVoList.stream().map(albumAttributeValue -> {
                    // 创建属性对象
                    return BeanUtil.copyProperties(albumAttributeValue, AttributeValueIndex.class);
                }).collect(Collectors.toList());

                // 设置专辑属性集合
                albumInfoIndex.setAttributeValueIndexList(attributeValueIndexList);
            }
            return albumInfo;

        },executor);

        // 创建异步对象-封装主播信息
        CompletableFuture<Void> userInfoCompletableFuture = albumInfoCompletableFuture.thenAcceptAsync(albumInfo -> {
            // 设置主播名称
            UserInfoVo userInfoVo = userFeignClient.getUserInfoVo(albumInfo.getUserId()).getData();
            // 判断
            Assert.notNull(userInfoVo, "用户查询异常，用户ID{}", albumInfo.getUserId());

            albumInfoIndex.setAnnouncerName(userInfoVo.getNickname());

        },executor);

        // 创建异步对象-封装分类信息
        CompletableFuture<Void> categoryCompletableFuture = albumInfoCompletableFuture.thenAcceptAsync(albumInfo -> {
            // 设置三级分类
            BaseCategoryView baseCategoryView = albumFeignClient.getCategoryView(albumInfo.getCategory3Id()).getData();
            // 判断
            Assert.notNull(baseCategoryView, "分页查询异常，分类的ID{}", albumInfo.getCategory3Id());

            // 设置一二三级ID
            albumInfoIndex.setCategory1Id(baseCategoryView.getCategory1Id());
            albumInfoIndex.setCategory2Id(baseCategoryView.getCategory2Id());

        },executor);

        // 创建异步对象-封装统计信息
        CompletableFuture<Void> statCompletableFuture = CompletableFuture.runAsync(() -> {
            // 设置统计数据--随机
            int num1 = RandomUtil.randomInt(1000, 2000);
            int num2 = RandomUtil.randomInt(500, 1000);
            int num3 = RandomUtil.randomInt(200, 400);
            int num4 = RandomUtil.randomInt(100, 200);
            // 播放量
            albumInfoIndex.setPlayStatNum(num1);
            // 订阅
            albumInfoIndex.setSubscribeStatNum(num2);
            // 购买量
            albumInfoIndex.setBuyStatNum(num3);
            // 评论数
            albumInfoIndex.setCommentStatNum(num4);

            // 基于统计值计算出专辑得分 为不同统计类型设置不同权重
            BigDecimal bigDecimal1 = new BigDecimal(num4).multiply(new BigDecimal("0.4"));
            BigDecimal bigDecimal2 = new BigDecimal(num3).multiply(new BigDecimal("0.3"));
            BigDecimal bigDecimal3 = new BigDecimal(num2).multiply(new BigDecimal("0.2"));
            BigDecimal bigDecimal4 = new BigDecimal(num1).multiply(new BigDecimal("0.1"));
            BigDecimal hotScore = bigDecimal1.add(bigDecimal2).add(bigDecimal3).add(bigDecimal4);
            albumInfoIndex.setHotScore(hotScore.doubleValue());

        },executor);

        // 异步任务编排
        CompletableFuture.allOf(
                albumInfoCompletableFuture,
                userInfoCompletableFuture,
                categoryCompletableFuture,
                statCompletableFuture
        ).join();

        // 保存
        repository.save(albumInfoIndex);
    }

    /*
     // 上架专辑-导入索引库(基础版本！)
    @Override
    public void upperAlbum(Long albumId) {
        // 创建封装数据实体
        // 根据专辑ID查询专辑信息
        AlbumInfo albumInfo = albumFeignClient.getAlbumInfo(albumId).getData();
        // 判断
        Assert.notNull(albumInfo, "专辑不存在，专辑ID{}", albumId);
        // 封装专辑数据
        AlbumInfoIndex albumInfoIndex = BeanUtil.copyProperties(albumInfo, AlbumInfoIndex.class);

        // 封装数据
        // 设置上架时间
        albumInfoIndex.setCreateTime(new Date());

        // 封装专辑属性信息
        List<AlbumAttributeValue> attributeValueVoList = albumInfo.getAlbumAttributeValueVoList();
        // 判断
        if(CollectionUtil.isNotEmpty(attributeValueVoList)){
            // 转换数据
            List<AttributeValueIndex> attributeValueIndexList = attributeValueVoList.stream().map(albumAttributeValue -> {
                // 创建属性对象
                return BeanUtil.copyProperties(albumAttributeValue, AttributeValueIndex.class);
            }).collect(Collectors.toList());

            // 设置专辑属性集合
            albumInfoIndex.setAttributeValueIndexList(attributeValueIndexList);
        }

        // 设置主播名称
        UserInfoVo userInfoVo = userFeignClient.getUserInfoVo(albumInfo.getUserId()).getData();
        // 判断
        Assert.notNull(userInfoVo,"用户查询异常，用户ID{}",albumInfo.getUserId());

        albumInfoIndex.setAnnouncerName(userInfoVo.getNickname());

        // 设置三级分裂
        BaseCategoryView baseCategoryView = albumFeignClient.getCategoryView(albumInfo.getCategory3Id()).getData();
        // 判断
        Assert.notNull(baseCategoryView,"分页查询异常，分类的ID{}",albumInfo.getCategory3Id());

        // 设置一二三级ID
        albumInfoIndex.setCategory1Id(baseCategoryView.getCategory1Id());
        albumInfoIndex.setCategory2Id(baseCategoryView.getCategory2Id());

        // 设置统计数据--随机
        int num1 = RandomUtil.randomInt(1000, 2000);
        int num2 = RandomUtil.randomInt(500, 1000);
        int num3 = RandomUtil.randomInt(200, 400);
        int num4 = RandomUtil.randomInt(100, 200);
        // 播放量
        albumInfoIndex.setPlayStatNum(num1);
        // 订阅
        albumInfoIndex.setSubscribeStatNum(num2);
        // 购买量
        albumInfoIndex.setBuyStatNum(num3);
        // 评论数
        albumInfoIndex.setCommentStatNum(num4);

        // 基于统计值计算出专辑得分 为不同统计类型设置不同权重
        BigDecimal bigDecimal1 = new BigDecimal(num4).multiply(new BigDecimal("0.4"));
        BigDecimal bigDecimal2 = new BigDecimal(num3).multiply(new BigDecimal("0.3"));
        BigDecimal bigDecimal3 = new BigDecimal(num2).multiply(new BigDecimal("0.2"));
        BigDecimal bigDecimal4 = new BigDecimal(num1).multiply(new BigDecimal("0.1"));
        BigDecimal hotScore = bigDecimal1.add(bigDecimal2).add(bigDecimal3).add(bigDecimal4);
        albumInfoIndex.setHotScore(hotScore.doubleValue());

        repository.save(albumInfoIndex);
    }
    */

    /**
     * 下架专辑-删除文档
     * @param albumId
     */
    @Override
    public void lowerAlbum(Long albumId) {
        repository.deleteById(albumId);
    }
}
