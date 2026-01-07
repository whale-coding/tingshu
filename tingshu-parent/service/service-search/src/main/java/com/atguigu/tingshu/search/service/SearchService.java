package com.atguigu.tingshu.search.service;

import com.atguigu.tingshu.model.search.AlbumInfoIndex;
import com.atguigu.tingshu.query.search.AlbumIndexQuery;
import com.atguigu.tingshu.vo.search.AlbumSearchResponseVo;

import java.util.List;
import java.util.Map;

public interface SearchService {
    /**
     * 上架专辑-导入索引库
     * @param albumId
     */
    void upperAlbum(Long albumId);

    /**
     * 下架专辑-删除文档
     * @param albumId
     */
    void lowerAlbum(Long albumId);

    /**
     * 专辑检索
     * @param albumIndexQuery
     * @return
     */
    AlbumSearchResponseVo search(AlbumIndexQuery albumIndexQuery);

    /**
     * 查询指定一级分类下热门排行专辑
     * @param category1Id
     * @return
     */
    List<Map<String, Object>> channel(Long category1Id);

    /**
     * 关键字自动补全
     * @param keyword
     * @return
     */
    List<String> completeSuggest(String keyword);

    /**
     * 构建提词库
     * @param albumInfoIndex
     */
    void saveSuggestIndex(AlbumInfoIndex albumInfoIndex);

    /**
     * 查询专辑 详情
     * @param albumId
     * @return
     */
    Map<String, Object> getItem(Long albumId);
}
