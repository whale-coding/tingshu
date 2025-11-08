package com.atguigu.tingshu.album.api;

import com.atguigu.tingshu.album.service.FileUploadService;
import com.atguigu.tingshu.common.result.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "上传管理接口")
@RestController
@RequestMapping("api/album")
public class FileUploadApiController {
    @Autowired
    private FileUploadService fileUploadService;

    /**
     * api/album/fileUpload
     * 文件上传
     * @param file 文件
     * @return 文件地址
     */
    @PostMapping("/fileUpload")
    public Result<String> fileUpload(MultipartFile file){

        String imgUrl=fileUploadService.fileUpload(file);

        return Result.ok(imgUrl);
    }

}
