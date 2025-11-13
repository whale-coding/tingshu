package com.atguigu.tingshu;

import com.atguigu.tingshu.search.service.SearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 专辑上架全量导入
 */
@SpringBootTest
public class BatchImportTest {

    @Autowired
    private SearchService searchService;

    @Test
    public void importAlbum(){
        for (int i = 0; i < 1602; i++) {
            try {
                searchService.upperAlbum(i+ 1L);
                System.out.println("导入专辑ID"+(i+1));
            } catch (Exception e) {
                continue;
            }
        }
    }
}
