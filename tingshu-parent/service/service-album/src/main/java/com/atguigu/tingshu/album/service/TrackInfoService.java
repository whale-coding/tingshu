package com.atguigu.tingshu.album.service;

import com.atguigu.tingshu.model.album.TrackInfo;
import com.atguigu.tingshu.query.album.TrackInfoQuery;
import com.atguigu.tingshu.vo.album.TrackInfoVo;
import com.atguigu.tingshu.vo.album.TrackListVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

public interface TrackInfoService extends IService<TrackInfo> {
    /**
     * 保存声音
     * @param trackInfoVo
     * @param userId
     */
    void saveTrackInfo(TrackInfoVo trackInfoVo, Long userId);

    /**
     * 初始化统计信息
     * @param trackId
     * @param statType
     * @param stateNum
     */
    void saveTrackState(Long trackId, String statType, int stateNum);

    /**
     * 获取当前用户声音分页列表
     * @param trackInfoQuery
     * @return
     */
    Page<TrackListVo> findUserTrackPage(Page<TrackListVo> listVoPage, TrackInfoQuery trackInfoQuery);

    /**
     * 修改声音信息
     * @param id
     * @param trackInfoVo
     */
    void updateTrackInfo(Long id, TrackInfoVo trackInfoVo);

    /**
     * 删除声音信息
     * @param id
     */
    void removeTrackInfo(Long id);
}
