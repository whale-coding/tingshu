# 1.查询当前用户专辑分页列表
# 1.1实现sql第一步
select * from album_info ai
    inner join album_stat ast
    on ai.id=ast.album_id
    where ai.user_id= ? and ai.album_title like ? and ai.status= ? and ai.is_deleted= ?
    order by ai.id desc;

# 实现sql第二步，选择返回字段
select ast.album_id,ai.album_title,ai.cover_url,ai.include_track_count,ai.is_finished,
       ai.status,ast.stat_type,ast.stat_num
from album_info ai
inner join album_stat ast
on ai.id=ast.album_id
where ai.user_id=? and ai.album_title like ? and ai.status=? and ai.is_deleted=?
order by ai.id desc;

# 实现sql第三步，实现行转列
# 分组：select * 只能显示三种数据：1.分组的字段 2.聚合函数 3.一组相同字段
select ast.album_id,ai.album_title,ai.cover_url,ai.include_track_count,ai.is_finished,
       ai.status
from album_info ai
inner join album_stat ast
on ai.id=ast.album_id
where ai.user_id=? and ai.album_title like ? and ai.status=? and ai.is_deleted=?
group by ai.id
order by ai.id desc;

# 实现sql第三步，实现行转列（聚合函数）
select ast.album_id,ai.album_title,ai.cover_url,ai.include_track_count,ai.is_finished,
       ai.status,
       max(if(ast.stat_type='0401',ast.stat_num,0)) playStatNum,
       max(if(ast.stat_type='0402',ast.stat_num,0)) subscribeStatNum,
       max(if(ast.stat_type='0403',ast.stat_num,0)) buyStatNum,
       max(if(ast.stat_type='0404',ast.stat_num,0)) albumCommentStatNum
from album_info ai
inner join album_stat ast
on ai.id=ast.album_id
where ai.user_id=? and ai.album_title like ? and ai.status=? and ai.is_deleted=?
group by ai.id
order by ai.id desc;


# 获取当前用户全部专辑列表
select id,album_title from album_info where user_id order by id desc  limit 10;