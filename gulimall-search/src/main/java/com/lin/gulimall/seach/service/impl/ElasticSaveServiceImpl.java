package com.lin.gulimall.seach.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.lin.common.to.es.SkuEsModel;
import com.lin.gulimall.seach.config.GuliMallElasticSearchConfig;
import com.lin.gulimall.seach.constant.EsConstant;
import com.lin.gulimall.seach.service.ElasticSaveService;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description TODO
 * @Date 2024/6/15 17:47
 * @Author Lin
 * @Version 1.0
 */
@Slf4j
@Service
public class ElasticSaveServiceImpl implements ElasticSaveService {
    @Autowired
    RestHighLevelClient restHighLevelClient;

    // 将商品的数据保存到ES中
    @Override
    public boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException {
        // 1、给ES建立索引：product（建立好映射关系）

        // 2、给ES中保存这些数据
        BulkRequest bulkRequest = new BulkRequest();
        for (SkuEsModel skuEsModel : skuEsModels) {
            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);
            indexRequest.id(skuEsModel.getSkuId().toString());
            String modelToJSON = JSONObject.toJSONString(skuEsModel);
            indexRequest.source(modelToJSON, XContentType.JSON);
            bulkRequest.add(indexRequest);
        }

        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, GuliMallElasticSearchConfig.COMMON_OPTIONS);

        // TODO：1、如果批量发生了错误
        boolean b = bulk.hasFailures();
        List<String> collect = Arrays.stream(bulk.getItems()).map(item -> {
            return item.getId();
        }).collect(Collectors.toList());
        log.info("商品上架完成：{}, 返回数据：{}", collect, bulk.toString());

        return b;
    }
}
