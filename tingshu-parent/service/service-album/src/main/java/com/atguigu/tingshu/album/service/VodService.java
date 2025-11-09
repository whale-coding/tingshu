package com.atguigu.tingshu.album.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface VodService {
    /**
     * 上传声音
     * @param file 声音文件
     * @return
     */
    Map<String, String> uploadTrack(MultipartFile file);
}
