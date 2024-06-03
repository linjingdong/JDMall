package com.lin.gulimall.product;

import com.lin.gulimall.product.service.AttrGroupService;
import com.lin.gulimall.product.service.BrandService;
import com.lin.gulimall.product.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@Slf4j
@SpringBootTest
class GulimallProductApplicationTests {
    @Autowired
    BrandService brandService;
    @Autowired
    CategoryService categoryService;

    @Test
    public void testFindPath() {
        Long[] cateLogPath = categoryService.findCateLogPath(225L);
        log.info("完整路径为:" + Arrays.toString(cateLogPath));
    }

    @Test
    void contextLoads() throws FileNotFoundException {

    }
}
