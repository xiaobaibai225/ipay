package com.pay.framework.payment;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 移动支付通用接口
 * @author PCCW
 *
 */
public interface ICodePay {
	
	/**
	 * 获取二维码地址
	 * @param request
	 * @param response
	 * @param payTypeId
	 * @return 发送到第三方支付所需要的参数
	 */
	public String getCodeForwardParameter(HttpServletRequest request, HttpServletResponse response,int payTypeId);
	
	/**
	 * mobile 的服务端通知
	 * @param request
	 * @param response
	 * @return 通知的状态
	 */
	public int codeServerNotify(HttpServletRequest request, HttpServletResponse response);

}
