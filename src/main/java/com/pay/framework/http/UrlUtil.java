package com.pay.framework.http;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * 关于拼接url 等一些url相关的操作工具类
 * @author houzhaowei
 *
 */
public class UrlUtil {

	/**
	 * 获取整个url
	 * @param baseUrl 基本的url（不带参数）
	 * @param params url参数
	 * @return
	 */
	public static String getFullUrl(String baseUrl , Map<String,String> params){
		StringBuffer fullUrl = new StringBuffer(baseUrl);
		if(baseUrl.indexOf("sign=") != -1){
			return baseUrl;
		}
		if(params == null){
			return baseUrl;
		}
		Set<String> keys = params.keySet();
		if(keys.size() == 0){
			return baseUrl;
		}
		Iterator<String> it = keys.iterator();
		//int index = 0;
		while (it.hasNext()){
			String key = it.next();
			//if(fullUrl == 0){
			//	fullUrl.append("?").append(key).append("=").append(params.get(key));
			//}else {
				fullUrl.append("&").append(key).append("=").append(params.get(key));
			//}
			//index ++ ;
		}
		
		return fullUrl.toString();
	}
	
}
