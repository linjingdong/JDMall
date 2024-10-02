package com.lin.gulimall.thirdparty.component;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 阿里云短信发送、短信验证
 */
@Component
public class SmsComponent {

    @Value("${spring.cloud.alicloud.access-key}")
    private String accessKeyId;
    @Value("${spring.cloud.alicloud.secret-key}")
    private String secretKeyId;

    public void sendSms(String phone, String code) throws Exception {
        // 发送短信为实现，这里模拟发送短信
        System.out.println("发送验证码，手机号为：" + phone + "验证码为：" + code);

        Config config = new Config()
                // 您的AccessKey ID
                .setAccessKeyId(accessKeyId)
                // 您的AccessKey Secret
                .setAccessKeySecret(secretKeyId);
        // 访问的域名
        config.endpoint = "dysmsapi.aliyuncs.com";

        Client client = new Client(config);

        SendSmsRequest request = new SendSmsRequest()
                .setSignName("JD商城短信验证码")
                .setTemplateCode("SMS_469000016")
                .setPhoneNumbers(phone)
                .setTemplateParam("{\"code\":\"" + code + "\"}");


        SendSmsResponse response = client.sendSms(request);
        System.out.println(response);
    }
}
