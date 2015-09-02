package com.pay.framework.payment.alipay;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import com.pay.framework.payment.alipay.config.AlipayConfig;
import com.pay.framework.payment.alipay.util.AlipaySubmit;




public class AlipayService {
    
    /**
     * 支付宝提供给商户的服务接入网关URL(新)
     */
    private static final String ALIPAY_GATEWAY_NEW = "https://mapi.alipay.com/gateway.do?";

    /**
     * 构造即时到帐批量退款无密接口
     * @param sParaTemp 请求参数集合
     * @return 支付宝返回XML处理结果
     * @throws Exception 
     */
    public static String refund_fastpay_by_platform_nopwd(Map<String, String> sParaTemp,String partner, String key) throws Exception {

    	//增加基本配置
        sParaTemp.put("service", "refund_fastpay_by_platform_nopwd");
        sParaTemp.put("partner", partner);
        //sParaTemp.put("notify_url", AlipayConfig.notify_url);
        sParaTemp.put("_input_charset", AlipayConfig.input_charset);
        return AlipaySubmit.sendPostInfo(sParaTemp, ALIPAY_GATEWAY_NEW,key);
    }
    
    /**
     * 构造即时到帐批量退款有密接口
     * @param sParaTemp 请求参数集合
     * @return 支付宝返回XML处理结果
     * @throws Exception 
     */
    public static String refund_fastpay_by_platform_pwd(String gateway,Map<String, String> sParaTemp,String partner, String key) throws Exception {

    	//增加基本配置
        sParaTemp.put("service", "refund_fastpay_by_platform_pwd");
        sParaTemp.put("partner", partner);
        //sParaTemp.put("notify_url", AlipayConfig.notify_url);
        sParaTemp.put("_input_charset", AlipayConfig.input_charset);
        //return AlipaySubmit.sendPostInfo(sParaTemp, ALIPAY_GATEWAY_NEW,key);
        return AlipaySubmit.buildForwardUrl(gateway, sParaTemp, key);
    }

    /**
     * 用于防钓鱼，调用接口query_timestamp来获取时间戳的处理函数
     * 注意：远程解析XML出错，与服务器是否支持SSL等配置有关
     * @return 时间戳字符串
     * @throws IOException
     * @throws DocumentException
     * @throws MalformedURLException
     */
    public static String query_timestamp(String partner) throws MalformedURLException,
                                                        DocumentException, IOException {

        //构造访问query_timestamp接口的URL串
        String strUrl = ALIPAY_GATEWAY_NEW + "service=query_timestamp&partner=" + partner;
        StringBuffer result = new StringBuffer();

        SAXReader reader = new SAXReader();
        Document doc = reader.read(new URL(strUrl).openStream());

        List<Node> nodeList = doc.selectNodes("//alipay/*");

        for (Node node : nodeList) {
            // 截取部分不需要解析的信息
            if (node.getName().equals("is_success") && node.getText().equals("T")) {
                // 判断是否有成功标示
                List<Node> nodeList1 = doc.selectNodes("//response/timestamp/*");
                for (Node node1 : nodeList1) {
                    result.append(node1.getText());
                }
            }
        }

        return result.toString();
    }
    
    /**
     * 构造账务明细查询接口
     * @param sParaTemp 请求参数集合
     * @return 表单提交HTML信息
     * @throws Exception 
     */
    public static String account_page_query(Map<String, String> sParaTemp,String partner,String key) throws Exception {

    	//增加基本配置
        sParaTemp.put("service", "account.page.query");
        sParaTemp.put("partner", partner);
        sParaTemp.put("_input_charset", AlipayConfig.input_charset);
        return AlipaySubmit.sendPostInfo(sParaTemp, ALIPAY_GATEWAY_NEW,key);
    }
}

