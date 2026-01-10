package com.atguigu.tingshu.album.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import com.atguigu.tingshu.album.mapper.AlbumAttributeValueMapper;
import com.atguigu.tingshu.album.mapper.AlbumInfoMapper;
import com.atguigu.tingshu.album.mapper.AlbumStatMapper;
import com.atguigu.tingshu.album.service.AlbumInfoService;
import com.atguigu.tingshu.common.constant.KafkaConstant;
import com.atguigu.tingshu.common.constant.RedisConstant;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.common.service.KafkaService;
import com.atguigu.tingshu.model.album.AlbumAttributeValue;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.AlbumStat;
import com.atguigu.tingshu.query.album.AlbumInfoQuery;
import com.atguigu.tingshu.vo.album.AlbumInfoVo;
import com.atguigu.tingshu.vo.album.AlbumListVo;
import com.atguigu.tingshu.vo.album.AlbumStatVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@SuppressWarnings({"all"})
public class AlbumInfoServiceImpl extends ServiceImpl<AlbumInfoMapper, AlbumInfo> implements AlbumInfoService {

	@Autowired
	private AlbumInfoMapper albumInfoMapper;

    @Autowired
    private AlbumAttributeValueMapper albumAttributeValueMapper;

    @Autowired
    private AlbumStatMapper albumStatMapper;

    @Autowired
    private KafkaService kafkaService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    /**
     *  新增专辑
     *  涉及到的表：album_info、album_attribute_value（外键album_id）、album_stat（外键album_id）
     *  因为涉及到多个表，因此需要事务@Transactional
     * @param albumInfoVo
     */
    // 默认处理RunTimeException,不能处理SQLException、IoException，因此设置rollbackFor = Exception.class，这样就可以处理了
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveAlbumInfo(AlbumInfoVo albumInfoVo, Long userId) {
        // 拷贝数据创建保存对象
        AlbumInfo albumInfo = BeanUtil.copyProperties(albumInfoVo, AlbumInfo.class);

        // 设置用户id
        albumInfo.setUserId(userId);
        // 设置免费试听集数
        albumInfo.setTracksForFree(5);
        // 设置审核通过--直接审核通过（方便测试通过）
        // 真实情况一定有后台管理员审核
        albumInfo.setStatus(SystemConstant.ALBUM_STATUS_PASS);

        // 保存album_info
        albumInfoMapper.insert(albumInfo);

        // 保存album_attribute_value
        List<AlbumAttributeValue> attributeValueVoList = albumInfo.getAlbumAttributeValueVoList();
        // 判断
        if(CollectionUtil.isEmpty(attributeValueVoList)){
            throw new GuiguException(400,"专辑信息不完整，没有属性信息");
        }
        // 保存album_attribute_value
        for (AlbumAttributeValue albumAttributeValue : attributeValueVoList) {
            albumAttributeValue.setAlbumId(albumInfo.getId());
            albumAttributeValueMapper.insert(albumAttributeValue);
        }

        // 保存album_stat
        this.saveAlbumStat(albumInfo.getId(),SystemConstant.ALBUM_STAT_PLAY,0);
        this.saveAlbumStat(albumInfo.getId(),SystemConstant.ALBUM_STAT_SUBSCRIBE,0);
        this.saveAlbumStat(albumInfo.getId(),SystemConstant.ALBUM_STAT_BUY,0);
        this.saveAlbumStat(albumInfo.getId(),SystemConstant.ALBUM_STAT_COMMENT,0);

        // 发送上架消息
        kafkaService.sendMessage(KafkaConstant.QUEUE_ALBUM_UPPER,albumInfo.getId().toString());
    }

    /**
     * 查看当前用户专辑分页列表
     * @param albumListVoPage
     * @param albumInfoQuery
     * @return
     */
    @Override
    public Page<AlbumListVo> findUserAlbumPage(Page<AlbumListVo> albumListVoPage, AlbumInfoQuery albumInfoQuery) {

        return albumInfoMapper.selectUserAlbumPage(albumListVoPage ,albumInfoQuery) ;
    }

