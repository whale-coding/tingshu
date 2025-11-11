package com.atguigu.tingshu.user.api;

import com.atguigu.tingshu.common.login.GuiguLogin;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.user.service.UserInfoService;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "微信授权登录接口")
@RestController
@RequestMapping("/api/user/wxLogin")
@Slf4j
public class WxLoginApiController {

    @Autowired
    private UserInfoService userInfoService;

    /**
     * api/user/wxLogin/wxLogin/{code}
     * 小程序授权登录
     * @param code
     * @return
     */
    @GetMapping("/wxLogin/{code}")
    public Result<Map<String,String>> wxLogin(@PathVariable String code){
        Map<String,String> resultMap=  userInfoService.wxLogin(code);

        return Result.ok(resultMap);
    }

    /**
     *获取登录用户信息
     * api/user/wxLogin/getUserInfo
     * api/user/wxLogin/getUserInfo
     * @return
     */
    @GetMapping("/getUserInfo")
    @GuiguLogin
    public Result<UserInfoVo> getUserInfo(){
        Long userId = AuthContextHolder.getUserId();
        UserInfoVo userInfoVo=userInfoService.getUserInfo(userId);

        return Result.ok(userInfoVo);
    }

    /**
     * api/user/wxLogin/updateUser
     * 更新用户信息
     * @param userInfoVo
     * @return
     */
    @PostMapping("/updateUser")
    @GuiguLogin
    public Result updateUser(@RequestBody UserInfoVo userInfoVo, HttpServletRequest request){
        // 从请求头中获取token
        String token = request.getHeader("token");

        // 获取用户id
        Long userId = AuthContextHolder.getUserId();
        userInfoVo.setId(userId);
        userInfoService.updateUser(userInfoVo,token);

        return Result.ok();
    }
}
