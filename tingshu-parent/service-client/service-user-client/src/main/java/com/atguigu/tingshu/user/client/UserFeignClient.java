package com.atguigu.tingshu.user.client;

import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.user.client.impl.UserDegradeFeignClient;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 产品列表API接口
 * </p>
 *
 * @author atguigu
 */
@FeignClient(value = "service-user", path = "api/user", fallback = UserDegradeFeignClient.class)
public interface UserFeignClient {
    /**
     * 根据用户ID查询用户信息
     * api/user/userInfo/getUserInfoVo/{userId}
     * @return
     */
    @GetMapping("/userInfo/getUserInfoVo/{userId}")
    public Result<UserInfoVo> getUserInfoVo(@PathVariable Long userId);

    /**
     * 获取用户声音列表付费情况
     * api/user/userInfo/userIsPaidTrack/{userId}/{albumId}
     * @param userId
     * @param albumId
     * @param needChackTrackIdList
     * @return
     */
    @PostMapping("/userInfo/userIsPaidTrack/{userId}/{albumId}")
    public Result<Map<Long,Integer>> userIsPaidTrack(@PathVariable Long userId,
                                                     @PathVariable Long albumId,
                                                     @RequestBody List<Long> needChackTrackIdList);

}
