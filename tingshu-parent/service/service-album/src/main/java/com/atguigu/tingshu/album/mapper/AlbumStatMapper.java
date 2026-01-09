package com.atguigu.tingshu.album.mapper;

import com.atguigu.tingshu.model.album.AlbumStat;
import com.atguigu.tingshu.vo.album.AlbumStatVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AlbumStatMapper extends BaseMapper<AlbumStat> {

    /**
     *  根据专辑ID获取专辑统计信息
     * @param albumId
     * @return
     */
    AlbumStatVo selectAlbumStatVo(Long albumId);

    /**
     * 更新专辑统计
     * @param albumId
     * @param statType
     */
    @Update("update album_stat set \n" +
            "stat_num=stat_num+#{count} ,update_time=now() \n" +
            "where album_id=#{albumId} and stat_type=#{statType} and is_deleted=0")
    void updateAlbumStat(@Param("albumId") Long albumId, @Param("count") Integer count, @Param("statType") String statType);
}
