package com.atguigu.tingshu.album.mapper;

import com.atguigu.tingshu.model.album.TrackStat;
import com.atguigu.tingshu.vo.album.TrackStatMqVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface TrackStatMapper extends BaseMapper<TrackStat> {
    /**
     * 声音统计处理
     * @param trackStatMqVo
     */
    @Update("update track_stat \n" +
            "set stat_num=stat_num+#{vo.count} , update_time=now()  \n" +
            "where track_id=#{vo.trackId} and stat_type=#{vo.statType} and is_deleted=0")
    void updateTrackStat(@Param("vo") TrackStatMqVo trackStatMqVo);

}
