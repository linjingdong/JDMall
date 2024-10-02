package com.lin.gulimall.seach.service;

import com.lin.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

/**
 * @Description TODO
 * @Date 2024/6/15 17:45
 * @Author Lin
 * @Version 1.0
 */
public interface ElasticSaveService {
    boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException;
}
