package com.atguigu.tingshu.search.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.extra.pinyin.PinyinUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.Buckets;
import co.elastic.clients.elasticsearch._types.aggregations.LongTermsBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.CompletionSuggestOption;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.Suggestion;
import co.elastic.clients.json.JsonData;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.utils.StringUtils;
import com.atguigu.tingshu.album.AlbumFeignClient;
import com.atguigu.tingshu.model.album.AlbumAttributeValue;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.BaseCategory3;
import com.atguigu.tingshu.model.album.BaseCategoryView;
import com.atguigu.tingshu.model.search.AlbumInfoIndex;
import com.atguigu.tingshu.model.search.AttributeValueIndex;
import com.atguigu.tingshu.model.search.SuggestIndex;
import com.atguigu.tingshu.query.search.AlbumIndexQuery;
import com.atguigu.tingshu.search.repository.AlbumInfoIndexRepository;
import com.atguigu.tingshu.search.repository.SuggestIndexRepository;
import com.atguigu.tingshu.search.service.SearchService;
import com.atguigu.tingshu.user.client.UserFeignClient;
import com.atguigu.tingshu.vo.album.AlbumStatVo;
import com.atguigu.tingshu.vo.search.AlbumInfoIndexVo;
import com.atguigu.tingshu.vo.search.AlbumSearchResponseVo;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.suggest.Completion;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;


@Slf4j
@Service
@SuppressWarnings({"all"})
public class SearchServiceImpl implements SearchService {
    // 索引名称常量
    private static final String INDEX_NAME = "albuminfo";
    private static final String SUGGEST_INDEX_NAME = "suggestinfo";

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

    @Autowired
    private SuggestIndexRepository suggestIndexRepository;

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

        // 建立提词库
        this.saveSuggestIndex(albumInfoIndex);
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
     * 查询指定一级分类下热门排行专辑
     *
     * @param category1Id
     * @return
     */
    @Override
    public List<Map<String, Object>> channel(Long category1Id) {
        // 创建封装对象
        List<Map<String, Object>> resultMap = null;
        try {
            // 根据一级分类查询对应的三级分类集合
            List<BaseCategory3> baseCategory3List = albumFeignClient.findTopBaseCategory3(category1Id).getData();
            // 方便根据指定的三级分类id获取三级分类对象，将list集合转换成map
            Map<Long, BaseCategory3> baseCategory3Map = baseCategory3List.stream().collect(Collectors.toMap(BaseCategory3::getId, baseCategory3 -> baseCategory3));

            // 验证
            Assert.notNull(baseCategory3List, "一级分类{}未包含置顶三级分类", category1Id);
            // 根据结果构建三级分类id集合
            List<Long> category3IdList = baseCategory3List.stream().map(baseCategory3 -> baseCategory3.getId()).collect(Collectors.toList());

            // 构建三级分类查询条件
            List<FieldValue> fieldValueList = category3IdList.stream().map(category3Id -> {
                FieldValue fieldValue = FieldValue.of(category3Id);
                return fieldValue;
            }).collect(Collectors.toList());

            // 发送请求查询es
            SearchResponse<AlbumInfoIndex> searchResponse = elasticsearchClient.search(s ->
                            s.index(INDEX_NAME)
                                    .query(q -> q.terms(t -> t.field("category3Id").terms(te -> te.value(fieldValueList))))
                                    .size(0)
                                    .aggregations("category3Agg", ag ->
                                            ag.terms(t -> t.field("category3Id").size(10))
                                                    .aggregations("top6Agg", agSub -> agSub.topHits(to -> to.sort(so -> so.field(fi -> fi.field("hotScore").order(SortOrder.Desc))).size(6)))),
                    AlbumInfoIndex.class);

            // 解析查询结果
            Aggregate category3Agg = searchResponse.aggregations().get("category3Agg");
            // 获取buckets数据
            Buckets<LongTermsBucket> buckets = category3Agg.lterms().buckets();
            // 获取集合数据
            List<LongTermsBucket> termsBucketList = buckets.array();

            // 判断
            if (CollectionUtil.isNotEmpty(termsBucketList)) {
                // 遍历集合处理各各分组
                resultMap = termsBucketList.stream().map(bucket -> {
                    // 创建集合对象，封装结果
                    Map<String, Object> map = new HashMap<>();
                    // 获取三级分类ID
                    long category3Id = bucket.key();
                    BaseCategory3 baseCategory3 = baseCategory3Map.get(category3Id);
                    map.put("baseCategory3", baseCategory3);
                    // 获取数据集合
                    Aggregate top6Agg = bucket.aggregations().get("top6Agg");
                    // 获取数据
                    List<Hit<JsonData>> hitList = top6Agg.topHits().hits().hits();
                    // 判断
                    if (CollectionUtil.isNotEmpty(hitList)) {
                        List<AlbumInfoIndex> albumInfoIndexList = hitList.stream().map(jsonDataHit -> {
                            // 获取数据结果
                            JsonData source = jsonDataHit.source();
                            // 转换数据
                            AlbumInfoIndex albumInfoIndex = JSON.parseObject(source.toString(), AlbumInfoIndex.class);
                            return albumInfoIndex;
                        }).collect(Collectors.toList());
                        // 手机分组专辑数据
                        map.put("list", albumInfoIndexList);
                    }
                    return map;
                }).collect(Collectors.toList());
            }
        } catch (IOException e) {
            log.error("查询热门专辑对接es异常：" + e.getMessage());
            throw new RuntimeException(e);
        }
        return resultMap;
    }

