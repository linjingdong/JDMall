package com.lin.gulimall.seach;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lin.gulimall.seach.config.GuliMallElasticSearchConfig;
import lombok.Data;
import lombok.ToString;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class GulimallSeachApplicationTests {
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @ToString
    @Data
    static class Account {
        private int account_number;
        private int balance;
        private String firstname;
        private String lastname;
        private int age;
        private String gender;
        private String address;
        private String employer;
        private String email;
        private String city;
        private String state;
    }

    @Test
    void searchDataTest() throws IOException {
        // 1、创建检索
        SearchRequest searchRequest = new SearchRequest();
        // 指定索引
        searchRequest.indices("brank");
        // 指定DSL，检索条件 searchSourceBuilder（封装条件）
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 1）、构造检索条件
        searchSourceBuilder.query(QueryBuilders.matchQuery("address", "mill"));
        // 2）、按照年龄分布进行聚合
        TermsAggregationBuilder ageAgg = AggregationBuilders.terms("AgesAgg").field("age");
        searchSourceBuilder.aggregation(ageAgg);
        // 2）、按照平均薪资进行聚合
        AvgAggregationBuilder balanceAvgAgg = AggregationBuilders.avg("balanceAgg").field("balance");
        searchSourceBuilder.aggregation(balanceAvgAgg);
        // System.out.println(searchSourceBuilder);
        searchRequest.source(searchSourceBuilder);

        // 2、执行索引
        SearchResponse search = restHighLevelClient.search(searchRequest, GuliMallElasticSearchConfig.COMMON_OPTIONS);

        // 3、分析结果
        // System.out.println(search.toString());
        // 3.1）、获取所有命中查询的数据
        SearchHits hits = search.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            String sourceAsString = hit.getSourceAsString();
            Account account = JSON.parseObject(sourceAsString, Account.class);
            System.out.println("account:" + account.toString());
        }

        // 3.2）、获取所有检索的分析数据
        Aggregations aggregations = search.getAggregations();
        // for (Aggregation aggregation : aggregations.asList()) {
        //     System.out.println("当前聚合" + aggregation.getName());
        // }
        Terms agesAgg = aggregations.get("AgesAgg");
        for (Terms.Bucket bucket : agesAgg.getBuckets()) {
            System.out.println("年龄：" + bucket.getKeyAsString());
        }

        Avg balanceAgg = aggregations.get("balanceAgg");
        System.out.println(balanceAgg.getValueAsString());
    }

    @Test
    void indexDataTest() throws IOException {
        IndexRequest indexRequest = new IndexRequest("user");
        indexRequest.id("1"); // 数据的id
        User user = new User();
        user.setUserName("zhangsan");
        user.setGender("male");
        user.setAge(20);

        String jsonString = JSONObject.toJSONString(user);
        indexRequest.source(jsonString, XContentType.JSON); // 要保存的数据

        IndexResponse index = restHighLevelClient.index(indexRequest, GuliMallElasticSearchConfig.COMMON_OPTIONS);
        System.out.println(index);
    }

    @Data
    class User {
        private String userName;
        private String Gender;
        private Integer age;
    }

    @Test
    void contextLoads() {
        System.out.println(restHighLevelClient);
    }

}
