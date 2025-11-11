package com.atguigu.tingshu.album.service.impl;

import com.alibaba.csp.sentinel.util.StringUtil;
import com.atguigu.tingshu.album.config.VodConstantProperties;
import com.atguigu.tingshu.album.service.VodService;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.common.util.UploadFileUtil;
import com.atguigu.tingshu.vo.album.TrackMediaInfoVo;
import com.qcloud.vod.VodUploadClient;
import com.qcloud.vod.model.VodUploadRequest;
import com.qcloud.vod.model.VodUploadResponse;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.vod.v20180717.VodClient;
import com.tencentcloudapi.vod.v20180717.models.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class VodServiceImpl implements VodService {

    @Autowired
    private VodConstantProperties vodConstantProperties;

    @Autowired
    private VodUploadClient vodUploadClient;

    /**
     * 上传声音
     * @param file 声音文件
     * @return
     */
    @Override
    public Map<String, String> uploadTrack(MultipartFile file) {
        // 结果Map
        Map<String, String> resultMap = null;

        try {
            // 创建对象封装数据
            resultMap = new HashMap<>();

            // 保存数据流到本地，并返回本地存储地址
            String tempPath = UploadFileUtil.uploadTempPath(vodConstantProperties.getTempPath(), file);
            // 判断
            if (StringUtil.isEmpty(tempPath)) {
                throw new GuiguException(400, "当前上传声音为空");
            }

            // 创建客户端对象
            // 创建请求对象
            VodUploadRequest request = new VodUploadRequest();
            // 设置媒体本地上传路径
            request.setMediaFilePath(tempPath);
            // 上传声音
            VodUploadResponse response = vodUploadClient.upload("ap-guangzhou", request);

            // 获取上传后的声音ID和声音播放地址
            String fileId = response.getFileId();
            String mediaUrl = response.getMediaUrl();

            resultMap.put("mediaFileId", fileId);
            resultMap.put("mediaUrl", mediaUrl);

            log.info("[专辑服务]上传音频文件到点播平台mediaFileId:{},mediaUrl:{}",fileId,mediaUrl);
        } catch (Exception e) {
            log.error("[专辑服务]上传音频文件到点播平台异常：文件：{}，错误信息：{}", file, e.getMessage());
            throw new RuntimeException(e);
        }

        return resultMap;
    }

    /**
     * 查询云点播声音信息
     * @param mediaFileId
     * @return
     */
    @Override
    public TrackMediaInfoVo getTrackMediaInfo(String mediaFileId) {
        // 创建封装对象
        TrackMediaInfoVo trackMediaInfoVo =new TrackMediaInfoVo();
        try{
            // 实例化一个认证对象，入参需要传入腾讯云账户 SecretId 和 SecretKey，此处还需注意密钥对的保密
            // 代码泄露可能会导致 SecretId 和 SecretKey 泄露，并威胁账号下所有资源的安全性。以下代码示例仅供参考，建议采用更安全的方式来使用密钥，请参见：https://cloud.tencent.com/document/product/1278/85305
            // 密钥可前往官网控制台 https://console.cloud.tencent.com/cam/capi 进行获取
            Credential cred = new Credential(vodConstantProperties.getSecretId(), vodConstantProperties.getSecretKey());


            // 实例化要请求产品的client对象
            VodClient client = new VodClient(cred,vodConstantProperties.getRegion());
            // 实例化一个请求对象,每个接口都会对应一个request对象
            DescribeMediaInfosRequest req = new DescribeMediaInfosRequest();
            // 封装声音唯一标识
            String[] fileIds1 = {mediaFileId};
            // 设置声音id请求参数
            req.setFileIds(fileIds1);
            // 返回的resp是一个DescribeMediaInfosResponse的实例，与请求对象对应
            DescribeMediaInfosResponse resp = client.DescribeMediaInfos(req);
            // 获取声音详情数据。封装
            if(resp!=null){
                // 获取媒资信息集合
                MediaInfo[] mediaInfoSet = resp.getMediaInfoSet();
                // 判断
                if(mediaInfoSet!=null &&mediaInfoSet.length>0){
                    // 获取媒资信息对象
                    MediaInfo mediaInfo = mediaInfoSet[0];
                    // 获取声音类型
                    String type = mediaInfo.getBasicInfo().getType();
                    // 获取声音大小
                    Long size = mediaInfo.getMetaData().getSize();
                    // 获取声音时长
                    Float duration = mediaInfo.getMetaData().getDuration();

                    trackMediaInfoVo.setType(type);
                    trackMediaInfoVo.setSize(size);
                    trackMediaInfoVo.setDuration(duration);
                }
            }
        } catch (Exception e) {
            log.error("[专辑服务]获取点播平台文件：{}，详情异常：{}", mediaFileId, e.getMessage());
            throw  new GuiguException(400,"获取云点播声音信息异常"+e.getMessage());
        }
        return trackMediaInfoVo;
    }

    /**
     * 删除云点播声音
     * @param beforeMediaFileId
     */
    @Override
    public void deleteTrackMedia(String beforeMediaFileId) {
        try {
            // 密钥可前往官网控制台 https://console.cloud.tencent.com/cam/capi 进行获取
            Credential cred = new Credential(vodConstantProperties.getSecretId(), vodConstantProperties.getSecretKey());

            // 实例化要请求产品的client对象,clientProfile是可选的
            VodClient client = new VodClient(cred, vodConstantProperties.getRegion());
            // 实例化一个请求对象,每个接口都会对应一个request对象
            DeleteMediaRequest req = new DeleteMediaRequest();
            req.setFileId(beforeMediaFileId);
            // 返回的resp是一个DeleteMediaResponse的实例，与请求对象对应
            DeleteMediaResponse resp = client.DeleteMedia(req);
        } catch (TencentCloudSDKException e) {
            log.info("[专辑服务]删除云点播文件：{}，失败{}", beforeMediaFileId, e.getMessage());
            throw new GuiguException(400,"删除声音异常"+e.getMessage());
        }
    }
}
