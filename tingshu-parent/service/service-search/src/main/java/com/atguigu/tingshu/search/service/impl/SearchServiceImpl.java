package com.atguigu.tingshu.search.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.RandomUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.alibaba.nacos.common.utils.StringUtils;
import com.atguigu.tingshu.album.AlbumFeignClient;
import com.atguigu.tingshu.model.album.AlbumAttributeValue;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.BaseCategoryView;
import com.atguigu.tingshu.model.search.AlbumInfoIndex;
import com.atguigu.tingshu.model.search.AttributeValueIndex;
import com.atguigu.tingshu.query.search.AlbumIndexQuery;
import com.atguigu.tingshu.search.repository.AlbumInfoIndexRepository;
import com.atguigu.tingshu.search.service.SearchService;
import com.atguigu.tingshu.user.client.UserFeignClient;
import com.atguigu.tingshu.vo.search.AlbumInfoIndexVo;
import com.atguigu.tingshu.vo.search.AlbumSearchResponseVo;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;


@Slf4j
@Service
@SuppressWarnings({"all"})
public class SearchServiceImpl implements SearchService {
    // 索引名称常量
    private static final String INDEX_NAME = "albuminfo";

    @Autowired
    private AlbumInfoIndexRepository repository;

    @Autowired
    private AlbumFeignClient albumFeignClient;

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private ThreadPoolExecutor executor;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

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

    /**
     * 专辑检索
     * @param albumIndexQuery
     * @return
     */
    @SneakyThrows
    @Override
    public AlbumSearchResponseVo search(AlbumIndexQuery albumIndexQuery) {
        // 构建请求对象
        SearchRequest searchRequest = this.buildDSL(albumIndexQuery);

        String string = searchRequest.toString();
        System.out.println("生成的DSL语句"+string);
        log.info("生成的DSL语句:{}",searchRequest.toString());

        // 执行查询
        SearchResponse<AlbumInfoIndex> searchResponse = elasticsearchClient.search(searchRequest, AlbumInfoIndex.class);

        // 转换查询结果
        AlbumSearchResponseVo albumSearchResponseVo = this.parseResult(searchResponse,albumIndexQuery);

        // 返回结果
        return albumSearchResponseVo;
    }

