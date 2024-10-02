package com.lin.gulimall.seach.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.lin.common.to.es.SkuEsModel;
import com.lin.common.utils.R;
import com.lin.gulimall.seach.config.GuliMallElasticSearchConfig;
import com.lin.gulimall.seach.constant.EsConstant;
import com.lin.gulimall.seach.feign.ProductFeignService;
import com.lin.gulimall.seach.service.MallSearchService;
import com.lin.gulimall.seach.vo.AttrResponseVo;
import com.lin.gulimall.seach.vo.BrandVo;
import com.lin.gulimall.seach.vo.SearchParam;
import com.lin.gulimall.seach.vo.SearchResult;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description TODO
 * @Date 2024/6/20 10:31
 * @Author Lin
 * @Version 1.0
 */
@Service
public class MallSearchServiceImpl implements MallSearchService {
    @Autowired
    private RestHighLevelClient client;
    @Autowired
    private ProductFeignService productFeignService;

    @Override
    public SearchResult search(SearchParam param) {
        // 1、动态构建查询需要的DSL数据
        SearchResult result = null;

        // 2、准备检索数据
        SearchRequest searchRequest = buildSearchRequest(param);


        // 3、执行检索请求
        try {
            SearchResponse response = client.search(searchRequest, GuliMallElasticSearchConfig.COMMON_OPTIONS);

            // 4、分析响应数据并封装成我们需要的格式
            result = buildSearchResult(response, param);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    /**
     * 准备检索请求
     *
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParam param) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 过滤（按照属性，分类，品牌，价格区间，库存）
        // 1. 构建bool - query
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        // 1.1 must-模糊匹配
        if (!StringUtils.isEmpty(param.getKeyword())) {
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }
        // 1.2 bool - filter 按照三级分类查询
        if (param.getCatalog3Id() != null) {
            boolQuery.filter(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        }
        // 1.2 bool - filter 按照品牌id进行查询
        if (param.getBrandId() != null && !param.getBrandId().isEmpty()) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }
        // 1.2 bool - filter 按照品牌属性进行查询
        if (param.getAttrs() != null && !param.getAttrs().isEmpty()) {
            for (String attrStr : param.getAttrs()) {
                BoolQueryBuilder nestBoolQuery = QueryBuilders.boolQuery();
                String[] s = attrStr.split("_");
                String attrId = s[0];
                String[] attrValues = s[1].split(":");
                nestBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                nestBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
                // 封装到nest中，每一个都得生成一个nested查询
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestBoolQuery, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            }

        }

        // 1.2 bool - filter 按照库存是否有货进行查询
        if (param.getHasStock() != null) {
            boolQuery.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1));
        }

        // 1.2 bool - filter 按照价格区间进行查询
        if (!StringUtils.isEmpty(param.getSkuPrice())) {
            //skuPrice形式为：1_500或_500或500_
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("skuPrice");
            String[] price = Arrays.stream(param.getSkuPrice().split("_")).filter(s -> !s.isEmpty()).toArray(String[]::new);

            System.out.println(Arrays.toString(price));
            if (price.length == 2) {
                rangeQueryBuilder.gte(price[0]).lte(price[1]);
            } else if (price.length == 1) {
                if (param.getSkuPrice().startsWith("_")) {
                    rangeQueryBuilder.lte(price[0]);
                }
                if (param.getSkuPrice().endsWith("_")) {
                    rangeQueryBuilder.gte(price[0]);
                }
            }
            boolQuery.filter(rangeQueryBuilder);
        }

        sourceBuilder.query(boolQuery);

        // 排序，分页，高亮
        // 2.1 排序
        if (!StringUtils.isEmpty(param.getSort())) {
            String sort = param.getSort();
            String[] s = sort.split("_");
            SortOrder order = s[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            sourceBuilder.sort(s[0], order);
        }
        // 2.2 分页
        sourceBuilder.from((param.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
        sourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);

        // 2.3 高亮
        if (!StringUtils.isEmpty(param.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style = 'color:red'>");
            highlightBuilder.postTags("</b>");
            sourceBuilder.highlighter(highlightBuilder);
        }

        // 聚合分析
        // 1.品牌聚合
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brand_agg");
        brandAgg.field("brandId").size(50);
        // 1.1 品牌聚合的子聚合
        brandAgg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brandAgg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        sourceBuilder.aggregation(brandAgg);

        // 2. 分类聚合
        TermsAggregationBuilder catalogAgg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20);
        catalogAgg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        sourceBuilder.aggregation(catalogAgg);

        // 3. 属性聚合
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        // 聚合分析出当前所有attr_id的名字
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        // 聚合分析出当前所有attr_id的所有可能的属性值attrValue
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));

        attr_agg.subAggregation(attrIdAgg);
        sourceBuilder.aggregation(attr_agg);

        String string = sourceBuilder.toString();
        System.out.println("构建的DSL" + string);

        return new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, sourceBuilder);
    }

    /**
     * 封装检索结果
     *
     * @param response
     * @param param
     * @return
     */
    private SearchResult buildSearchResult(SearchResponse response, SearchParam param) {
        SearchResult result = new SearchResult();
        // 1、返回当前所有查询到的商品
        SearchHits hits = response.getHits();
        List<SkuEsModel> esModels = new ArrayList<>();
        if (hits.getHits() != null && hits.getHits().length > 0) {
            for (SearchHit hit : hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel skuEsModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                if (!StringUtils.isEmpty(param.getKeyword())) {
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String string = skuTitle.getFragments()[0].string();
                    skuEsModel.setSkuTitle(string);
                }
                esModels.add(skuEsModel);
            }
        }
        result.setProducts(esModels);

        // 2、返回当前所有商品涉及到的属性信息
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attrAgg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attrIdAgg.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            // 获取并设置属性的Id
            long attrId = bucket.getKeyAsNumber().longValue();
            attrVo.setAttrId(attrId);

            // 获取并设置属性的名字
            String attrName = ((ParsedStringTerms) bucket.getAggregations().get("attr_name_agg")).getBuckets().get(0).getKeyAsString();
            attrVo.setAttrName(attrName);

            // 获取属性的值
            List<? extends Terms.Bucket> attrValueAgg = ((ParsedStringTerms) bucket.getAggregations().get("attr_value_agg")).getBuckets();
            List<String> attrValueList = attrValueAgg.stream().map(item -> {
                return item.getKeyAsString();
            }).collect(Collectors.toList());
            attrVo.setAttrValue(attrValueList);

            attrVos.add(attrVo);
        }

        result.setAttrs(attrVos);

        // 3、返回当前所有商品涉及到的品牌信息
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        ParsedLongTerms brand_agg = response.getAggregations().get("brand_agg");
        for (Terms.Bucket bucket : brand_agg.getBuckets()) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            // 获取品牌的Id
            long brandId = bucket.getKeyAsNumber().longValue();
            brandVo.setBrandId(brandId);

            // 获取品牌的名字
            String catalogName = ((ParsedStringTerms) bucket.getAggregations().get("brand_name_agg")).getBuckets().get(0).getKeyAsString();
            brandVo.setBrandName(catalogName);

            // 获取品牌的图片
            String catalogImg = ((ParsedStringTerms) bucket.getAggregations().get("brand_img_agg")).getBuckets().get(0).getKeyAsString();
            brandVo.setBrandImg(catalogImg);

            brandVos.add(brandVo);
        }

        result.setBrands(brandVos);

        // 4、返回当前所有商品涉及到的分类信息
        ParsedLongTerms catalogAgg = response.getAggregations().get("catalog_agg");

        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        List<? extends Terms.Bucket> catalogAggBuckets = catalogAgg.getBuckets();
        for (Terms.Bucket catalogAggBucket : catalogAggBuckets) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            // 分类Id
            long catalogId = Long.parseLong(catalogAggBucket.getKeyAsString());
            catalogVo.setCatalogId(catalogId);

            // 分类名：在这个聚合的子聚合里面
            ParsedStringTerms catalog_name_agg = catalogAggBucket.getAggregations().get("catalog_name_agg");
            String catalogName = catalog_name_agg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catalogName);
            catalogVos.add(catalogVo);
        }
        result.setCatalogs(catalogVos);

        // 5、分页信息-页码
        result.setPageNum(param.getPageNum());

        // 6、分页信息-总记录数
        long total = hits.getTotalHits().value;
        result.setTotal(total);

        // 7、分页信息-总页码
        int totalInt = (int) total;
        int totalPages = totalInt % EsConstant.PRODUCT_PAGESIZE == 0 ? totalInt / EsConstant.PRODUCT_PAGESIZE : (totalInt / EsConstant.PRODUCT_PAGESIZE + 1);
        result.setTotalPages(totalPages);

        // 8、分页信息-导航页码
        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 0; i < totalPages; i++) {
            pageNavs.add(i + 1);
        }
        result.setPageNavs(pageNavs);

        if (param.getAttrs() != null && !param.getAttrs().isEmpty()) {
            // 9、构建面包屑导航功能
            List<SearchResult.NavVo> navVos = param.getAttrs().stream().map(attr -> {
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                String[] s = attr.split("_");
                // 1）、设置面包屑的值
                navVo.setNavValue(s[1]);
                // 2）、设置面包屑的名字(这里只知道Id，不知道属性的名字，所以我们需要远程调用到Product这个微服务用Id找到相应的属性名字)
                R r = productFeignService.attrInfo(Long.parseLong(s[0]));
                result.getAttrIds().add(Long.parseLong(s[0]));
                // 远程调用返回成功
                if (r.getCode() == 0) {
                    AttrResponseVo data = r.getDataByKey("attr", new TypeReference<AttrResponseVo>() {
                    });
                    navVo.setNavName(data.getAttrName());
                } else {
                    // 调用失败，直接封装属性的Id
                    navVo.setNavName(s[0]);
                }

                // 3）、取消了这个面包屑以后，我们要跳转到哪个地方，讲请求地址的url当前条件置空替换掉
                String replace = replaceQueryString(param, attr, "attrs");
                navVo.setLink("http://search.gulimall.com:8081/list.html?" + replace);
                return navVo;
            }).collect(Collectors.toList());
            result.setNavs(navVos);
        }

        // 品牌和分类
        if (param.getBrandId() != null && !param.getBrandId().isEmpty()) {
            List<SearchResult.NavVo> navs = result.getNavs();
            SearchResult.NavVo navVo = new SearchResult.NavVo();

            navVo.setNavName("品牌");

            // 要查询品牌的名字，所以需要远程调用
            R r = productFeignService.brandsInfo(param.getBrandId());
            if (r.getCode() == 0) {
                List<BrandVo> brands = r.getDataByKey("brands", new TypeReference<List<BrandVo>>() {
                });
                System.out.println(brands);

                String replace = "";
                StringBuffer buffer = new StringBuffer();
                for (BrandVo brand : brands) {
                    buffer.append(brand.getName());
                    replace = replaceQueryString(param, brand.getBrandId() + "", "brandId");
                }
                navVo.setNavValue(buffer.toString());
                navVo.setLink("http://search.gulimall.com:8081/list.html?" + replace);
            }

            navs.add(navVo);
        }

        // 不需要导航取消


        return result;
    }

    private static String replaceQueryString(SearchParam param, String value, String key) {
        String encode = null;
        try {
            encode = URLEncoder.encode(value, "UTF-8");
            encode = encode.replace("+", "%20"); // 浏览器对空格编码和java不一样
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return param.get_queryString().replace("&" + key + "=" + encode, "");
    }
}
