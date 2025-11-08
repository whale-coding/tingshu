package com.atguigu.tingshu.album.service;

import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.vo.album.AlbumInfoVo;
import com.baomidou.mybatisplus.extension.service.IService;

public interface AlbumInfoService extends IService<AlbumInfo> {
    /**
     *  新增专辑
     * @param albumInfoVo
     */
    void saveAlbumInfo(AlbumInfoVo albumInfoVo, Long userId);

}
