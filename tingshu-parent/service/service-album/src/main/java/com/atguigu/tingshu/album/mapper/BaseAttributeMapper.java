package com.atguigu.tingshu.album.mapper;

import com.atguigu.tingshu.model.album.BaseAttribute;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BaseAttributeMapper extends BaseMapper<BaseAttribute> {

    /**
     * 根据一级分类Id获取分类属性（标签）列表
     * @param category1Id 一级分类id
     * @return 标签列表
     */
    List<BaseAttribute> selectAttribute(Long category1Id);

}
