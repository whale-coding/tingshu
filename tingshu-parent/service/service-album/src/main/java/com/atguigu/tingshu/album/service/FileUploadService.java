package com.atguigu.tingshu.album.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传service
 */
public interface FileUploadService {
    /**
     * 文件上传
     * @param file 文件
     * @return 文件地址
     */
    String fileUpload(MultipartFile file);
}
