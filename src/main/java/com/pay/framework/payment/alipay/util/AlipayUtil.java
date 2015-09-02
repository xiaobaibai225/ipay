package com.pay.framework.payment.alipay.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.pay.framework.payment.alipay.config.AlipayConfig;
import com.pay.framework.service.PayTypeService;
import com.pay.model.PaymentBean;

public class AlipayUtil {
	
	@Autowired
	private PayTypeService payTypeService;
	
	/**
	 * 支付宝一键支付查询接口
	 * @param orderform
	 * @param payment
	 * @param extMap
	 * @return
	 */
	public static String oneKeyContractQuery(PaymentBean payment,String external_sign_no,String partner,String key) {
		// //////////////////////////////////请求参数//////////////////////////////////////

		// 代扣项代码
		String item_code = "DEFAULT";
		// 必填，不需要修改
		// 协议代码
		String protocol_code = "b2c_charge";
		// 必填，不需要修改
		// 商户网站唯一签约号
		//String external_sign_no = external_sign_no;
		// 必填，与签约接口中同名的参数对应

		// ////////////////////////////////////////////////////////////////////////////////

		// 把请求参数打包成数组
//		AlipayConfig.partner = mid;
//		AlipayConfig.key = payment.getSeckey();
		Map<String, String> sParaTemp = new HashMap<String, String>();
		sParaTemp.put("service", "dut.customer.sign.query");
		sParaTemp.put("partner", partner);
		sParaTemp.put("_input_charset", AlipayConfig.input_charset);
		sParaTemp.put("item_code", item_code);
		sParaTemp.put("protocol_code", protocol_code);
		sParaTemp.put("external_sign_no", external_sign_no);

		// 建立请求
		String sHtmlText="";
		try {
		//	sHtmlText = AlipaySubmit.buildRequest("", "", sParaTemp,key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sHtmlText;
	}
	
	/**
	 * 返回16位随机数
	 * @return
	 */
	public static String getRandomExternalSignNo(){
		return RandomStringUtils.randomNumeric(8);
	}
}
