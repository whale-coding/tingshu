package com.atguigu.tingshu.user.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSON;
import com.atguigu.tingshu.common.constant.KafkaConstant;
import com.atguigu.tingshu.common.constant.RedisConstant;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.common.service.KafkaService;
import com.atguigu.tingshu.common.util.MongoUtil;
import com.atguigu.tingshu.model.user.UserListenProcess;
import com.atguigu.tingshu.user.service.UserListenProcessService;
import com.atguigu.tingshu.vo.album.TrackStatMqVo;
import com.atguigu.tingshu.vo.user.UserListenProcessVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.query.Query;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@SuppressWarnings({"all"})
public class UserListenProcessServiceImpl implements UserListenProcessService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private KafkaService kafkaService;

	/**
	 * 获取声音的上次跳出时间
	 * @param trackId
	 * @return
	 */
	@Override
	public BigDecimal getTrackBreakSecond(Long trackId, Long userId) {
		// 构建查询条件
		// userid=? and trackid=?
		Query query = new Query();
		// 添加条件
		query.addCriteria(Criteria.where("userId").is(userId).and("trackId").is(trackId));
		// 最新的一条
		query.with(Sort.by(Sort.Direction.DESC,"updateTime"));
		// 默认查询用户对某一个声音的进度记录只有一条，但是前端有可能定时器在发送更新的时候出现阻塞
		query.limit(1);
		// 执行查询
		//MongoUtil.getCollectionName(MongoUtil.MongoCollectionEnum.USER_LISTEN_PROCESS,userId) 动态生成集合名称
		UserListenProcess userListenProcess = mongoTemplate.findOne(query, UserListenProcess.class, MongoUtil.getCollectionName(MongoUtil.MongoCollectionEnum.USER_LISTEN_PROCESS, userId));

		// 判断
		if(userListenProcess!=null){
			return userListenProcess.getBreakSecond();
		}
		return new BigDecimal(0.00);
	}

	/**
	 * 更新播放进度
	 * @param userListenProcessVo
	 * @param userId
	 */
	@Override
	public void updateListenProcess(UserListenProcessVo userListenProcessVo, Long userId) {
		// 查询是否已经存在当前用户播放声音的记录
		// 构建查询条件
		// userid=? and trackid=?
		Query query = new Query();
		// 添加条件
		query.addCriteria(Criteria.where("userId").is(userId).and("trackId").is(userListenProcessVo.getTrackId()));
		// 最新的一条
		query.with(Sort.by(Sort.Direction.DESC,"updateTime"));
		// 默认查询用户对某一个声音的进度记录只有一条，但是前端有可能定时器在发送更新的时候出现阻塞
		query.limit(1);

		// 执行查询
		// MongoUtil.getCollectionName(MongoUtil.MongoCollectionEnum.USER_LISTEN_PROCESS,userId) 动态生成集合名称
		UserListenProcess userListenProcess = mongoTemplate.findOne(query, UserListenProcess.class, MongoUtil.getCollectionName(MongoUtil.MongoCollectionEnum.USER_LISTEN_PROCESS, userId));
		// 判断
		if(userListenProcess==null){
			// 存储保存声音记录
			userListenProcess=new UserListenProcess();
			userListenProcess.setUserId(userId);
			userListenProcess.setAlbumId(userListenProcessVo.getAlbumId());
			userListenProcess.setTrackId(userListenProcessVo.getTrackId());
			userListenProcess.setBreakSecond(userListenProcessVo.getBreakSecond());
			userListenProcess.setIsShow(1);
			userListenProcess.setCreateTime(new Date());
			userListenProcess.setUpdateTime(new Date());
		}else{
			// 更新播放时间
			userListenProcess.setBreakSecond(userListenProcessVo.getBreakSecond());
			// 更新修改时间
			userListenProcess.setUpdateTime(new Date());
		}
		mongoTemplate.save(userListenProcess,MongoUtil.getCollectionName(MongoUtil.MongoCollectionEnum.USER_LISTEN_PROCESS,userId));

		// 防止重复发送
		String key= RedisConstant.USER_TRACK_REPEAT_STAT_PREFIX+userId+":"+userListenProcessVo.getTrackId();
		// 存储到redis setnx
		long ttl = DateUtil.endOfDay(new Date()).getTime() - System.currentTimeMillis();
		// System.currentTimeMillis()：获取当前时间的毫秒值 setnx
		Boolean flag = redisTemplate.opsForValue().setIfAbsent(key, userListenProcessVo.getTrackId(), ttl, TimeUnit.MILLISECONDS);
		// 判断
		if(flag){
			// 更新当前声音的播放量
			TrackStatMqVo trackStatMqVo=new TrackStatMqVo();
			trackStatMqVo.setBusinessNo(IdUtil.fastSimpleUUID());
			trackStatMqVo.setAlbumId(userListenProcessVo.getAlbumId());
			trackStatMqVo.setTrackId(userListenProcessVo.getTrackId());
			trackStatMqVo.setStatType(SystemConstant.TRACK_STAT_PLAY);
			trackStatMqVo.setCount(1);
			// 发送消息
			kafkaService.sendMessage(KafkaConstant.QUEUE_TRACK_STAT_UPDATE, JSON.toJSONString(trackStatMqVo));
		}
	}

	/**
	 * 获取声音的上次跳出时间
	 * @param trackId
	 * @return
	 */
	@Override
	public Map<String, Long> getLatelyTrack(Long userId) {
		// 创建封装结果对象
		Map<String, Long> resultMap=new HashMap<>();
		// 构建条件对象
		// where userId=? order by desc update_time limit 1
		Query query = new Query();
		// 添加条件
		query.addCriteria(Criteria.where("userId").is(userId));
		// 排序
		query.with(Sort.by(Sort.Direction.DESC,"updateTime"));
		// 限制获取结果
		query.limit(1);
		// 执行查询
		UserListenProcess userListenProcess = mongoTemplate.findOne(query, UserListenProcess.class, MongoUtil.getCollectionName(MongoUtil.MongoCollectionEnum.USER_LISTEN_PROCESS, userId));
		// 判断
		if(userListenProcess!=null){
			resultMap.put("trackId",userListenProcess.getTrackId());
			resultMap.put("albumId",userListenProcess.getAlbumId());
		}
		return resultMap;
	}
}
