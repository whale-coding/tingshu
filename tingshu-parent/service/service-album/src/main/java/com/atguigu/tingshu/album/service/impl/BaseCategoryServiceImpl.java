package com.atguigu.tingshu.album.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.tingshu.album.mapper.*;
import com.atguigu.tingshu.album.service.BaseCategoryService;
import com.atguigu.tingshu.model.album.BaseAttribute;
import com.atguigu.tingshu.model.album.BaseCategory1;
import com.atguigu.tingshu.model.album.BaseCategoryView;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@SuppressWarnings({"all"})
public class BaseCategoryServiceImpl extends ServiceImpl<BaseCategory1Mapper, BaseCategory1> implements BaseCategoryService {

	@Autowired
	private BaseCategory1Mapper baseCategory1Mapper;

	@Autowired
	private BaseCategory2Mapper baseCategory2Mapper;

	@Autowired
	private BaseCategory3Mapper baseCategory3Mapper;

    @Autowired
    private BaseCategoryViewMapper baseCategoryViewMapper;  // 视图Mapper

    @Autowired
    private BaseAttributeMapper baseAttributeMapper;

    /**
     * 查询所有分类（1、2、3级分类）
     * @return 分类集合
     */
    @Override
    public List<JSONObject> getBaseCategoryList() {
        // 创建集合一级分类，收集数据
        List<JSONObject> allList=new ArrayList<>();

        // 查询所有分类信息
        List<BaseCategoryView> baseCategoryViewList = baseCategoryViewMapper.selectList(null);

        //判断
        if(CollectionUtil.isNotEmpty(baseCategoryViewList)){
            // 分组一级分类
            Map<Long, List<BaseCategoryView>> category1Map = baseCategoryViewList.stream()
                    .collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));
            // 遍历一级分类分组数据
            for (Map.Entry<Long, List<BaseCategoryView>> entry1 : category1Map.entrySet()){
                // 创建一级分类封装对象
                JSONObject obj1=new JSONObject();

                // 获取一级分类ID
                Long category1Id = entry1.getKey();
                // 获取一级分类name
                List<BaseCategoryView> category2List = entry1.getValue();
                String category1Name = category2List.get(0).getCategory1Name();
                // 一级分类封装对象属性填充：分类id和分类name
                obj1.put("categoryId",category1Id);
                obj1.put("categoryName",category1Name);

                // 创建二级分类收集集合
                List<JSONObject> array2=new ArrayList<>();

                //分组二级分类集合
                Map<Long, List<BaseCategoryView>> category2Map = category2List.stream()
                        .collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));

                // 遍历二级分类分组数据
                for (Map.Entry<Long, List<BaseCategoryView>> entry2 : category2Map.entrySet()){
                    // 创建二级分类封装对象
                    JSONObject obj2=new JSONObject();

                    // 获取二级分类ID
                    Long category2Id = entry2.getKey();
                    // 获取二分类name
                    List<BaseCategoryView> category3List = entry2.getValue();
                    String category2Name = category3List.get(0).getCategory2Name();

                    // 二级分类封装对象属性填充：分类id和分类name
                    obj2.put("categoryId",category2Id);
                    obj2.put("categoryName",category2Name);

                    // 封装三级分类集合
                    List<JSONObject> array3 = category3List.stream().map(baseCategoryView ->{
                        JSONObject obj3 = new JSONObject();
                        obj3.put("categoryId", baseCategoryView.getCategory3Id());
                        obj3.put("categoryName", baseCategoryView.getCategory3Name());
                        return obj3;
                    }).collect(Collectors.toList());

                    // 二级分类封装对象属性填充：三级分类
                    obj2.put("categoryChild",array3);

                    // 收集二级分类对象
                    array2.add(obj2);
                }

                // 一级分类封装对象属性填充：二级分类
                obj1.put("categoryChild",array2);

                // 收集一级分类对象
                allList.add(obj1);
            }
        }

        return allList;
    }

    /**
     * 根据一级分类Id获取分类属性（标签）列表
     * @param category1Id 一级分类id
     * @return 标签列表
     */
    @Override
    public List<BaseAttribute> findAttribute(Long category1Id) {
        return baseAttributeMapper.selectAttribute(category1Id);
    }

    /**
     * api/album/category/getCategoryView/{category3Id}
     * 根据三级分类Id 获取到分类信息
     * @param category3Id 三级分类Id
     * @return 分类信息
     */
    @Override
    public BaseCategoryView getCategoryView(Long category3Id) {
        return baseCategoryViewMapper.selectById(category3Id);
    }
}
