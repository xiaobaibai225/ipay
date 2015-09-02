package com.pay.framework.payment;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface AutoPay {
	/**
	 * 用于系统后台自动发送支付请求
	 * @param request
	 * @param response
	 * @param payTypeId
	 * @return
	 */
	public String autoPayUrl(HttpServletRequest request, HttpServletResponse response,int payTypeId);
}
