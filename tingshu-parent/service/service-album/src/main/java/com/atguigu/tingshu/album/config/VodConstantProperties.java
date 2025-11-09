package com.atguigu.tingshu.album.config;


import com.qcloud.vod.VodUploadClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix="vod") //读取节点
@Data
public class VodConstantProperties {

    private Integer appId;
    private String secretId;
    private String secretKey;
    //https://cloud.tencent.com/document/api/266/31756#.E5.9C.B0.E5.9F.9F.E5.88.97.E8.A1.A8
    private String region;
    private String procedure;
    private String tempPath;
    private String playKey;


    /**
     * 初始化腾讯客户端对象
     * @return 客户端对象
     */
    @Bean
    public VodUploadClient vodUploadClient(){
        return new VodUploadClient(secretId, secretKey);
    }
}
