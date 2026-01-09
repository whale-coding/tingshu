package com.atguigu.tingshu.album.mapper;

import com.atguigu.tingshu.model.album.TrackInfo;
import com.atguigu.tingshu.query.album.TrackInfoQuery;
import com.atguigu.tingshu.vo.album.AlbumTrackListVo;
import com.atguigu.tingshu.vo.album.TrackListVo;
import com.atguigu.tingshu.vo.album.TrackStatVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TrackInfoMapper extends BaseMapper<TrackInfo> {
    /**
     *  获取当前用户声音分页列表
     * @param listVoPage
     * @param trackInfoQuery
     * @return
     */
    Page<TrackListVo> selectUserTrackPage(Page<TrackListVo> listVoPage, @Param("vo") TrackInfoQuery trackInfoQuery);

    /**
     * 更新声音排序
     * @param albumId
     * @param orderNum
     */
    void updateOrderNum(@Param("albumId") Long albumId, @Param("orderNum") Integer orderNum);

    /**
     * 查询专辑声音分页列表
     * @param albumTrackListVoPage
     * @param albumId
     * @return
     */
    Page<AlbumTrackListVo> selectAlbumTrackPage(Page<AlbumTrackListVo> albumTrackListVoPage, @Param("albumId") Long albumId);

    /**
     * 获取声音统计信息
     * @param trackId
     * @return
     */
    @Select("select\n" +
            "    max(if(stat_type='0701',stat_num,0)) playStatNum,\n" +
            "    max(if(stat_type='0702',stat_num,0)) collectStatNum,\n" +
            "    max(if(stat_type='0703',stat_num,0)) praiseStatNum,\n" +
            "    max(if(stat_type='0704',stat_num,0)) commentStatNum\n" +
            "    from track_stat where track_id=#{trackId} and is_deleted=0")
    TrackStatVo selectTrackStatVo(Long trackId);
}
