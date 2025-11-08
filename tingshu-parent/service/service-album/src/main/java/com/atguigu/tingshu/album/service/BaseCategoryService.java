package com.atguigu.tingshu.album.service;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.tingshu.model.album.BaseAttribute;
import com.atguigu.tingshu.model.album.BaseCategory1;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface BaseCategoryService extends IService<BaseCategory1> {

    /**
     * 查询所有分类（1、2、3级分类）
     * @return 分类集合
     */
    List<JSONObject> getBaseCategoryList();

    /**
     * 根据一级分类Id获取分类属性（标签）列表
     * @param category1Id 一级分类id
     * @return 标签列表
     */
    List<BaseAttribute> findAttribute(Long category1Id);
}
