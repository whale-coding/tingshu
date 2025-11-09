package com.atguigu.tingshu.album.mapper;

import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.query.album.AlbumInfoQuery;
import com.atguigu.tingshu.vo.album.AlbumListVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AlbumInfoMapper extends BaseMapper<AlbumInfo> {
    /**
     * 查看当前用户专辑分页列表
     * @param albumListVoPage
     * @param albumInfoQuery
     * @return
     */
    // 多个参数一定要加@Param注解
    Page<AlbumListVo> selectUserAlbumPage(@Param("albumListVoPage") Page<AlbumListVo> albumListVoPage,
                                          @Param("vo") AlbumInfoQuery albumInfoQuery);
}
