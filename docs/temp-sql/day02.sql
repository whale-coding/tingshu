
# 查询所有分类（1、2、3级分类）
# 1.涉及到的表：base_category1、base_category2、base_category3
# 2.表关联关系：category1_id、category2_id （外键）
# 3.查询方法：内连接 inner join
# 4.条件：没有条件
select * from base_category1 c1
    inner join base_category2 c2
    on c1.id=c2.category1_id
    inner join base_category3 c3
    on c2.id=c3.category2_id;

# sql数据去重
select distinct(c1.id)  from base_category1 c1
    inner join base_category2 c2
    on c1.id=c2.category1_id
    inner join base_category3 c3
    on c2.id=c3.category2_id;

# group by
select c1.id,c1.name  from base_category1 c1
    inner join base_category2 c2
    on c1.id=c2.category1_id
    inner join base_category3 c3
    on c2.id=c3.category2_id
    group by c1.id;


# 视图（虚拟表）
# 视图实质是一个封装的sql语句，作用1:二次开发封装sql使用操作;作用二：隐藏字段使用

# 1.创建视图
# 语法：create [or replace] view 视图名称 as SQL语句；
create or replace view  base_category_view as
    select c3.id,
           c1.id category1_id,
           c1.name category1_name,
           c2.id category2_id,
           c2.name category2_name,
           c3.id category3_id,
           c3.name category3_name,
           c3.is_deleted,
           c3.create_time,
           c3.update_time
    from base_category1 c1
    inner join base_category2 c2
    on c1.id=c2.category1_id
    inner join base_category3 c3
    on c2.id=c3.category2_id;

# 2.查询视图
select  * from base_category_view;

# 删除视图
drop view base_category_view;


# 根据一级分类Id获取分类属性（标签）列表
select bai.id,bai.category1_id,bai.attribute_name,
       bav.id base_attribute_value_id,bav.attribute_id,bav.value_name
    from base_attribute bai
    inner join base_attribute_value bav
    on bai.id=bav.attribute_id
    where bai.category1_id=2;

