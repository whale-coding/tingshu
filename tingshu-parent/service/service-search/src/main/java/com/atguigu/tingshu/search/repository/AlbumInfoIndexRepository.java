package com.atguigu.tingshu.search.repository;

import com.atguigu.tingshu.model.search.AlbumInfoIndex;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * AlbumInfoIndex对应的Repository
 */
public interface AlbumInfoIndexRepository  extends ElasticsearchRepository<AlbumInfoIndex,Long> {
}
