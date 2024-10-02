package com.lin.gulimall.product;

import com.lin.gulimall.product.config.MyRedissonConfig;
import com.lin.gulimall.product.dao.AttrGroupDao;
import com.lin.gulimall.product.dao.SkuSaleAttrValueDao;
import com.lin.gulimall.product.service.BrandService;
import com.lin.gulimall.product.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.UUID;

@Slf4j
@SpringBootTest
class GulimallProductApplicationTests {
    @Autowired
    BrandService brandService;
    @Autowired
    CategoryService categoryService;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    MyRedissonConfig redissonConfig;
    @Autowired
    AttrGroupDao attrGroupDao;
    @Autowired
    SkuSaleAttrValueDao skuSaleAttrValueDao;

    @Test
    public void Test() {
//        List<SpuItemAttrGroupVo> attrGroupWithAttrBySpuId = attrGroupDao.getAttrGroupWithAttrBySpuId(8L, 225L);
//        System.out.println(attrGroupWithAttrBySpuId);
        System.out.println(skuSaleAttrValueDao.getSaleAttrsBySpuId(8L));
    }

    @Test
    public void redissonTest() {
        System.out.println(redissonConfig);
    }

    @Test
    public void testStringRedisTemplate() {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        ops.set("hello", "world" + UUID.randomUUID().toString());

        String hello = ops.get("hello");
        System.out.println("baocunde shuju wei " + hello);
    }

    @Test
    public void testFindPath() {
        Long[] cateLogPath = categoryService.findCateLogPath(225L);
        log.info("完整路径为:" + Arrays.toString(cateLogPath));
    }

    @Test
    void contextLoads() throws FileNotFoundException {

    }
}
