package com.atguigu.tingshu.album.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.atguigu.tingshu.album.config.MinioConstantProperties;
import com.atguigu.tingshu.album.service.FileUploadService;
import com.atguigu.tingshu.common.execption.GuiguException;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;


/**
 * 文件上传service 实现类
 */
@Service
@Slf4j
public class FileUploadServiceImpl implements FileUploadService {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioConstantProperties properties;

    /**
     * 文件上传
     *
     * @param file 文件
     * @return 文件路径
     */
    @Override
    public String fileUpload(MultipartFile file) {
        // 要返回的url
        String url = null;

        try {
            // 业务校验验证文件是否为图片，借助ImageIO读取图片。继续判断图片文件后缀名是否符合要求，图片长宽
            BufferedImage read = ImageIO.read(file.getInputStream());
            //判断
            if (read == null) {
                throw new GuiguException(400, "图片格式非法！");
            }

            // 判断存储桶是否存在
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(properties.getBucketName()).build());
            if (!found) {
                // 不存在，创建
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(properties.getBucketName()).build());
            } else {
                System.out.println("Bucket '"+properties.getBucketName()+"' already exists.");
            }

            // 随机生成文件名称
            // 创建文件夹名称
            String folderName= "/"+DateUtil.today()+"/";
            // 获取后缀名
            String extName = FileUtil.extName(file.getOriginalFilename());
            // 生成随机文件名称
            String fileName= IdUtil.randomUUID();
            fileName=folderName+fileName+"."+extName;

            // 实现文件上传
            minioClient.putObject(
                    PutObjectArgs.builder().bucket(properties.getBucketName()).object(fileName).stream(
                                    file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());

            // 构建文件路径
            // http://192.168.254.156:9000/sph/2024-02-26/671660c0-b01d-4ea6-8428-691800c20926.jpg
            url = properties.getEndpointUrl()+"/"+properties.getBucketName()+fileName;
            log.info("文件上传成功，URL为：{}",url);
        } catch (Exception e) {
            log.error("文件上传失败！message: {}", e.getMessage());
            throw new RuntimeException(e);
        }
        return url;
    }
}
