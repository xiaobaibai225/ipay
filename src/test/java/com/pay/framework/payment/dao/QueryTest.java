package com.pay.framework.payment.dao;

import com.pay.framework.payment.BasePay;
import com.pay.framework.payment.alipay.Md5Encrypt;
import com.pay.framework.util.MD5Util;

public class QueryTest {

	public static void main(String[] args) {
		String corderid = "f27b77e74a3e917a014a3e9311030004";
		String companyid ="350001";
		
		String secKey = companyid + BasePay.SECKEY_SUFFIX;
	    String signedSecKey = MD5Util.MD5Encode(secKey, "UTF-8");
		String sign = Md5Encrypt.md5("corderid=" + corderid + "&"
				+ signedSecKey + "&companyid=" + companyid, "UTF-8");
		String queryurl = String.format("http://114.251.247.103:8086/ipay/pay/querystat?corderid=%s&companyid=%s&sign=%s", corderid,companyid, sign);
		System.out.println(queryurl);

	}

}
