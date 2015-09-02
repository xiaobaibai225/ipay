package com.pay.framework.payment;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * mispos支付通用接口
 * @author PCCW
 *
 */
public interface IMisPosPay {
	
	/**
	 * 获取mispos支付参数
	 * @param request
	 * @param response
	 * @param payTypeId
	 * @param id
	 * @return 发送到第三方支付所需要的参数
	 */
	public String getMisposPayParam(int payTypeId,String id,HttpServletRequest request, HttpServletResponse response);
	
	/**
	 * 获取mispos退款参数
	 * @param request
	 * @param response
	 * @param refundId
	 * @param payTypeId
	 * @return 发送到第三方支付所需要的参数
	 */
	public String getMisposRefundParam(int payTypeId,String refundId,HttpServletRequest request, HttpServletResponse response);
	
	/**
	 * 获取mispos打印参数
	 * @param request
	 * @param response
	 * @param payTypeId
	 * @return 发送到第三方支付所需要的参数
	 */
	public String getMisposReprintParam(int payTypeId,HttpServletRequest request, HttpServletResponse response); 	
	
	/**
	 * 获取mispos已支付信息
	 * @param request
	 * @param response
	 * @param payTypeId
	 */
	public String getMisposPayJson(int payTypeId, HttpServletRequest request,HttpServletResponse response);
	
	/**
	 * 保存mispos返回的信息
	 * @param request
	 * @param response
	 * @param payTypeId
	 */
	public boolean saveMisposMsg(int payTypeId, HttpServletRequest request,HttpServletResponse response);

	public Map misposCheck(HttpServletRequest request,HttpServletResponse response, int payType) throws Exception;
	
}
