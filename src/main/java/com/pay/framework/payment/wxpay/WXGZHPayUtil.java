package com.pay.framework.payment.wxpay;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.pay.framework.http.HttpRequester;
import com.pay.framework.http.HttpResponse;
import com.pay.model.PaymentBean;

public class WXGZHPayUtil {
	// 获取token
	public static String getWXToken(PaymentBean pay){
		String token = "";
		Map map = new HashMap();
		map.put("grant_type", "client_credential");
		map.put("appid",pay.getExt());
		map.put("secret","b114d0eca1f58fb252c9287fc3660408");
		String tokenUrl = "https://api.weixin.qq.com/cgi-bin/token";
		try {
			HttpRequester req=new HttpRequester();
			HttpResponse responseStr = req.sendGet(tokenUrl,map);
			
			token = responseStr.getContent();
			JSONArray objArr = JSONArray.fromObject(token);
			for(int i = 0; i < objArr.size(); i++){
				JSONObject jsonObject = objArr.getJSONObject(i);
				token = jsonObject.getString("access_token");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return token;
	}
	
	// 获取jsapi_ticket
	public static String getJsapiTicket(PaymentBean pay){
		String jsTicket = "";
		String token = getWXToken(pay);
		String jsTicketURL = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=%s&type=jsapi";
		String.format(jsTicketURL, token);
		// https 访问url 获得token
		try {
			HttpRequester req=new HttpRequester();
			HttpResponse responseStr = req.sendGet(jsTicketURL);
			jsTicket = responseStr.getContent();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsTicket;
	}
	
	public static void main(String[] args) {
		PaymentBean pay = new PaymentBean();
		pay.setExt("wx3de33fa2af6b94e1");
		pay.setSeckey("4ca4ff33c84cedf6fc58345f3e9e0e51");
		getJsapiTicket(pay);
	}
	
}
