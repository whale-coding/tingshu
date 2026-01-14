package com.atguigu.tingshu.order.api;

import com.atguigu.tingshu.common.login.GuiguLogin;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.order.service.OrderInfoService;
import com.atguigu.tingshu.vo.order.OrderInfoVo;
import com.atguigu.tingshu.vo.order.TradeVo;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "订单管理")
@RestController
@RequestMapping("api/order")
@SuppressWarnings({"all"})
public class OrderInfoApiController {

	@Autowired
	private OrderInfoService orderInfoService;

	/**
	 * 订单确认
	 * api/order/orderInfo/trade
	 * @param tradeVo
	 * @return
	 */
	@PostMapping("/orderInfo/trade")
	@GuiguLogin
	public Result<OrderInfoVo> tradeData(@RequestBody TradeVo tradeVo){
		OrderInfoVo orderInfoVo=orderInfoService.tradeData(tradeVo);
		return Result.ok(orderInfoVo);
	}
}

