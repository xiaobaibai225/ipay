package com.pay.framework.payment.ccbpay;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.httpclient.NameValuePair;

import com.pay.framework.log.LogManager;
import com.pay.framework.payment.alipay.util.httpClient.HttpProtocolHandler;
import com.pay.framework.payment.alipay.util.httpClient.HttpRequest;
import com.pay.framework.payment.alipay.util.httpClient.HttpResponse;
import com.pay.framework.payment.alipay.util.httpClient.HttpResultType;

/**
 * 建行支付工具类
 * @author zidanezhang
 *
 */
public class CCBPayBtoBUtil {
	
	

	/**
	 * 构造HTTP POST交易表单的方法示例
	 * 
	 * @param action
	 *            表单提交地址
	 * @param hiddens
	 *            以MAP形式存储的表单键值
	 * @return 构造好的HTTP POST交易表单
	 */
	public static String createHtml(String action, Map<String, String> hiddens) {
		StringBuffer sf = new StringBuffer();
		sf.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/></head><body>");
		sf.append("<form id = \"ccbpaybtobForm\" action=\"" + action
				+ "\" method=\"post\">");
		if (null != hiddens && 0 != hiddens.size()) {
			Set<Entry<String, String>> set = hiddens.entrySet();
			Iterator<Entry<String, String>> it = set.iterator();
			while (it.hasNext()) {
				Entry<String, String> ey = it.next();
				String key = ey.getKey();
				String value = ey.getValue();
				sf.append("<input type=\"hidden\" name=\"" + key + "\" id=\""
						+ key + "\" value=\"" + value + "\"/>");
			}
		}
		sf.append("</form>");
		sf.append("</body>");
		sf.append("<script type=\"text/javascript\">");
		sf.append("document.all.ccbpaybtobForm.submit();");
		sf.append("</script>");
		sf.append("</html>");
		return sf.toString();
	}
	
	/**
	 * 
	 * @param sParaTemp
	 * @param postUrl
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public static String sendPostInfo(Map<String, String> sParaTemp,String postUrl) throws UnsupportedEncodingException  {
		
		NameValuePair[] nameValuePair = new NameValuePair[sParaTemp.size()];
		int i = 0;
		for (Map.Entry<String, String> entry : sParaTemp.entrySet()) {
			nameValuePair[i++] = new NameValuePair(entry.getKey(), entry.getValue());
		}
		HttpProtocolHandler httpProtocolHandler = HttpProtocolHandler.getInstance();
		HttpRequest request = new HttpRequest(HttpResultType.BYTES);
		
		request.setUrl(postUrl);
		request.setCharset("utf-8");
		request.setParameters(nameValuePair);
		
		HttpResponse response = httpProtocolHandler.execute(request);
		if (response == null) {
			return null;
		}

		String strResult = response.getStringResult();

		return strResult;
	} 

}
