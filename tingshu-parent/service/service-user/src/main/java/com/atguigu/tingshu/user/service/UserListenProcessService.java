package com.atguigu.tingshu.user.service;

import com.atguigu.tingshu.vo.user.UserListenProcessVo;

import java.math.BigDecimal;
import java.util.Map;

public interface UserListenProcessService {
    /**
     * 获取声音的上次跳出时间
     * @param trackId
     * @return
     */
    BigDecimal getTrackBreakSecond(Long trackId, Long userId);

    /**
     * 更新播放进度
     * @param userListenProcessVo
     * @param userId
     */
    void updateListenProcess(UserListenProcessVo userListenProcessVo, Long userId);

    /**
     * 获取用户最近一次播放记录
     * @param userId
     * @return
     */
    Map<String, Long> getLatelyTrack(Long userId);
}
