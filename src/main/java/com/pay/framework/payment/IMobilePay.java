package com.pay.framework.payment;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 移动支付通用接口
 * @author PCCW
 *
 */
public interface IMobilePay {
	
	/**
	 * 获取移动支付对接参数
	 * @param request
	 * @param response
	 * @param payTypeId
	 * @return 发送到第三方支付所需要的参数
	 */
	public String getMobileForwardParameter(HttpServletRequest request, HttpServletResponse response,int payTypeId);
	
	/**
	 * mobile 的服务端通知
	 * @param request
	 * @param response
	 * @return 通知的状态
	 */
	public int mobileServerNotify(int payTypeId,HttpServletRequest request, HttpServletResponse response);

	/**
	 * 获取wap移动支付对接参数
	 * @param request
	 * @param response
	 * @param payTypeId
	 * @return mobile 跳转的url地址
	 */
	public String getWapForwardParameter(HttpServletRequest request, HttpServletResponse response,int payTypeId);
	
	/**
	 * 跳转短信支付
	 * @param request
	 * @param response
	 * @param payTypeId
	 * @return mobile 跳转的url地址
	 */
	public String getPhoneMessageForwardParameter(HttpServletRequest request, HttpServletResponse response,int payTypeId);
	
	/**
	 * mobile wap 的服务端通知
	 * @param request
	 * @param response
	 * @return 通知的状态
	 */
	public int wapServerNotify(HttpServletRequest request, HttpServletResponse response);
	
	/**
	 * 获取移动支付对接参数
	 * @param request
	 * @param response
	 * @param payTypeId
	 * @return 发送到第三方支付所需要的参数
	 * @throws MalformedURLException 
	 * @throws IOException 
	 */
	public String getIapResult(HttpServletRequest request, HttpServletResponse response,int payTypeId) throws MalformedURLException, IOException;
}
