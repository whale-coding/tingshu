package com.atguigu.tingshu.user.api;

import com.atguigu.tingshu.common.login.GuiguLogin;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.user.service.UserInfoService;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "用户管理接口")
@RestController
@RequestMapping("api/user")
@SuppressWarnings({"all"})
public class UserInfoApiController {

	@Autowired
	private UserInfoService userInfoService;

    /**
     * 根据用户ID查询用户信息
     * api/user/userInfo/getUserInfoVo/{userId}
     * @return
     */
    @GetMapping("/userInfo/getUserInfoVo/{userId}")
    public Result<UserInfoVo> getUserInfoVo(@PathVariable Long userId){
        UserInfoVo userInfoVo=userInfoService.getUserInfo(userId);

        return Result.ok(userInfoVo);
    }

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
                                                     @RequestBody List<Long> needChackTrackIdList){
        Map<Long,Integer> resultMap=userInfoService.userIsPaidTrack(userId,albumId,needChackTrackIdList);

        return Result.ok(resultMap);
    }

    /**
     * 判断用户是否购买过指定专辑
     * api/user/userInfo/isPaidAlbum/{albumId}
     * @param albumId
     * @return
     */
    @GetMapping("/userInfo/isPaidAlbum/{albumId}")
    @GuiguLogin
    public Result<Boolean> isPaidAlbum(@PathVariable Long albumId){
        // 获取用户ID
        Long userId = AuthContextHolder.getUserId();
        // 调用service查询
        Boolean flag=userInfoService.isPaidAlbum(userId,albumId);
        return Result.ok(flag);
    }
}

