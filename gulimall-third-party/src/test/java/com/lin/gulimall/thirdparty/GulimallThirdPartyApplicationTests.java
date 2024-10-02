package com.lin.gulimall.thirdparty;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileNotFoundException;

@SpringBootTest
public class GulimallThirdPartyApplicationTests {

    //    @Autowired
//    private OSSClient ossClient;
    @Value("${spring.cloud.alicloud.access-key}")
    private String accessKeyId;
    @Value("${spring.cloud.alicloud.secret-key}")
    private String secretKeyId;

    @Test
    public void sendMsgTest() throws Exception {
        Config config = new Config()
                // 您的AccessKey ID
                .setAccessKeyId(accessKeyId)
                // 您的AccessKey Secret
                .setAccessKeySecret(secretKeyId);
        // 访问的域名
        config.endpoint = "dysmsapi.aliyuncs.com";

        Client client = new Client(config);

        String code = "1600f";
        SendSmsRequest request = new SendSmsRequest()
                .setSignName("JD商城短信验证码")
                .setTemplateCode("SMS_469000016")
                .setPhoneNumbers("17817174032")
                .setTemplateParam("{\"code\":\"" + code + "\"}");


        System.out.println("{\"code\":\"" + code + "\"}");
        SendSmsResponse response = client.sendSms(request);
        System.out.println(response);

    }


    @Test
    public void uploadTest() throws FileNotFoundException {
//        // Endpoint以杭州为例，其它Region请按实际情况填写。
//        String endpoint = "oss-cn-guangzhou.aliyuncs.com";
//        // 云账号AccessKey有所有API访问权限，建议遵循阿里云安全最佳实践，创建并使用RAM子账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建。
//        String accessKeyId = "";
//        String accessKeySecret = "";
//
//        // 创建OSSClient实例。
//        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
//
//        // 上传文件流。
//        InputStream inputStream = new FileInputStream("D:\\project\\Guli\\参考\\Guli Mall(包含代码、课件、sql)\\Guli Mall\\分布式基础\\资源\\pics\\7ae0120ec27dc3a7.jpg");
//        ossClient.putObject("gulimall-linjingdong", "7ae0120ec27dc3.jpg", inputStream);
//
//        // 关闭OSSClient。
//        ossClient.shutdown();
    }
}
