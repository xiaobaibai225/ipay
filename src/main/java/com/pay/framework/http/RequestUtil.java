package com.pay.framework.http;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.pay.framework.util.StringUtil;

/**
 * request 对象的相关操作
 * 
 * @author PCCW
 * 
 */
public class RequestUtil {

	public static Map<String, String> getParamaterMap(HttpServletRequest request) {

		Map<String, String> params = new HashMap<String, String>();
		@SuppressWarnings("unchecked")
		Map<String, String[]> requestParams = request.getParameterMap();
		for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			String[] values = (String[]) requestParams.get(name);
			String valueStr = "";
			for (int i = 0; i < values.length; i++) {
				valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
			}
			// 乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
			try {
				if (request.getCharacterEncoding().toUpperCase().equals("ISO-8859-1")) {
					valueStr = new String(valueStr.getBytes("ISO-8859-1"), "UTF-8");
				}
				//判空及字符串null
				if (StringUtil.isBlank(valueStr) || "null".equals(valueStr.toLowerCase())) {
					valueStr = "" ;
				}

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			params.put(name, StringUtil.isNotBlank(valueStr) ? valueStr.trim() : valueStr);
		}
		return params;
	}

	/**
	 * 获取客户端ip
	 * 
	 * @param request
	 * @return ip 字符串
	 */
	public static String getClientIp(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}

		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}

		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}

	/**
	 * 获取请求的完整url
	 * 
	 * @param request
	 * @return
	 */
	public static String getUrl(HttpServletRequest request) {
		String url = request.getScheme() + "://"; // 请求协议 http 或 https
		url += request.getHeader("host");// 请求服务器
		url += request.getRequestURI();// 工程名
		if (request.getQueryString() != null) {
			url += "?" + request.getQueryString(); // 参数
		}
		return url;
	}

	/**
	 * 获取请求的referer
	 * 
	 * @param request
	 * @return
	 */
	public static String getRefererUrl(HttpServletRequest request) {
		String referer = request.getHeader("Referer");
		return referer;
	}

}
