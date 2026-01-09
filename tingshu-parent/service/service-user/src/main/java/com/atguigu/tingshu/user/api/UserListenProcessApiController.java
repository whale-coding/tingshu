package com.atguigu.tingshu.user.api;

import com.atguigu.tingshu.common.login.GuiguLogin;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.user.service.UserListenProcessService;
import com.atguigu.tingshu.vo.user.UserListenProcessVo;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@Tag(name = "用户声音播放进度管理接口")
@RestController
@RequestMapping("api/user")
@SuppressWarnings({"all"})
public class UserListenProcessApiController {

	@Autowired
	private UserListenProcessService userListenProcessService;

	/**
	 * api/user/userListenProcess/getTrackBreakSecond/{trackId}
	 * 获取声音的上次跳出时间
	 * @param trackId
	 * @return
	 */
	@GuiguLogin(required = false)
	@GetMapping("/userListenProcess/getTrackBreakSecond/{trackId}")
	public Result<BigDecimal> getTrackBreakSecond(@PathVariable Long trackId){
		// 获取用户ID
		Long userId = AuthContextHolder.getUserId();
		// 判断
		if(userId!=null){
			BigDecimal breakSecond=userListenProcessService.getTrackBreakSecond(trackId,userId);
			return Result.ok(breakSecond);
		}
		return Result.ok();
	}

	/**
	 * api/user/userListenProcess/updateListenProcess
	 * 更新播放进度
	 * @param userListenProcessVo
	 * @return
	 */
	@PostMapping("/userListenProcess/updateListenProcess")
	@GuiguLogin(required = false)
	public Result updateListenProcess(@RequestBody UserListenProcessVo userListenProcessVo){
		// 获取用户ID
		Long userId = AuthContextHolder.getUserId();
		if(userId!=null){
			userListenProcessService.updateListenProcess(userListenProcessVo,userId);
		}
		return Result.ok();
	}

	/**
	 * api/user/userListenProcess/getLatelyTrack
	 * 获取用户最近一次播放记录
	 * @return
	 */
	@GetMapping("/userListenProcess/getLatelyTrack")
	@GuiguLogin
	public Result<Map<String,Long>> getLatelyTrack(){
		// 获取用户ID
		Long userId = AuthContextHolder.getUserId();
		// 获取记录
		Map<String,Long> resultMap=userListenProcessService.getLatelyTrack(userId);
		return Result.ok(resultMap);
	}

}

