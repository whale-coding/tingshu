package com.atguigu.tingshu.user.client.impl;


import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.user.client.UserFeignClient;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class UserDegradeFeignClient implements UserFeignClient {

    /**
     * 根据用户ID查询用户信息
     * @param userId
     * @return
     */
    @Override
    public Result<UserInfoVo> getUserInfoVo(Long userId) {
        log.error("调用用户微服务的getUserInfoVo方法降级");
        return Result.fail();
    }

    @Override
    public Result<Map<Long, Integer>> userIsPaidTrack(Long userId, Long albumId, List<Long> needChackTrackIdList) {
        log.error("调用用户微服务的userIsPaidTrack方法降级");
        return Result.fail();
    }
}
