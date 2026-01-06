package com.atguigu.tingshu.search.repository;

import com.atguigu.tingshu.model.search.SuggestIndex;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * 关键字自动补充
 */
public interface SuggestIndexRepository  extends ElasticsearchRepository<SuggestIndex,String> {
}
