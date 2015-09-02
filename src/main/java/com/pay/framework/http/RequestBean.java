package com.pay.framework.http;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.pay.framework.db.BeanOperator;

/**
 * 将request 封装成  T
 * @author houzhaowei
 *
 * @param <T>
 */
public class RequestBean {
	
	private RequestBean(){}

	/**
	 * 获取封装后的实例
	 * @param clz T.class
	 * @param request HttpServletRequest
	 * @return T
	 */
	public static <T> T getObject(Class<T> clz,HttpServletRequest request){

		T bean = null;
		try {
			bean = (T)clz.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		Map<String,String> map = RequestUtil.getParamaterMap(request);
		bean = BeanOperator.map2ObjectStr(map, clz);
		return bean;
	}
	
}
