package com.atguigu.tingshu.order.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.atguigu.tingshu.album.AlbumFeignClient;
import com.atguigu.tingshu.common.constant.RedisConstant;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.order.OrderInfo;
import com.atguigu.tingshu.model.user.VipServiceConfig;
import com.atguigu.tingshu.order.helper.SignHelper;
import com.atguigu.tingshu.order.mapper.OrderInfoMapper;
import com.atguigu.tingshu.order.service.OrderInfoService;
import com.atguigu.tingshu.user.client.UserFeignClient;
import com.atguigu.tingshu.vo.order.OrderDerateVo;
import com.atguigu.tingshu.vo.order.OrderDetailVo;
import com.atguigu.tingshu.vo.order.OrderInfoVo;
import com.atguigu.tingshu.vo.order.TradeVo;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import cn.hutool.core.lang.Assert;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@SuppressWarnings({"all"})
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private AlbumFeignClient albumFeignClient;

    /**
     * 订单确认
     * @param tradeVo
     * @return
     */
    @Override
    public OrderInfoVo tradeData(TradeVo tradeVo) {
        // 创建订单确认对象
        OrderInfoVo orderInfoVo = new OrderInfoVo();
        // 获取用户ID
        Long userId = AuthContextHolder.getUserId();
        // 获取用户选择的付费项目
        String itemType = tradeVo.getItemType();
        // 设置付费类型
        orderInfoVo.setItemType(itemType);
        // 定义封装的金额变量
        // 订单原始金额
        BigDecimal originalAmount = new BigDecimal("0.00");
        // 减免总金额
        BigDecimal derateAmount = new BigDecimal("0.00");
        // 订单总金额
        BigDecimal orderAmount = new BigDecimal("0.00");

        // 定义封装的详情集合
        // 1.订单详情集合
        List<OrderDetailVo> orderDetailVoList = new ArrayList<>();
        // 2.订单减免明细列表
        List<OrderDerateVo> orderDerateVoList=new ArrayList<>();

        // 判断 1003
        if (SystemConstant.ORDER_ITEM_TYPE_VIP.equals(itemType)){
            // vip确认
            // 根据用户选择的套餐查询套餐详情
            Long itemId = tradeVo.getItemId();
            // 查询数据
            VipServiceConfig vipServiceConfig = userFeignClient.getVipServiceConfig(itemId).getData();
            // 判断
            Assert.notNull(vipServiceConfig,"查询vip套餐异常，套餐id:{}",itemId);
            // 获取原价
            originalAmount = vipServiceConfig.getPrice();
            // 获取优惠后价格
            orderAmount=vipServiceConfig.getDiscountPrice();
            // 计算优惠价格
            derateAmount=originalAmount.subtract(orderAmount);
            // 封装订单明细
            OrderDetailVo orderDetailVo=new OrderDetailVo();
            orderDetailVo.setItemId(itemId);
            orderDetailVo.setItemName(vipServiceConfig.getName());
            orderDetailVo.setItemUrl(vipServiceConfig.getImageUrl());
            orderDetailVo.setItemPrice(originalAmount);
            orderDetailVoList.add(orderDetailVo);
            // 封装优惠明细
            OrderDerateVo orderDerateVo=new OrderDerateVo();
            orderDerateVo.setDerateType(SystemConstant.ORDER_DERATE_VIP_SERVICE_DISCOUNT);
            orderDerateVo.setDerateAmount(derateAmount);
            orderDerateVo.setRemarks("VIP限时优惠：" + derateAmount);
            // 添加到集合
            orderDerateVoList.add(orderDerateVo);
        }else if (SystemConstant.ORDER_ITEM_TYPE_ALBUM.equals(itemType)){
            // 专辑确认
            // 获取专辑ID
            Long albumId = tradeVo.getItemId();
            // 根据专辑ID查询是否购买过本专辑
            Result<Boolean> paidAlbum = userFeignClient.isPaidAlbum(albumId);
            Boolean isBuy = paidAlbum.getData();
            if(isBuy){
                throw new GuiguException(400,"已经购买过该专辑，请。。。。。");
            }
            // 获取专辑信息
            AlbumInfo albumInfo = albumFeignClient.getAlbumInfo(albumId).getData();
            Assert.notNull(albumInfo,"查询专辑异常，专辑ID:{}",albumId);
            // 获取用户信息
            UserInfoVo userInfoVo = userFeignClient.getUserInfoVo(userId).getData();
            Assert.notNull(userInfoVo,"查询用户异常，用户ID:{}",userId);
            // 获取金额
            originalAmount = albumInfo.getPrice();
            // 订单价格
            orderAmount=originalAmount;
            // 判断是否存在普通会员折扣
            if(albumInfo.getDiscount().intValue()!=-1){
                // 有普通用户折扣
                if(userInfoVo.getIsVip()==0){
                    orderAmount=originalAmount.multiply(albumInfo.getDiscount()).divide(new BigDecimal(10),2, RoundingMode.HALF_UP);
                }
                if(userInfoVo.getIsVip()==1&&new Date().after(userInfoVo.getVipExpireTime())){
                    orderAmount=originalAmount.multiply(albumInfo.getDiscount()).divide(new BigDecimal(10),2, RoundingMode.HALF_UP);
                }
            }
            // 判断是否存在vip折扣
            if(albumInfo.getVipDiscount().intValue()!=-1){
                // 判断是否为vip
                if(userInfoVo.getIsVip()==1&&new Date().before(userInfoVo.getVipExpireTime())){
                    orderAmount=originalAmount.multiply(albumInfo.getVipDiscount()).divide(new BigDecimal(10),2, RoundingMode.HALF_UP);
                }
            }
            // 计算优惠金额
            derateAmount=originalAmount.subtract(orderAmount);
            // 封装订单明细
            OrderDetailVo orderDetailVo=new OrderDetailVo();
            orderDetailVo.setItemId(albumId);
            orderDetailVo.setItemName(albumInfo.getAlbumTitle());
            orderDetailVo.setItemUrl(albumInfo.getCoverUrl());
            orderDetailVo.setItemPrice(originalAmount);
            orderDetailVoList.add(orderDetailVo);
            // 封装优惠集合列表
            if(derateAmount.intValue()>0){
                OrderDerateVo orderDerate=new OrderDerateVo();
                orderDerate.setDerateType(SystemConstant.ORDER_DERATE_ALBUM_DISCOUNT);
                orderDerate.setDerateAmount(derateAmount);
                orderDerate.setRemarks("专辑优惠："+derateAmount);
                orderDerateVoList.add(orderDerate);
            }
        }else if (SystemConstant.ORDER_ITEM_TYPE_TRACK.equals(itemType)){
            // 声音确认
        }

        // 设置金额
        orderInfoVo.setOriginalAmount(originalAmount);
        orderInfoVo.setDerateAmount(derateAmount);
        orderInfoVo.setOrderAmount(orderAmount);
        // 设置集合
        orderInfoVo.setOrderDetailVoList(orderDetailVoList);
        orderInfoVo.setOrderDerateVoList(orderDerateVoList);

        // 统一处理参数
        // tradeNo交易号处理--防止订单重复提交的
        String tradeNo = IdUtil.fastUUID();
        // 定义交易号存储key
        String tradeKey= RedisConstant.ORDER_TRADE_NO_PREFIX+ userId;
        // 存储数据redis
        redisTemplate.opsForValue().set(tradeKey,tradeNo,5, TimeUnit.MINUTES);
        // 设置交易号
        orderInfoVo.setTradeNo(tradeNo);

        // 设置时间戳
        orderInfoVo.setTimestamp(DateUtil.current());
        // 将对象转换成map集合
        Map<String, Object> beanToMap = BeanUtil.beanToMap(orderInfoVo, false, true);
        // 生成签名--防止订单确认后的信息被篡改
        String sign = SignHelper.getSign(beanToMap);
        // 设置签名
        orderInfoVo.setSign(sign);

        return orderInfoVo;
    }
}