    /**
     * 根据ID删除专辑
     * @param id
     * 涉及到的表： album_info、album_attribute_value、album_stat
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeAlbumInfo(Long id) {
        // 判断当前专辑下是否有声音
        AlbumInfo albumInfo = albumInfoMapper.selectById(id);
        if(albumInfo.getIncludeTrackCount()>0){
            throw new GuiguException(400,"当前专辑下有声音,请谨慎删除，先删除声音");
        }

        // 删除专辑
        albumInfoMapper.deleteById(id);

        // 删除专辑统计:delete from album_stat where album_id=?
        QueryWrapper<AlbumStat> albumStatQueryWrapper=new QueryWrapper<>();
        albumStatQueryWrapper.eq("album_id",id);
        albumStatMapper.delete(albumStatQueryWrapper);

        // 删除专辑属性
        QueryWrapper<AlbumAttributeValue> attributeValueQueryWrapper=new QueryWrapper<>();
        attributeValueQueryWrapper.eq("album_id",id);
        albumAttributeValueMapper.delete(attributeValueQueryWrapper);

        // 发送下架消息
        kafkaService.sendMessage(KafkaConstant.QUEUE_ALBUM_LOWER,id.toString());
    }

    /**
     * 根据ID查询专辑信息
     * @param id
     * @return
     */
    @Override
    @GuiGuCache(prefix="albumInfo:")  // 自定义注解方式实现缓存
    public AlbumInfo getAlbumInfo(Long id) {
        // redissson整合
		// return getAlbumInfoRedisson(id);

        // redis整合
		// return getAlbumInfoRedis(id);

        // 查询数据库
        return getAlbumInfoDB(id);
    }

