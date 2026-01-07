package com.atguigu.tingshu.album.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.nacos.common.utils.StringUtils;
import com.atguigu.tingshu.album.AlbumFeignClient;
import com.atguigu.tingshu.album.mapper.AlbumInfoMapper;
import com.atguigu.tingshu.album.mapper.TrackInfoMapper;
import com.atguigu.tingshu.album.mapper.TrackStatMapper;
import com.atguigu.tingshu.album.service.TrackInfoService;
import com.atguigu.tingshu.album.service.VodService;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.TrackInfo;
import com.atguigu.tingshu.model.album.TrackStat;
import com.atguigu.tingshu.query.album.TrackInfoQuery;
import com.atguigu.tingshu.user.client.UserFeignClient;
import com.atguigu.tingshu.vo.album.AlbumTrackListVo;
import com.atguigu.tingshu.vo.album.TrackInfoVo;
import com.atguigu.tingshu.vo.album.TrackListVo;
import com.atguigu.tingshu.vo.album.TrackMediaInfoVo;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.hutool.core.lang.Assert;
import java.util.function.Predicate;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@SuppressWarnings({"all"})
public class TrackInfoServiceImpl extends ServiceImpl<TrackInfoMapper, TrackInfo> implements TrackInfoService {

	@Autowired
	private TrackInfoMapper trackInfoMapper;

    @Autowired
    private AlbumInfoMapper albumInfoMapper;

    @Autowired
    private VodService vodService;

    @Autowired
    private TrackStatMapper trackStatMapper;

    @Autowired
    private AlbumFeignClient albumFeignClient;

    @Autowired
    private UserFeignClient userFeignClient;

    /**
     * 保存声音
     * @param trackInfoVo
     * @param userId
     * 涉及到的表：
     * track_info :声音表，保存声音的主体信息
     * album_info：在声音添加时，更新专辑包含总数
     * track_stat：初始化统计信息
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveTrackInfo(TrackInfoVo trackInfoVo, Long userId) {
        // 拷贝数据
        TrackInfo trackInfo = BeanUtil.copyProperties(trackInfoVo, TrackInfo.class);
        trackInfo.setUserId(userId);

        // 根据当前声音所属专辑id查询专辑信息
        AlbumInfo albumInfo = albumInfoMapper.selectById(trackInfo.getAlbumId());
        // 设置排序字段
        trackInfo.setOrderNum(albumInfo.getIncludeTrackCount() + 1);

        // 查询云点播声音的 时长 ，大小，类型
        TrackMediaInfoVo trackMediaInfoVo = vodService.getTrackMediaInfo(trackInfo.getMediaFileId());
        trackInfo.setMediaDuration(new BigDecimal(trackMediaInfoVo.getDuration()));
        trackInfo.setMediaSize(trackMediaInfoVo.getSize());
        trackInfo.setMediaType(trackMediaInfoVo.getType());
        // 设置声音来源
        trackInfo.setSource(SystemConstant.TRACK_SOURCE_USER);
        // 设置状态
        trackInfo.setStatus(SystemConstant.TRACK_STATUS_PASS);

        // 保存声音
        trackInfoMapper.insert(trackInfo);
        // 更新专辑包含总数
        albumInfo.setIncludeTrackCount(albumInfo.getIncludeTrackCount() + 1);

        albumInfoMapper.updateById(albumInfo);

        // 初始化统计信息
        this.saveTrackState(trackInfo.getId(), SystemConstant.TRACK_STAT_PLAY, 0);
        this.saveTrackState(trackInfo.getId(), SystemConstant.TRACK_STAT_COLLECT, 0);
        this.saveTrackState(trackInfo.getId(), SystemConstant.TRACK_STAT_PRAISE, 0);
        this.saveTrackState(trackInfo.getId(), SystemConstant.TRACK_STAT_COMMENT, 0);
    }

    /**
     * 初始化统计信息
     * @param trackId
     * @param statType
     * @param stateNum
     */
    @Override
    public void saveTrackState(Long trackId, String statType, int stateNum) {
        TrackStat trackStat = new TrackStat();
        trackStat.setTrackId(trackId);
        trackStat.setStatType(statType);
        trackStat.setStatNum(stateNum);

        trackStatMapper.insert(trackStat);
    }

    /**
     * 获取当前用户声音分页列表
     * @param trackInfoQuery
     * @return
     */
    @Override
    public Page<TrackListVo> findUserTrackPage(Page<TrackListVo> listVoPage, TrackInfoQuery trackInfoQuery) {
        return trackInfoMapper.selectUserTrackPage(listVoPage, trackInfoQuery);
    }

    /**
     * 修改声音信息
     * @param id
     * @param trackInfoVo
     */
    @Override
    public void updateTrackInfo(Long id, TrackInfoVo trackInfoVo) {
        // 根据id查询数据声音信息
        TrackInfo trackInfo = trackInfoMapper.selectById(id);
        // 获取修改前的声音id
        String beforeMediaFileId = trackInfo.getMediaFileId();
        // 获取修改后的声音id
        String aftermediaFileId = trackInfoVo.getMediaFileId();
        // 整合修改数据
        BeanUtils.copyProperties(trackInfoVo,trackInfo);
        // 判断声音是否更改--删除云点播服务存储的旧声音
        if (!StringUtils.equals(beforeMediaFileId, aftermediaFileId)) {
            // 调用vodService删除
            vodService.deleteTrackMedia(beforeMediaFileId);
            // 新声音的id查询云点播声音详情 大小 时长 类型
            if (StringUtils.isNotBlank(aftermediaFileId)) {
                TrackMediaInfoVo trackMediaInfo = vodService.getTrackMediaInfo(aftermediaFileId);
                trackInfo.setMediaType(trackMediaInfo.getType());
                trackInfo.setMediaDuration(new BigDecimal(trackMediaInfo.getDuration()));
                trackInfo.setMediaSize(trackMediaInfo.getSize());

            }
        }
        // 执行修改
        trackInfoMapper.updateById(trackInfo);
    }

