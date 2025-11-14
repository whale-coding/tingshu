package com.atguigu.tingshu.search.api;

import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.query.search.AlbumIndexQuery;
import com.atguigu.tingshu.search.service.SearchService;
import com.atguigu.tingshu.vo.search.AlbumSearchResponseVo;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "搜索专辑管理")
@RestController
@RequestMapping("api/search")
@SuppressWarnings({"all"})
public class SearchApiController {

    @Autowired
    private SearchService searchService;

    /**
     * 上架专辑-导入索引库
     * api/search/albumInfo/upperAlbum/{albumId}
     * @param albumId
     * @return
     * 主要用于测试，实际会使用消息队列调用service即可，controller用不到
     */
    @GetMapping("/albumInfo/upperAlbum/{albumId}")
    public Result upperAlbum(@PathVariable Long albumId){
        searchService.upperAlbum(albumId);
        return Result.ok();
    }

    /**
     * /api/search/albumInfo/lowerAlbum/{albumId}
     * 下架专辑-删除文档
     * @param albumId
     * @return
     * 主要用于测试，实际会使用消息队列调用service即可，controller用不到
     */
    @GetMapping("/albumInfo/lowerAlbum/{albumId}")
    public Result lowerAlbum(@PathVariable Long albumId){
        searchService.lowerAlbum(albumId);
        return Result.ok();
    }

    /**
     * 专辑检索
     * /api/search/albumInfo
     * @param albumIndexQuery
     * @return
     */
    @PostMapping("/albumInfo")
    public Result<AlbumSearchResponseVo> search(@RequestBody AlbumIndexQuery albumIndexQuery){
        AlbumSearchResponseVo searchResponseVo=searchService.search(albumIndexQuery);

        return Result.ok(searchResponseVo);
    }
}