    // 根据ID查询专辑信息 整合redisson 分布式锁
    private AlbumInfo getAlbumInfoRedisson(Long id) {
        try {
            // 定义获取数据的key
            String dataKey = RedisConstant.ALBUM_INFO_PREFIX + id;
            // 尝试从缓存中获取数据
            AlbumInfo albumInfo = (AlbumInfo) redisTemplate.opsForValue().get(dataKey);
            // 判断
            if(albumInfo!=null){
                return albumInfo;
            }
            // 定义锁key
            String lockKey=RedisConstant.ALBUM_LOCK_PREFIX+id+RedisConstant.CACHE_LOCK_SUFFIX;
            // 获取锁
            RLock lock = redissonClient.getLock(lockKey);
            // 加锁
            lock.lock();
            try {
                // 二次校验缓存
                albumInfo = (AlbumInfo) redisTemplate.opsForValue().get(dataKey);
                // 判断
                if(albumInfo!=null){
                    return albumInfo;
                }
                // 查询数据库
                albumInfo = this.getAlbumInfoDB(id);
                // 判断
                if(albumInfo==null){
                    albumInfo=new AlbumInfo();
                    redisTemplate.opsForValue().set(dataKey,albumInfo,RedisConstant.ALBUM_TEMPORARY_TIMEOUT, TimeUnit.SECONDS);
                    return albumInfo;
                }else{
                    redisTemplate.opsForValue().set(dataKey,albumInfo,RedisConstant.ALBUM_TIMEOUT,TimeUnit.SECONDS);
                    return albumInfo;
                }
            } finally {
                // 释放锁
                lock.unlock();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return getAlbumInfoDB(id);
        }
    }


    // 根据ID查询专辑信息 整合 redis分布式锁
    private AlbumInfo getAlbumInfoRedis(Long id) {
        try {
            // 定义key
            String dataKey= RedisConstant.ALBUM_INFO_PREFIX+id;
            // 尝试查询redis缓存
            AlbumInfo albumInfo = (AlbumInfo) redisTemplate.opsForValue().get(dataKey);
            // 判断
            if(albumInfo==null){
                // 没有查询到数据--先尝试获取锁
                // 生成uuid
                String lockValue = IdUtil.fastUUID();
                // 定义锁的key
                String lockKey=RedisConstant.ALBUM_LOCK_PREFIX+id;
                // 获取锁
                Boolean flag = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, RedisConstant.ALBUM_LOCK_EXPIRE_PX2, TimeUnit.SECONDS);
                // 判断
                if(flag){
                    try {
                        // 获取到了锁--查询mysql
                        albumInfo = this.getAlbumInfoDB(id);
                        // 判断是否查询到结果
                        if(albumInfo==null){
                            // 防止缓存穿透
                            albumInfo=new AlbumInfo();
                            // 存储到redis
                            redisTemplate.opsForValue().set(dataKey,albumInfo,RedisConstant.ALBUM_TEMPORARY_TIMEOUT,TimeUnit.SECONDS);
                            return albumInfo;
                        }else{
                            // 存储到redis
                            redisTemplate.opsForValue().set(dataKey,albumInfo,RedisConstant.ALBUM_TIMEOUT,TimeUnit.SECONDS);
                            return albumInfo;
                        }
                    } finally {
                        // 释放锁
                        // 定义lua脚本
                        String script="if redis.call(\"get\",KEYS[1]) == ARGV[1]\n" +
                                "then\n" +
                                "    return redis.call(\"del\",KEYS[1])\n" +
                                "else\n" +
                                "    return 0\n" +
                                "end";
                        // 创建一个脚本对象
                        DefaultRedisScript<Long> redisScript = new DefaultRedisScript();
                        // 设置返回值类型
                        redisScript.setResultType(Long.class);
                        // 设置脚本
                        redisScript.setScriptText(script);
                        // 发送lua脚本给redis
                        redisTemplate.execute(redisScript, Arrays.asList(lockKey),lockValue);
                    }
                }else{
                    try {
                        // 获取锁失败
                        Thread.sleep(100);
                        return getAlbumInfo(id);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }else{
                // 查询到了数据，直接返回
                return albumInfo;
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        // 兜底处理方案：Redis服务有问题，查询数据库
        return getAlbumInfoDB(id);
    }

    /*
    // 最原始版本，没有考虑到缓存问题
    @Override
    public AlbumInfo getAlbumInfo(Long id) {
        // 查询专辑信息
        AlbumInfo albumInfo = albumInfoMapper.selectById(id);
        // 查询专辑属性信息:select*from album_attribute_value where album_id=id
        LambdaQueryWrapper<AlbumAttributeValue> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(AlbumAttributeValue::getAlbumId,id);
        List<AlbumAttributeValue> attributeValueList = albumAttributeValueMapper.selectList(queryWrapper);

        //封装数据
        if(albumInfo!=null){
            albumInfo.setAlbumAttributeValueVoList(attributeValueList);
        }

        return albumInfo;
    }
     */

    /**
     * 抽取 根据ID查询专辑信息 查询数据库的操作
     * @param id
     * @return
     */
    private AlbumInfo getAlbumInfoDB(Long id) {
        // 查询专辑信息
        AlbumInfo albumInfo = albumInfoMapper.selectById(id);
        // 查询专辑属性信息
        LambdaQueryWrapper<AlbumAttributeValue> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(AlbumAttributeValue::getAlbumId, id);
        List<AlbumAttributeValue> attributeValueList = albumAttributeValueMapper.selectList(queryWrapper);
        // 封装数据
        if(albumInfo!=null){
            albumInfo.setAlbumAttributeValueVoList(attributeValueList);
        }
        return albumInfo;
    }

    /**
     *  修改专辑
     * @param albumInfoVo
     * @param id
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAlbumInfo(AlbumInfoVo albumInfoVo, Long id) {
        // 创建修改对象
        AlbumInfo albumInfo = BeanUtil.copyProperties(albumInfoVo, AlbumInfo.class);
        // 设置ID
        albumInfo.setId(id);
        albumInfo.setUpdateTime(new Date());
        // album_info
        albumInfoMapper.updateById(albumInfo);

        // 先删除属性数据: delete from album_attribute_value where album_id=?
        // 构建条件对象
        QueryWrapper<AlbumAttributeValue> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("album_id",id);
        // 删除
        albumAttributeValueMapper.delete(queryWrapper);

        // 添加属性数据
        // 保存album_attribute_value
        List<AlbumAttributeValue> attributeValueVoList = albumInfo.getAlbumAttributeValueVoList();

        // 判断
        if(CollectionUtil.isEmpty(attributeValueVoList)){
            throw new GuiguException(400,"专辑信息不完整，没有属性信息");
        }

        for (AlbumAttributeValue albumAttributeValue : attributeValueVoList) {
            albumAttributeValue.setAlbumId(albumInfo.getId());
            albumAttributeValueMapper.insert(albumAttributeValue);
        }

        if("1".equals(albumInfoVo.getIsOpen())){
            // 上架
            kafkaService.sendMessage(KafkaConstant.QUEUE_ALBUM_UPPER,albumInfo.getId().toString());
        }else{
            // 下架
            kafkaService.sendMessage(KafkaConstant.QUEUE_ALBUM_LOWER,albumInfo.getId().toString());
        }
    }

    /**
     * 获取当前用户全部专辑列表
     * @return
     */
    @Override
    public List<AlbumInfo> findUserAllAlbumList(Long userId) {
        // TODO 避免小程序响应1千多条记录导致卡顿 需要指定加载数量  limit 10
        // select id,album_title from album_info where user_id order by id desc  limit 10
        // 构建条件对象
        QueryWrapper<AlbumInfo> queryWrapper=new QueryWrapper<>();
        // 添加条件
        queryWrapper.eq("user_id",userId);
        // 排序
        queryWrapper.orderByDesc("id");
        // 过滤返回字段
        queryWrapper.select("id","album_title");
        // 选择返回条数
        queryWrapper.last(" limit 10 ");

        List<AlbumInfo> albumInfoList = albumInfoMapper.selectList(queryWrapper);

        return albumInfoList;
    }

    /**
     * 根据专辑ID获取专辑统计信息
     * @param albumId
     * @return
     */
    @Override
    public AlbumStatVo getAlbumStatVo(Long albumId) {
        return albumStatMapper.selectAlbumStatVo(albumId);
    }


    /**
     * 保存专辑统计信息
     * @param albumId
     * @param statType
     * @param statNum
     */
    private void saveAlbumStat(Long albumId, String statType, int statNum) {
        // 封装统计对象
        AlbumStat albumStat=new AlbumStat();
        albumStat.setAlbumId(albumId);
        albumStat.setStatType(statType);
        albumStat.setStatNum(statNum);

        albumStatMapper.insert(albumStat);
    }
}
