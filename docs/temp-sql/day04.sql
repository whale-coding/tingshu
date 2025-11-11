# 获取当前用户声音分页列表
select
    ti.album_id,
    ai.album_title,
    ts.track_id,
    ti.track_title,
    ti.cover_url,
    ti.media_duration,
    ti.status,
    max(if(ts.stat_type='0701',ts.stat_num,0)) playStatNum,
    max(if(ts.stat_type='0702',ts.stat_num,0)) collectStatNum,
    max(if(ts.stat_type='0703',ts.stat_num,0)) praiseStatNum,
    max(if(ts.stat_type='0704',ts.stat_num,0)) commentStatNum
from album_info ai
inner join track_info ti
on ai.id=ti.album_id
inner join track_stat ts
on ti.id=ts.track_id
where ti.user_id=? and ti.track_title like ? and ti.is_deleted=0 and ti.status=?
group by ti.id
order by ti.id desc;


# 更新声音排序
update track_info set order_num=order_num-1 where order_num > ? and album_id=? and is_deleted=0;