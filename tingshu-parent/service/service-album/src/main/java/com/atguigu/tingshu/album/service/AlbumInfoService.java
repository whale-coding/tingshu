package com.atguigu.tingshu.album.service;

import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.query.album.AlbumInfoQuery;
import com.atguigu.tingshu.vo.album.AlbumInfoVo;
import com.atguigu.tingshu.vo.album.AlbumListVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface AlbumInfoService extends IService<AlbumInfo> {
    /**
     *  新增专辑
     * @param albumInfoVo
     */
    void saveAlbumInfo(AlbumInfoVo albumInfoVo, Long userId);

    /**
     * 查看当前用户专辑分页列表
     * @param albumListVoPage
     * @param albumInfoQuery
     * @return
     */
    Page<AlbumListVo> findUserAlbumPage(Page<AlbumListVo> albumListVoPage, AlbumInfoQuery albumInfoQuery);

    /**
     * 根据ID删除专辑
     * @param id
     */
    void removeAlbumInfo(Long id);

    /**
     * 根据ID查询专辑信息
     * @param id
     * @return
     */
    AlbumInfo getAlbumInfo(Long id);

    /**
     *  修改专辑
     * @param albumInfoVo
     * @param id
     */
    void updateAlbumInfo(AlbumInfoVo albumInfoVo, Long id);

    /**
     * 获取当前用户全部专辑列表
     * @return
     */
    List<AlbumInfo> findUserAllAlbumList(Long userId);
}