    /**
     * 构建DSL数据，返回请求对象
     *
     * @param albumIndexQuery
     * @return
     */
    private SearchRequest buildDSL(AlbumIndexQuery albumIndexQuery) {
        // 创建请求对象
        SearchRequest.Builder builder = new SearchRequest.Builder();
        // 指定查询的索引
        builder.index(INDEX_NAME);

        // 创建多条件对象
        BoolQuery.Builder allBoolQuery = new BoolQuery.Builder();

        // 封装关键字查询
        String keyword = albumIndexQuery.getKeyword();
        // 判断
        if (StringUtils.isNotEmpty(keyword)) {
            // 创建关键字Bool对象
            BoolQuery.Builder keywordBuild = new BoolQuery.Builder();
            // 设置标题匹配查询
            keywordBuild.should(s -> s.match(m -> m.field("albumTitle").query(keyword)));
            // 设置简介匹配查询
            keywordBuild.should(s -> s.match(m -> m.field("albumIntro").query(keyword)));
            // 设置主播匹配查询
            keywordBuild.should(s -> s.term(t -> t.field("announcerName").value(keyword)));

            // 添加条件到最外出的bool对象
            allBoolQuery.must(keywordBuild.build()._toQuery());
        }
        // 封装三级分类
        if (albumIndexQuery.getCategory1Id() != null) {
            allBoolQuery.filter(f -> f.term(t -> t.field("category1Id").value(albumIndexQuery.getCategory1Id())));
        }
        if (albumIndexQuery.getCategory2Id() != null) {
            allBoolQuery.filter(f -> f.term(t -> t.field("category2Id").value(albumIndexQuery.getCategory2Id())));
        }
        if (albumIndexQuery.getCategory3Id() != null) {
            allBoolQuery.filter(f -> f.term(t -> t.field("category3Id").value(albumIndexQuery.getCategory3Id())));
        }

        // 专辑属性设置---nested类型
        List<String> attributeList = albumIndexQuery.getAttributeList();
        // 判断
        if (CollectionUtil.isNotEmpty(attributeList)) {
            // 遍历处理
            for (String attr : attributeList) {
                // attr=属性id:属性值id
                // 截取数据
                String[] split = attr.split(":");
                // 判断
                if (split != null && split.length == 2) {
                    // 获取属性ID
                    String attrbuteId = split[0];
                    // 获取属性值
                    String valueId = split[1];

                    allBoolQuery.filter(f -> f.nested(n -> n.path("attributeValueIndexList").
                            query(q -> q.bool(b -> b.
                                    filter(fi -> fi.term(t -> t.field("attributeValueIndexList.attributeId").value(attrbuteId))).
                                    filter(fil -> fil.term(te -> te.field("attributeValueIndexList.valueId").value(valueId)))))));
                }
            }
        }
        // 设置查询条件query
        builder.query(allBoolQuery.build()._toQuery());

        // 封装分页
        // start=(currentPage-1)*pageSize
        int startIndex = (albumIndexQuery.getPageNo() - 1) * albumIndexQuery.getPageSize();
        builder.from(startIndex);
        builder.size(albumIndexQuery.getPageSize());

        // 高亮
        if (StringUtils.isNotEmpty(keyword)) {
            builder.highlight(h -> h.fields("albumTitle", hf -> hf.preTags("<font color='red'>").postTags("</font>")));
        }

        // 排序 综合排序[1:desc] 播放量[2:desc] 发布时间[3:desc]；asc:升序 desc:降序）
        String order = albumIndexQuery.getOrder();
        // 判断
        if (StringUtils.isNotEmpty(order)) {
            // order=1:desc
            String[] split = order.split(":");
            if (split != null && split.length == 2) {
                // 获取排序字段
                String fieldNum = split[0];
                // 获取排序方式
                String orderType = split[1];
                // 定义排序字段
                String field = "";
                // 转换字段
                switch (fieldNum) {
                    case "1":
                        field = "hotScore";
                        break;
                    case "2":
                        field="playStatNum";
                        break;
                    case "3" :
                        field="createTime";
                        break;
                }
                String finalField = field;
                builder.sort(s->s.field(f->f.field(finalField).order("asc".equals(orderType)? SortOrder.Asc:SortOrder.Desc)));
            }
        }
        // 过滤结果
        builder.source(s->s.filter(so->so.excludes( "category1Id",
                "category2Id",
                "category3Id",
                "attributeValueIndexList.attributeId",
                "attributeValueIndexList.valueId")));

        // 返回请求对象
        return builder.build();
    }


    /**
     * 解析结果集，转换返回值对象类型
     *
     * @param searchResponse
     * @return
     */
    private AlbumSearchResponseVo parseResult(SearchResponse<AlbumInfoIndex> searchResponse,AlbumIndexQuery albumIndexQuery) {
        // 创建分装结果对象
        AlbumSearchResponseVo albumSearchResponseVo=new AlbumSearchResponseVo();

        // 获取总每页条数
        Integer pageSize = albumIndexQuery.getPageSize();

        // 设置每页条数
        albumSearchResponseVo.setPageSize(pageSize);
        // 设置当前页
        albumSearchResponseVo.setPageNo(albumIndexQuery.getPageNo());
        // 获取总条数
        long total = searchResponse.hits().total().value();
        albumSearchResponseVo.setTotal(total);
        // 计算总页数
        albumSearchResponseVo.setTotalPages(total%pageSize==0?total/pageSize:total/pageSize+1);

        // 获取查询的结果数据
        List<Hit<AlbumInfoIndex>> hits = searchResponse.hits().hits();
        // 判断
        if(CollectionUtil.isNotEmpty(hits)){
            List<AlbumInfoIndexVo> albumInfoIndexVoList = hits.stream().map(hit -> {
                // 获取结果数据
                AlbumInfoIndexVo albumInfoIndexVo = BeanUtil.copyProperties(hit.source(), AlbumInfoIndexVo.class);
                // 获取高亮数据
                Map<String, List<String>> highlight = hit.highlight();
                // 判断
                if(highlight!=null &&highlight.containsKey("albumTitle")){
                    String albumTitle = highlight.get("albumTitle").get(0);
                    // 覆盖原来不带高亮的专辑标题名称
                    albumInfoIndexVo.setAlbumTitle(albumTitle);
                }
                return albumInfoIndexVo;
            }).collect(Collectors.toList());

            // 封装集合到结果集中
            albumSearchResponseVo.setList(albumInfoIndexVoList);
        }

        return albumSearchResponseVo;
    }
}
