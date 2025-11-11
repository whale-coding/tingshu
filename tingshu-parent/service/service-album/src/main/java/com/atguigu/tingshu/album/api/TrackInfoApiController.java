package com.atguigu.tingshu.album.api;

import com.atguigu.tingshu.album.service.TrackInfoService;
import com.atguigu.tingshu.album.service.VodService;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.model.album.TrackInfo;
import com.atguigu.tingshu.query.album.TrackInfoQuery;
import com.atguigu.tingshu.vo.album.TrackInfoVo;
import com.atguigu.tingshu.vo.album.TrackListVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
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

    /**
     * 保存声音
     * api/album/trackInfo/saveTrackInfo
     * @param trackInfoVo
     * @return
     *
     */
    @PostMapping("/trackInfo/saveTrackInfo")
    public Result saveTrackInfo(@RequestBody TrackInfoVo trackInfoVo){
        // 获取用户id
        Long userId = AuthContextHolder.getUserId();

        // 调用service
        trackInfoService.saveTrackInfo(trackInfoVo,userId);

        return Result.ok();
    }

    /**
     * 获取当前用户声音分页列表
     * api/album/trackInfo/findUserTrackPage/{page}/{limit}
     * @param page
     * @param limit
     * @param trackInfoQuery
     * @return
     */
    @PostMapping("/trackInfo/findUserTrackPage/{page}/{limit}")
    public Result<Page<TrackListVo>> findUserTrackPage(@PathVariable Long page,
                                                       @PathVariable Long limit,
                                                       @RequestBody TrackInfoQuery trackInfoQuery){
        // 封装分页查询对象
        Page<TrackListVo> listVoPage=new Page<>(page,limit);
        // 封装用户id
        Long userId = AuthContextHolder.getUserId();
        trackInfoQuery.setUserId(userId);

        // 调用service
        listVoPage=trackInfoService.findUserTrackPage(listVoPage,trackInfoQuery);

        return Result.ok(listVoPage);
    }

    /**
     *查询声音信息
     * api/album/trackInfo/getTrackInfo/{id}
     * @param id
     * @return
     */
    @GetMapping("/trackInfo/getTrackInfo/{id}")
    public Result<TrackInfo>getTrackInfo(@PathVariable Long id ){

        return Result.ok(trackInfoService.getById(id));
    }

    /**
     *修改声音信息
     * api/album/trackInfo/updateTrackInfo/{id}
     * @param id
     * @param trackInfoVo
     * @return
     */
    @PutMapping("/trackInfo/updateTrackInfo/{id}")

    public Result updateTrackInfo(@PathVariable Long id,
                                  @RequestBody TrackInfoVo trackInfoVo){

        trackInfoService.updateTrackInfo(id,trackInfoVo);

        return Result.ok();
    }

    /**
     * 删除声音信息
     * api/album/trackInfo/removeTrackInfo/{id}
     * @param id
     * @return
     */
    @DeleteMapping("/trackInfo/removeTrackInfo/{id}")
    public Result removeTrackInfo(@PathVariable Long id ){
        trackInfoService.removeTrackInfo(id);

        return Result.ok();
    }
}

