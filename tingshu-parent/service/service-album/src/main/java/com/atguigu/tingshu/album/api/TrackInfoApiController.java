package com.atguigu.tingshu.album.api;

import com.atguigu.tingshu.album.service.TrackInfoService;
import com.atguigu.tingshu.album.service.VodService;
import com.atguigu.tingshu.common.result.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Tag(name = "声音管理")
@RestController
@RequestMapping("api/album")
@SuppressWarnings({"all"})
public class TrackInfoApiController {

	@Autowired
	private TrackInfoService trackInfoService;

    @Autowired
    private VodService vodService;

    /**
     * 上传声音
     * api/album/trackInfo/uploadTrack
     * @param file
     * @return
     */
    @PostMapping("/trackInfo/uploadTrack")
    public Result<Map<String,String>> uploadTrack(MultipartFile file){
        Map<String,String> resultMap=vodService.uploadTrack(file);

        return Result.ok(resultMap);
    }
}

