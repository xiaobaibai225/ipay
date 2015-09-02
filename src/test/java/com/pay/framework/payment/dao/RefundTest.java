package com.pay.framework.payment.dao;

import java.math.BigDecimal;

import com.pay.framework.payment.BasePay;
import com.pay.framework.payment.alipay.Md5Encrypt;
import com.pay.framework.util.CommonUtil;
import com.pay.framework.util.MD5Util;

public class RefundTest {
	
	public static void main(String[] args)
	{
		String companyId="350001";
		String signedSecKey = MD5Util.MD5Encode(companyId + BasePay.SECKEY_SUFFIX, "UTF-8");
		String[] corderids = new String[] { "2015080800001" };
		//String payurl="http://soho3q.sohochina.com:8080"; 
	    //String payurl= "http://192.168.165.2:6020";
		//String payurl="http://114.251.247.103";
//		String payurl="http://www.soho3q.com";
		String payurl="http://192.168.220.35:8880";
		for (String corderid : corderids) {
			String sign = Md5Encrypt.md5(String.format("corderid=%s&%s&companyid=%s", corderid, signedSecKey, companyId));
			String refundurl = String.format("%s/ipay/pay/refund?corderid=%s&companyid=%s&newcorderid=%s&sign=%s&refund_fee=1000",payurl, corderid, companyId,CommonUtil.getOrderFormNumber(),sign);
			System.out.println(refundurl);
		}
		
		//window.open("http://www.soho3q.com/ipay/pay/misposRefund?corderid=1508131128344220&companyid=350001&newcorderid=20150820180816681366&sign=230a2eb037518147e4c37c65daddc807&refund_fee=1");
	}
}