    /**
     * 删除声音信息
     * @param id
     * 涉及到的表：track_stat、track_info、album_info
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeTrackInfo(Long id) {
        // 查询根据声音id，查询声音信息--获取专辑ID
        TrackInfo trackInfo = trackInfoMapper.selectById(id);
        Long albumId = trackInfo.getAlbumId();
        Integer orderNum = trackInfo.getOrderNum();

        // 删除声音
        trackInfoMapper.deleteById(id);

        // 更新声音排序
        trackInfoMapper.updateOrderNum(albumId,orderNum);

        // 根据专辑ID获取专辑对象
        AlbumInfo albumInfo = albumInfoMapper.selectById(albumId);

        // 更新专辑包含总数
        albumInfo.setIncludeTrackCount(albumInfo.getIncludeTrackCount()-1);
        albumInfo.setUpdateTime(new Date());
        albumInfoMapper.updateById(albumInfo);

        // 删除声音统计信息
        trackStatMapper.delete(new QueryWrapper<TrackStat>().eq("track_id",id));

        // 云点播声音信息删除
        vodService.deleteTrackMedia(trackInfo.getMediaFileId());
    }

    /**
     * 查询专辑声音分页列表
     * @param albumTrackListVoPage
     * @param albumId
     * @return
     */
    @Override
    public Page<AlbumTrackListVo> findAlbumTrackPage(Page<AlbumTrackListVo> albumTrackListVoPage, Long albumId, Long userId) {
        // 查询专辑声音分页列表
        Page<AlbumTrackListVo> albumTrackPage = trackInfoMapper.selectAlbumTrackPage(albumTrackListVoPage, albumId);

        // 获取专辑类型 免费  vip免费  付费
        AlbumInfo albumInfo = albumFeignClient.getAlbumInfo(albumId).getData();
        Assert.notNull(albumInfo, "查询专辑Id:{},出现异常", albumId);
        String payType = albumInfo.getPayType();

        // 判断是否登录
        if (userId == null) {
            // 未登录
            // 判断在未登录的情况下，判读是否为vip免费或者付费
            if (payType.equals(SystemConstant.ALBUM_PAY_TYPE_VIPFREE) || payType.equals(SystemConstant.ALBUM_PAY_TYPE_REQUIRE)) {
                // 只开发试听部分的声音 除了试听集数外，其余对象的isShowPaidMark都设置为true
                albumTrackPage.getRecords().stream().filter(new Predicate<AlbumTrackListVo>() {
                    @Override
                    public boolean test(AlbumTrackListVo albumTrackListVo) {
                        return albumTrackListVo.getOrderNum() > albumInfo.getTracksForFree();
                    }
                }).forEach(albumTrackListVo -> {
                    // 出去过滤的声音后，其余的需要添加付费标识
                    albumTrackListVo.setIsShowPaidMark(true);
                });
            }
        } else {
            // 登录
            // 设置变量
            Boolean isNeedCheckPayStatus = false;
            // 根据用户ID查询用户信息
            UserInfoVo userInfoVo = userFeignClient.getUserInfoVo(userId).getData();
            Assert.notNull(userInfoVo,"查询用户信息id:{},异常",userId);
            // 获取用户vip状态
            Integer isVip = userInfoVo.getIsVip();
            // 获取用户vip的过期时间
            Date vipExpireTime = userInfoVo.getVipExpireTime();

            // vip免费
            if(payType.equals(SystemConstant.ALBUM_PAY_TYPE_VIPFREE)){
                // 普通用户
                if(isVip.intValue()==0){
                    isNeedCheckPayStatus=true;
                }
                // 是否为vip过期用户
                if(isVip.intValue()==1&&new Date().after(vipExpireTime)){
                    isNeedCheckPayStatus=true;
                }
            }
            // 付费
            if(payType.equals(SystemConstant.ALBUM_PAY_TYPE_REQUIRE)){
                isNeedCheckPayStatus=true;
            }
            if (isNeedCheckPayStatus) {
                // 获取待验证声音列表
                List<AlbumTrackListVo> needChackTrackList = albumTrackPage.getRecords().stream().filter(albumTrackListVo -> {
                    return albumTrackListVo.getOrderNum() > albumInfo.getTracksForFree();
                }).collect(Collectors.toList());
                // 获取待验证声音id列表
                List<Long> needChackTrackIdList = needChackTrackList.stream().map(albumTrackListVo -> albumTrackListVo.getTrackId()).collect(Collectors.toList());
                // 进行下一步处理--查询是否购买过专辑或者声音列表
                Map<Long, Integer> resultMap = userFeignClient.userIsPaidTrack(userId, albumId, needChackTrackIdList).getData();
                // 根据购买情况设置付费标识
                for (AlbumTrackListVo albumTrackListVo : needChackTrackList) {
                    // 根据指定的声音id，获取结果
                    Integer result = resultMap.get(albumTrackListVo.getTrackId());
                    // 判断 结果为0说明未购买
                    if(result.intValue()==0){
                        albumTrackListVo.setIsShowPaidMark(true);
                    }
                }
            }
        }
        return albumTrackPage;
    }
}
