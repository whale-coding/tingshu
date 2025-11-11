package com.atguigu.tingshu.album.service;

import com.atguigu.tingshu.vo.album.TrackMediaInfoVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface VodService {
    /**
     * 上传声音
     * @param file 声音文件
     * @return
     */
    Map<String, String> uploadTrack(MultipartFile file);

    /**
     * 查询云点播声音信息
     * @param mediaFileId
     * @return
     */
    TrackMediaInfoVo getTrackMediaInfo(String mediaFileId);

    /**
     * 删除云点播声音
     * @param beforeMediaFileId
     */
    void deleteTrackMedia(String beforeMediaFileId);
}
