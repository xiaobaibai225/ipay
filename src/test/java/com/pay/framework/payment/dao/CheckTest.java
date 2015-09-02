package com.pay.framework.payment.dao;

import com.pay.framework.payment.BasePay;
import com.pay.framework.payment.alipay.Md5Encrypt;
import com.pay.framework.util.MD5Util;

public class CheckTest {

	public static void main(String[] args) {
		
		String start ="2015-08-03";
		String end = "2015-08-03";
		String companyid ="350001";
		String paytype="40001";
		String url="http://114.251.247.103";
		//String url="http://soho3q.sohochina.com:8080";
		
		String secKey = companyid + BasePay.SECKEY_SUFFIX;
	    String signedSecKey = MD5Util.MD5Encode(secKey, "UTF-8");
		StringBuffer md5Str = new StringBuffer("");
		md5Str.append("start=").append(start).append("&").append(signedSecKey).append("&end=").append(end).append("&companyid=").append(companyid);
		String sign = Md5Encrypt.md5(md5Str.toString());
		String checkurl = String.format("%s/ipay/pay/misposCheck?start=%s&end=%s&companyid=%s&sign=%s&paytype=%s", url,start,end, companyid,sign,paytype);
		System.out.println(checkurl);

	}

}
