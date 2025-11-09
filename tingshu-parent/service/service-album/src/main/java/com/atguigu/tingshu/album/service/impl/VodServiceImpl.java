package com.atguigu.tingshu.album.service.impl;

import com.alibaba.csp.sentinel.util.StringUtil;
import com.atguigu.tingshu.album.config.VodConstantProperties;
import com.atguigu.tingshu.album.service.VodService;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.common.util.UploadFileUtil;
import com.qcloud.vod.VodUploadClient;
import com.qcloud.vod.model.VodUploadRequest;
import com.qcloud.vod.model.VodUploadResponse;
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
}
