package com.pay.framework.payment.dao;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.pay.framework.payment.BasePay;
import com.pay.framework.payment.alipay.Md5Encrypt;
import com.pay.framework.payment.icbcpay.ICBCPaySubmit;
import com.pay.framework.service.OrderFormService;
import com.pay.framework.service.PayService;
import com.pay.framework.util.MD5Util;
import com.pay.model.OrderFormPay;
import com.pay.model.PaymentBean;
import com.pay.model.Refund;

public class ICBCCheckTest {

	@Autowired
	private static OrderFormService orderFormService;

	@Autowired
	private static PayService payService;
	
	public static void main(String[] args) {
		
		
		/*
		String start ="2015-01-05";
		String end = "2015-01-06";
		String companyid ="350001";
		String paytype="17";
		//String url="http://114.251.247.103:8086";
		String url="http://soho3q.sohochina.com:8080";
		
		String secKey = companyid + BasePay.SECKEY_SUFFIX;
	    String signedSecKey = MD5Util.MD5Encode(secKey, "UTF-8");
		StringBuffer md5Str = new StringBuffer("");
		md5Str.append("start=").append(start).append("&").append(signedSecKey).append("&end=").append(end).append("&companyid=").append(companyid);
		String sign = Md5Encrypt.md5(md5Str.toString());
		String checkurl = String.format("%s/ipay/pay/check?start=%s&end=%s&companyid=%s&sign=%s&paytype=%s", url,start,end, companyid,sign,paytype);
		System.out.println(checkurl);
		*/
		Refund refund = new Refund();
		refund.setOuterfundno("201504230000001");
		refund.setOrdernumber("20150423101751385966");
		
		PaymentBean payment = new PaymentBean();//15
		payment.setExt7("B2CEJEINF");
		payment.setExt6("020000459999AAA");
		payment.setExt5("102");
		payment.setExt4("test20141223-1.y.0200");
		payment.setExt3("501");
		payment.setExt2("500");
		payment.setExt1("192.168.180.10");
		payment.setMerchantid("0200EC24375827");
		payment.setQuerykey("0200004519000100173");
		
		
		OrderFormPay orderForPay = new OrderFormPay();
		orderForPay.setTranseq("HFG000006161875939");
		
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat sdf2 = new SimpleDateFormat("HHmmssSSSSSS");//TODO 精确到毫秒
		Date datenow = new Date();
		
		// 把请求参数打包成数组
		Map<String, String> sParaTemp = new HashMap<String, String>();
		sParaTemp.put("TransCode", payment.getExt7());
		sParaTemp.put("CIS",payment.getExt6());
		sParaTemp.put("BankCode", payment.getExt5());
		sParaTemp.put("ID", payment.getExt4());
		sParaTemp.put("TranDate", sdf1.format(datenow));
		sParaTemp.put("TranTime", sdf2.format(datenow));
		sParaTemp.put("fSeqno", refund.getOuterfundno());
		sParaTemp.put("Ordertype", "0");
		sParaTemp.put("ShopType", "B2C");
		sParaTemp.put("ShopCode", payment.getMerchantid());
		sParaTemp.put("ShopAcct", payment.getQuerykey());
		sParaTemp.put("QrySerialNo", "");
		sParaTemp.put("QryOrderNum", refund.getOrdernumber());
		sParaTemp.put("BeginDate", "");
		sParaTemp.put("EndDate", "");
		sParaTemp.put("ResultType", "010");
		sParaTemp.put("PTOrderNo", orderForPay.getTranseq());
		sParaTemp.put("NextTag", "");
		sParaTemp.put("IfSeqno", "");
		sParaTemp.put("AcctSeq", "");
		String errorMsg = ICBCPaySubmit.refundSocketEBSS(sParaTemp, payment);

	}

}
