package com.lin.gulimall.seach.service;

import com.lin.gulimall.seach.vo.SearchParam;
import com.lin.gulimall.seach.vo.SearchResult;


/**
 * @Description TODO
 * @Date 2024/6/20 10:31
 * @Author Lin
 * @Version 1.0
 */
public interface MallSearchService {
    /**
     * @return ：返回检索的结果
     */
    SearchResult search(SearchParam param);
}
