package com.atguigu.tingshu.album.mapper;

import com.atguigu.tingshu.model.album.TrackInfo;
import com.atguigu.tingshu.query.album.TrackInfoQuery;
import com.atguigu.tingshu.vo.album.TrackListVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
}
