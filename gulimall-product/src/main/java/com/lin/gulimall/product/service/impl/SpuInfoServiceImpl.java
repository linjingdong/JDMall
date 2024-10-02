package com.lin.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.lin.common.constant.ProductConstant;
import com.lin.common.to.SkuHasStockVo;
import com.lin.common.to.SkuReductionTo;
import com.lin.common.to.SpuBoundTo;
import com.lin.common.to.es.SkuEsModel;
import com.lin.common.utils.R;
import com.lin.gulimall.product.entity.*;
import com.lin.gulimall.product.feign.CouponFeignService;
import com.lin.gulimall.product.feign.SearchFeignService;
import com.lin.gulimall.product.feign.WareFeignService;
import com.lin.gulimall.product.service.*;
import com.lin.gulimall.product.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lin.common.utils.PageUtils;
import com.lin.common.utils.Query;

import com.lin.gulimall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


@Slf4j
@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {
    @Autowired
    private SpuInfoDescService spuInfoDescService;
    @Autowired
    private SpuImagesService spuImagesService;
    @Autowired
    private AttrService attrService;
    @Autowired
    private ProductAttrValueService productAttrValueService;
    @Autowired
    private SkuInfoService skuInfoService;
    @Autowired
    private SkuImagesService skuImagesService;
    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    private CouponFeignService couponFeignService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private WareFeignService wareFeignService;
    @Autowired
    private SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo spuSaveVo) {
        // 1、保存基本信息：pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuSaveVo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());

        this.saveBaseSpuInfo(spuInfoEntity);

        // 2、保存SPU的描述图片：pms_spu_info_desc
        List<String> decript = spuSaveVo.getDecript();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        spuInfoDescEntity.setDecript(String.join(",", decript));

        spuInfoDescService.saveSpuInfoDesc(spuInfoDescEntity);

        // 3、保存SPU的图片集：pms_spu_images
        List<String> images = spuSaveVo.getImages();
        spuImagesService.saveImages(spuInfoEntity.getId(), images);

        // 4、保存SPU的规格参数：pms_product_attr_value
        List<BaseAttrs> baseAttrs = spuSaveVo.getBaseAttrs();
        List<ProductAttrValueEntity> productAttrValueList = baseAttrs.stream().map(attr -> {
            ProductAttrValueEntity productAttrValue = new ProductAttrValueEntity();
            productAttrValue.setAttrId(attr.getAttrId());
            AttrEntity attrEntity = attrService.getById(attr.getAttrId());
            productAttrValue.setAttrName(attrEntity.getAttrName());
            productAttrValue.setAttrValue(attr.getAttrValues());
            productAttrValue.setQuickShow(attr.getShowDesc());
            productAttrValue.setSpuId(spuInfoEntity.getId());

            return productAttrValue;
        }).collect(Collectors.toList());

        productAttrValueService.saveProductAttr(productAttrValueList);

        // 5、保存SPU的积分信息：gulimall_sms
        Bounds bounds = spuSaveVo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds, spuBoundTo);
        spuBoundTo.setSpuId(spuInfoEntity.getId());
        R r = couponFeignService.saveSpuBounds(spuBoundTo);
        if (r.getCode() != 0) {
            log.error("远程保存spu积分信息失败");
        }

        // 6、保存当前SPU对应的所有SKU信息：
        List<Skus> skus = spuSaveVo.getSkus();
        if (!CollectionUtils.isEmpty(skus)) {
            skus.forEach(item -> {
                /*
                    private String skuName;
                    private BigDecimal price;
                    private String skuTitle;
                    private String skuSubtitle;
                **/
                String defaultImage = "";
                for (Images image : item.getImages()) {
                    if (image.getDefaultImg() == 1) {
                        defaultImage = image.getImgUrl();
                    }
                }

                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item, skuInfoEntity);
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setSkuDefaultImg(defaultImage);

                // 6.1）、SKU的基本信息：pms_sku_info
                skuInfoService.saveSkuInfo(skuInfoEntity);

                Long skuId = skuInfoEntity.getSkuId();

                List<SkuImagesEntity> skuImages = item.getImages().stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    return skuImagesEntity;
                }).filter(entity -> {
                    return StringUtils.isNotEmpty(entity.getImgUrl());
                }).collect(Collectors.toList());

                // 6.2）、SKU的图片信息：pms_sku_images
                skuImagesService.saveBatch(skuImages);

                // 6.3）、SKU的销售属性信息：pms_sku_sale_attr_value
                List<Attr> skuAttrs = item.getAttr();
                List<SkuSaleAttrValueEntity> skuAttrValueList = skuAttrs.stream().map(skuAttr -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(skuAttr, skuSaleAttrValueEntity);
                    skuSaleAttrValueEntity.setSkuId(skuId);

                    return skuSaleAttrValueEntity;
                }).collect(Collectors.toList());

                skuSaleAttrValueService.saveBatch(skuAttrValueList);

                // 6.4）、SKU的优惠、满减等信息：gulimall_sms
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item, skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if (skuReductionTo.getFullCount() > 0
                        || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) > 0) {
                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if (r1.getCode() != 0) {
                        log.error("远程保存sku优惠信息失败");
                    }
                }
            });
        }
    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.baseMapper.insert(spuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondtion(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> queryWrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        if (StringUtils.isNotEmpty(key)) {
            queryWrapper.and(wrapper -> {
                wrapper.eq("id", key).or().like("spu_name", key);
            });
        }

        String status = (String) params.get("status");
        if (StringUtils.isNotEmpty(status)) {
            queryWrapper.eq("publish_status", status);
        }

        String brandId = (String) params.get("brandId");
        if (StringUtils.isNotEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            queryWrapper.eq("brand_id", brandId);
        }

        String catelogId = (String) params.get("catelogId");
        if (StringUtils.isNotEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            queryWrapper.eq("catalog_id", catelogId);
        }


        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }


    @Override
    public void up(Integer spuId) {
        // 1.查出当前spuId对应的所有sku信息，品牌和名字
        List<SkuInfoEntity> skus = skuInfoService.getSkusBySpuId(spuId);

        // 收集SkuId
        List<Long> skuIdsList = skus.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());

        // 查询当前SKU当前的所有可以被用来检索的规格属性
        List<ProductAttrValueEntity> baseAttrsForEs = productAttrValueService.baseAttrListForSpu(String.valueOf(spuId)); // 查出所有的基于SPU的属性
        List<Long> attrIds = baseAttrsForEs.stream()
                .map(attr -> {
                    return attr.getAttrId();
                }).collect(Collectors.toList());
        List<Long> attrIdsForSearch = attrService.selectSearchAttrs(attrIds); // 查出可以被用来检索的属性

        Set<Long> searchIdsSet = new HashSet<>(attrIdsForSearch);
        List<SkuEsModel.Attrs> attrsListForEs = baseAttrsForEs.stream().filter(item -> {
            return searchIdsSet.contains(item.getAttrId()); // 用contains（）在所有属性当中过滤出可以用来检索的属性
        }).map(item -> {
            SkuEsModel.Attrs attrsForEs = new SkuEsModel.Attrs();
            BeanUtils.copyProperties(item, attrsForEs); // 拷贝为ES需要用的检索属性
            return attrsForEs;
        }).collect(Collectors.toList());

        // 发送远程调用，查看库存
        Map<Long, Boolean> stockMap = null;
        try {
            R r = wareFeignService.getSkusHasStock(skuIdsList);
            TypeReference<List<SkuHasStockVo>> typeReference = new TypeReference<List<SkuHasStockVo>>() {};
            stockMap = r.getData(typeReference).stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::isHasStock));
        } catch (Exception e) {
            log.error("库存服务查询异常：原因：{}" + e);
        }

        Map<Long, Boolean> finalStockMap = stockMap;
        // 2.封装每个sku的信息
        List<SkuEsModel> upProducts = skus.stream().map(sku -> {
            // 组装需要的数据
            SkuEsModel esModel = new SkuEsModel();
            BeanUtils.copyProperties(sku, esModel); // 属性对拷，但还有一些数据需要自己处理
            // 以下是自行处理的数据
            esModel.setSkuPrice(sku.getPrice());
            esModel.setSkuImg(sku.getSkuDefaultImg());
            // esModel.setHasStock():布尔值判断是否有库存(设置库存信息)
            if (finalStockMap == null) {
                esModel.setHasStock(true);
            } else {
                esModel.setHasStock(finalStockMap.get(sku.getSkuId()));
            }

            // esModel.setHotScore():热度评分（默认为0
            esModel.setHotScore(0L);

            // 处理品牌和分类的名字信息
            BrandEntity brandForEs = brandService.getById(esModel.getBrandId());
            esModel.setBrandName(brandForEs.getName());
            esModel.setBrandImg(brandForEs.getLogo());
            esModel.setCatalogName(categoryService.getById(esModel.getCatalogId()).getName());

            // 设置为ES需要用的检索属性
            esModel.setAttrs(attrsListForEs);

            return esModel;
        }).collect(Collectors.toList());

        // 将所有通过search服务发送给es进行保存
        R r = searchFeignService.productStatusUp(upProducts);

        if (r.getCode() == 0) {
            // 远程调用成功
            // 修改当前SPU的状态：pms_spu_info（publish_status）
            baseMapper.updateSpuStatus(spuId, ProductConstant.SpuStatus.SPU_UP.getCode());
        } else {
            // 远程调用失败
            // TODO:重复调用：接口幂等性？重试机制？
            /**
             * 1、构造请求数据，将对象转为json；
             *      RequestTemplate template = buildRequestTemplateFromArgs.create(avg);
             * 2、发送请求执行（执行成功后会解码响应数据）
             *      executeAndDecode(template);
             * 3、执行请求会有重试机制
             *      while(true) {
             *          try{
             *              executeAndDecode(template);
             *              } catch() {
             *                  try{
             *                      retryer.continueOrPropagate(e);} catch(Exception) {throw ex;}
             *                      continue;
             *              }
             *          }
             */
        }
    }
}