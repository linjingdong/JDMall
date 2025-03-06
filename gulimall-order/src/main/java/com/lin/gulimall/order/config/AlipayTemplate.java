package com.lin.gulimall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.lin.gulimall.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private   String app_id = "9021000143679739";

    // 商户私钥，您的PKCS8格式RSA2私钥
    private  String merchant_private_key = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCdVEw88sZ4YaxLBPOZwuJn6eGyrooEKM/vRObpiloj3Otth6kvJ2UeSLk1sNJ27we6iraTfjnAPfs6nvsLLQSAihcf+ITSHrJR1XFPHq3wUWdQfcrpDqw2jk1Jbi59863MQwkfQSgcj1kcrevvG0ZJzJbZA9OvS738XgFa6sFe7W9Ud7pR+U5W30VFszr/rsk9ihpgXANl9WP8QVOpSyDctgophIsWCjcn5th+evw1b1NckE4cqaWjBWR7O9GyahT4j0KEIC9hhD87BuOrSBqwjCO4PG5s1Ob2w0hZc8E11UaGoB1y08D+O9a6sMCaQT9A5BlqmiogiV8wFmaST30vAgMBAAECggEAD2K0Be3T/OfIWpnEXNATJghYOwoYkymOlB6P2lzeTc2H9HEk2Win2dtQyXBeVKUxg0uXaTsZ2LP3ApeTg1dGrm9SrZy3GHlKBPizmQIuDmdXjCaRFlOKAdfOtRSiVUzkPSp9FOJhUtGt7EER5pMrP4JTaiuWovjsPmbPCR4WBPtZ+fSy+NRIAms3Ke64Dc7jScGJ4pauyhgM5fq4beCOhqFgXdkatzkEemCv1vMkrVAOLnQhPxWVtr0OTkBgQKfCvBKZmHgaGcSJnWWGsegBACOzUReG74kSagwyjUpXyd2Ny1sRfESxj8uxbHjZgjiiTJBy7bfvyBK15PFXRmgJgQKBgQDM/2mS8B73E1IbCTVleFeNDUTqaP+rdKuBWUNJ0GjvEIGUsF1Ch7VAUgsmfKkravFK+5hFeEzLoGhaeq9qxe0q9U5nBNLcCJ3v+6E97IE/hS0ptGwg972MMqbrN4eN2cBVJGXLGKMXn2ELtcJJOuca2L+spm/lyg1AoMHrcrVHQQKBgQDEeNFm86NDyIP0Yteg3JVN0Wkcd/UIn31z/bhR9j4rR4PyfQyUE6KWRn5CDx8fLrjZqgOCdlBv6A9bRorxpb80jfcDt8tyB4Ix7ncZZ8BPHuFqObRCyjYSyFIGPDW761i9vWzRKReLMYIb5h+3wlPFNnZUeUabajzFD0K8uKeYbwKBgQDAq53yeLThy8rzligVgpHnBH2tQ5po73LyU48mpTa62myv70L38Myt825/cdMd95lpvCZVxMeI6u6pe327gONz6LMTqDj2jTLEB2B47vxbUzQramPmaHJCfqjbkolZDTvzyg3SYQmIfkeb1e0RHsdvOCCOiK/K7aifAXjBbkJrAQKBgBHaNJTZQJWRK8RSEuvP/1UR2S44DDDbZSfb/xqbeGywU+H90N3HpvdmsmRIQXiw4yUMGMOLAqYYfQS7NTbd67DX0YEp+zeCTSdCVtodHlFSKgNf+Ow0uZMdVq6wW+WWbfrXsoKS7Eu+lOb6Exm8c6Yw27GzT2Ih1iHF0Debw8bLAoGAMDJ82v7qJoMQ81vbO0xNWRLw3E5fItXx8Dgyom46p7saT4lirtlXs4KMF0zVw0hvnUgaKHzn4g6E6d8pT7OtvEIkNXOAIRpOqBs/yvIX5J6mh3uqiwpvDY5Z1rpgs0yTti7elYp2LJeQxA6CSCSxtWiqjr8KbpBmQ6R4Jshkz9g=";
    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private  String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgyE83B3p2HDogTUfbG5OPLxZ3QGEib6zbt7hxX9VHA+9fV9/FAznQ4wugM9J7K4OF5X4zrMvPFuepl+YVFPk1TOHqZqK37V6KiGawZrUJECOvh0OW28a1xn1pqaHVD9wu2tnqXFEdiHlY0hDqSCR+GhTIkB5zKzeTk1T307u7+sOGvfUc0L+2WanhbvWSCcUoLkSck27rbwp+aR9AdJuV8UNiLpleYmWNt+GO6oR5RAn4HlpfRwmdCnMaVBCPTySJji1x7G9YXb1SSue4QNKpyr5HH6sOPVfQZL1jlCAFGAKPElnPX4zdvJtr0MhZX+esh+tctbTSO8vE1T/zWM91QIDAQAB";
    // 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    private  String notify_url = "http://wbxifg.natappfree.cc/payed/notify";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private  String return_url = "http://member.gulimall.com:8081/memberOrder.html";

    // 签名方式
    private  String sign_type = "RSA2";

    // 字符编码格式
    private  String charset = "utf-8";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private  String gatewayUrl = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";

    public  String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应："+result);

        return result;

    }
}
