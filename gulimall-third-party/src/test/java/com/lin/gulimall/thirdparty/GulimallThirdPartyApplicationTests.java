package com.lin.gulimall.thirdparty;

import com.aliyun.oss.OSSClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@SpringBootTest
class GulimallThirdPartyApplicationTests {

    @Autowired
    private OSSClient ossClient;

    @Test
    void contextLoads() {
    }

    @Test
    public void uploadTest() throws FileNotFoundException {
        // 上传文件流。
        InputStream inputStream = new FileInputStream("D:\\project\\Guli\\参考\\Guli Mall(包含代码、课件、sql)\\Guli Mall\\分布式基础\\资源\\pics\\7ae0120ec27dc3a7.jpg");
        ossClient.putObject("gulimall-linjingdong", "7ae0120ec27dc3a7.jpg", inputStream);

        // 关闭OSSClient。
        ossClient.shutdown();
    }

}
