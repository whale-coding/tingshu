package com.atguigu.tingshu.search.service;

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
}
