package com.atguigu.tingshu.user.service;

import com.atguigu.tingshu.model.user.UserInfo;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

public interface UserInfoService extends IService<UserInfo> {

    /**
     * 小程序授权登录
     * @param code
     * @return
     */
    Map<String, String> wxLogin(String code);

    /**
     * 获取登录用户信息
     * @param userId
     * @return
     */
    UserInfoVo getUserInfo(Long userId);

    /**
     * 更新用户信息
     * @param userInfoVo
     */
    void updateUser(UserInfoVo userInfoVo,String token);
}
