package com.atguigu.tingshu.user.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import com.atguigu.tingshu.common.constant.KafkaConstant;
import com.atguigu.tingshu.common.constant.RedisConstant;
import com.atguigu.tingshu.common.login.GuiguLogin;
import com.atguigu.tingshu.common.service.KafkaService;
import com.atguigu.tingshu.model.user.UserInfo;
import com.atguigu.tingshu.user.mapper.UserInfoMapper;
import com.atguigu.tingshu.user.service.UserInfoService;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@SuppressWarnings({"all"})
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

	@Autowired
	private UserInfoMapper userInfoMapper;

    @Autowired
    private WxMaService wxMaService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private KafkaService kafkaService;

    /**
     * 小程序授权登录
     * @param code
     * @return
     */
    @Override
    public Map<String, String> wxLogin(String code) {
        // 需要返回的结果Map
        Map<String, String> resultMap= null;
        try {
            // 创建封装响应对象
            resultMap = new HashMap<>();
            // 调用微信认证接口 GET https://api.weixin.qq.com/sns/jscode2session
            WxMaJscode2SessionResult sessionInfo = wxMaService.getUserService().getSessionInfo(code);
            // 判断
            if(sessionInfo!=null){
                // 获取认证的openId
                String openid = sessionInfo.getOpenid();
                // 构建查询条件对象
                LambdaQueryWrapper<UserInfo> queryWrapper= new LambdaQueryWrapper<>();
                queryWrapper.eq(UserInfo::getWxOpenId,openid);
                // 根据openid查询用户信息:select*from user_info where wx_open_id=?
                UserInfo userInfo = userInfoMapper.selectOne(queryWrapper);
                // 判断是否注册还是登录
                if(userInfo==null){  // 此时注册
                    // 1.初始化用户信息
                    userInfo=new UserInfo();
                    userInfo.setWxOpenId(openid);
                    userInfo.setNickname("听友:"+ IdUtil.fastUUID());
                    userInfo.setAvatarUrl("https://oss.aliyuncs.com/aliyun_id_photo_bucket/default_handsome.jpg");
                    userInfo.setIsVip(0);
                    // 2.保存用户信息
                    userInfoMapper.insert(userInfo);
                    // 3.初始化用户账户,使用消息队列异步进行（service-account服务进行消费）
                    kafkaService.sendMessage(KafkaConstant.QUEUE_USER_REGISTER,userInfo.getId().toString());
                }
                // 1.生成token
                String token=IdUtil.getSnowflakeNextIdStr();

                // 2.定义存储key
                String loginKey= RedisConstant.USER_LOGIN_KEY_PREFIX+token;
                // 基于对安全信息控制，UserInfoVo
                UserInfoVo userInfoVo = BeanUtil.copyProperties(userInfo, UserInfoVo.class);

                // 3.存储用户信息到redis
                redisTemplate.opsForValue().set(loginKey,userInfoVo,RedisConstant.USER_LOGIN_KEY_TIMEOUT, TimeUnit.SECONDS);
                // 4.封装token，响应到前端
                resultMap.put("token",token);
            }
        } catch (WxErrorException e) {
            log.error("[用户服务]微信登录异常：{}", e);
            throw new RuntimeException(e);
        }
        return resultMap;
    }

    /**
     * 获取登录用户信息
     * @param userId
     * @return
     */
    @GuiguLogin
    @Override
    public UserInfoVo getUserInfo(Long userId) {
        // 获取用户信息
        UserInfo userInfo = userInfoMapper.selectById(userId);

        return BeanUtil.copyProperties(userInfo,UserInfoVo.class);
    }

    /**
     * 更新用户信息
     * @param userInfoVo
     */
    @GuiguLogin
    @Override
    public void updateUser(UserInfoVo userInfoVo, String token) {
        // 转换类型
        UserInfo userInfo = BeanUtil.copyProperties(userInfoVo, UserInfo.class);

        userInfoMapper.updateById(userInfo);

        // 获取最新数据
        UserInfoVo user = this.getUserInfo(userInfoVo.getId());

        String loginKey = RedisConstant.USER_LOGIN_KEY_PREFIX+token;

        redisTemplate.opsForValue().set(loginKey,user,RedisConstant.USER_LOGIN_KEY_TIMEOUT, TimeUnit.SECONDS);
    }
}