    /**
     * 关键字自动补全
     *
     * @param keyword
     * @return
     */
    @Override
    public List<String> completeSuggest(String keyword) {
        try {
            // 1.查询题词库
            // 构建查询题词库DSL,获取查询结果
            SearchResponse<SuggestIndex> searchResponse = elasticsearchClient.search(s ->s.index(SUGGEST_INDEX_NAME)
                            .suggest(su -> su
                                    .suggesters("mySuggestKeyword", fi -> fi.prefix(keyword).completion(c -> c.field("keyword").size(10).skipDuplicates(true)))
                                    .suggesters("mySuggestPinyin", fi -> fi.prefix(keyword).completion(c -> c.field("keywordPinyin").size(10).skipDuplicates(true)))
                                    .suggesters("mySuggestSequence", fi -> fi.prefix(keyword).completion(c -> c.field("keywordSequence").size(10).skipDuplicates(true))))
                    , SuggestIndex.class);
            // 封装结果--去重 set集合
            Set<String> titleSet = new LinkedHashSet<>();
            titleSet.addAll(this.parseSuggestResult("mySuggestKeyword", searchResponse));
            titleSet.addAll(this.parseSuggestResult("mySuggestPinyin", searchResponse));
            titleSet.addAll(this.parseSuggestResult("mySuggestPinyin", searchResponse));

            log.info("titleSet的内容为：{}", titleSet);

            // 结果不足10条
            if(titleSet.size()>=10){
                return new ArrayList<>(titleSet).subList(0,10);
            }

            // 2.查询专辑索引库
            SearchResponse<AlbumInfoIndex> albumInfoIndexSearchResponse = elasticsearchClient.search(s -> s.index(INDEX_NAME).query(q -> q.match(m -> m.field("albumTitle").query(keyword))), AlbumInfoIndex.class);
            // 解析结果
            List<Hit<AlbumInfoIndex>> hitList = albumInfoIndexSearchResponse.hits().hits();
            // 判断
            if(CollectionUtil.isNotEmpty(hitList)){
                // 遍历处理
                for (Hit<AlbumInfoIndex> albumInfoIndexHit : hitList) {
                    AlbumInfoIndex albumInfoIndex = albumInfoIndexHit.source();
                    titleSet.add(albumInfoIndex.getAlbumTitle());
                    // 判断
                    if(titleSet.size()>=10){
                        break;
                    }
                }
            }
            // 存储到结果集合中
            return new ArrayList<>(titleSet);
        } catch (IOException e) {
            log.error("[搜索服务]建议词自动补全异常：{}", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 构建提词库
     *
     * @param albumInfoIndex
     */
    @Override
    public void  saveSuggestIndex(AlbumInfoIndex albumInfoIndex) {
        // 创建提词对象
        SuggestIndex suggestIndex = new SuggestIndex();
        // 设置提词对象id
        suggestIndex.setId(albumInfoIndex.getId().toString());
        // 设置提词对象title
        suggestIndex.setTitle(albumInfoIndex.getAlbumTitle());
        // 设置提词对象 汉字提词或者单词
        suggestIndex.setKeyword(new Completion(new String[]{albumInfoIndex.getAlbumTitle()}));
        // 设置提词对象 拼音提词
        suggestIndex.setKeywordPinyin(new Completion(new String[]{PinyinUtil.getPinyin(albumInfoIndex.getAlbumTitle(), "")}));
        // 设置提词对象 首字母缩写提词
        suggestIndex.setKeywordSequence(new Completion(new String[]{PinyinUtil.getFirstLetter(albumInfoIndex.getAlbumTitle(), "")}));

        // 添加到提词库
        suggestIndexRepository.save(suggestIndex);
    }

    /**
     * 查询专辑 详情
     * @param albumId
     * @return
     */
    @Override
    public Map<String, Object> getItem(Long albumId) {
        // 创建封装结果对象,多线程环境要使用线程安全的 Map
        Map<String, Object> resultMap=new ConcurrentHashMap<>();
        // 查询专辑信息
        CompletableFuture<AlbumInfo> albumInfoCompletableFuture = CompletableFuture.supplyAsync(() -> {
            AlbumInfo albumInfo = albumFeignClient.getAlbumInfo(albumId).getData();
            Assert.notNull(albumInfo, "查询专辑ID:{},出现异常", albumInfo);

            // 封装
            resultMap.put("albumInfo", albumInfo);
            return albumInfo;
        }, executor);

        // 查询三级分类信息
        CompletableFuture<Void> categoryViewFuture = albumInfoCompletableFuture.thenAcceptAsync(albumInfo -> {
            BaseCategoryView baseCategoryView = albumFeignClient.getCategoryView(albumInfo.getCategory3Id()).getData();
            Assert.notNull(baseCategoryView, "查询分类三级分类ID:{},出现异常", albumInfo.getCategory3Id());

            resultMap.put("baseCategoryView",baseCategoryView);
        }, executor);

        //查询专辑统计信息
        CompletableFuture<Void> albumStatVoFuture = CompletableFuture.runAsync(() -> {
            AlbumStatVo albumStatVo = albumFeignClient.getAlbumStatVo(albumId).getData();
            // 判断
            Assert.notNull(albumStatVo, "查询专辑统计信息ID:{},出现异常", albumId);
            // 存储
            resultMap.put("albumStatVo", albumStatVo);
        }, executor);

        // 查询用户信息
        CompletableFuture<Void> announcerFuture = albumInfoCompletableFuture.thenAcceptAsync(albumInfo -> {
            UserInfoVo userInfoVo = userFeignClient.getUserInfoVo(albumInfo.getUserId()).getData();
            Assert.notNull(userInfoVo, "查询用户信息用户ID:{},出现异常", albumInfo.getUserId());

            resultMap.put("announcer", userInfoVo);
        }, executor);

        // 编排异步对象关系
        CompletableFuture.allOf(
                albumInfoCompletableFuture,
                categoryViewFuture,
                albumStatVoFuture,
                announcerFuture
        ).join();

        return resultMap;
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

    /**
     * 解析题词查询结果
     *
     * @param mySuggestKeyword
     * @param searchResponse
     * @return
     */
    private Collection<String> parseSuggestResult(String suggestKeyword, SearchResponse<SuggestIndex> searchResponse) {
        // 获取题词组数据
        List<Suggestion<SuggestIndex>> suggestions = searchResponse.suggest().get(suggestKeyword);
        // 创建接收结果的集合
        List<String> list=new ArrayList<>();
        // 遍历
        for (Suggestion<SuggestIndex> suggestion : suggestions) {
            List<CompletionSuggestOption<SuggestIndex>> options = suggestion.completion().options();
            // 判断
            if(CollectionUtil.isNotEmpty(options)){
                for (CompletionSuggestOption<SuggestIndex> option : options) {
                    SuggestIndex suggestIndex = option.source();
                    list.add(suggestIndex.getTitle());
                }
            }
        }
        return list;
    }
}
