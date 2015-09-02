package com.pay.framework.payment.cmbpay;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cmb.MerchantCode;

public class CmbPaySubmit {
   
	/**
	 *  strKey	商户密钥
		strDate	订单日期
		strBranchId	开户分行号
		strCono	商户号
		strBillNo	订单号
		strAmount	订单金额
		strMerchantPara	商户自定义参数
		strMerchantUrl	商户接受通知的URL
		strPayerID	付款方用户标识。用来唯一标识商户的一个用户。长度限制为40字节以内。
		并不要求商户提供用户的注册名称，但需要保证一个用户对应一个UserID。
		商户可以通过某些转换，把用户名转换为一个UserID。比如商户可以把用户注册的“日期+时分秒毫秒”作为UserID。如果还有重复，可再加上用户的IP。
		空白表示匿名用户。
		strPayeeID	收款方的用户标识。生成规则同上。
		strClientIP	商户取得的客户端IP，如果有多个IP用逗号”,”分隔。长度限制为64字节。
		strGoodsType	商品类型，长度限制为8字节。
		strReserved	保留，长度限制为1024字节。

	 */
	public static String buildRequestString(String strKey, String strDate, 
			String strBranchId, String strCono,String strBillNo, 
			String strAmount,String strMerchantPara, String strMerchantUrl, 
			String strPayerID,String strPayeeID, String strClientIP, String strGoodsType, String strReserved
 ){
		//MerchantCode metchantCode = new MerchantCode();
		return MerchantCode.genMerchantCode(strKey,strDate,strBranchId, strCono,strBillNo,strAmount, strMerchantPara, strMerchantUrl, 
		strPayerID, strPayeeID,  strClientIP, strGoodsType,strReserved);
	}

}
