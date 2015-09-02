package com.pay.framework.payment;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

import com.pay.model.Check;
import com.pay.model.OrderFormPay;
import com.pay.model.OrderFormUnPay;


/**
 * 第三方支付一个跳转页面以及两个通知接口
 * @author PCCW
 *
 */
@Component
public interface IPay {
	/**
	 * 获得跳转到网关的url
	 * @param orderform
	 * @param payment
	 * @param extMap 扩展参数Map
	 * @return 要发送支付请求的url，某些特殊情况在方法里面发了请求，则返回"success" 或 "post"字符串
	 */
	public String getForwardUrl(HttpServletRequest request, HttpServletResponse response,int payTypeId);
	
	/**
	 * 处理网关服务器通知
	 * @param payment
	 * @param parameterMap 网关参数
	 * @return
	 */
	public int doPaymentServerNotify(HttpServletRequest request, HttpServletResponse response);
	/**
	 *  处理网关浏览器通知
	 * @param payment
	 * @param parameterMap
	 * @return
	 */
	public int doPaymentPageNotify(HttpServletRequest request, HttpServletResponse response);
	
	/**
	 * 退款接口
	 * @param request
	 * @param response
	 * @return
	 */
	public String refund(HttpServletRequest request, HttpServletResponse response) throws Exception;
	
	/**
	 * 退款通知接口
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public int refundNotify(HttpServletRequest request, HttpServletResponse response) throws Exception;
	
	/**
	 * 对账
	 * @param request
	 * @param response
	 * @return 
	 * @throws Exception
	 */
	public List<Check> check(HttpServletRequest request, HttpServletResponse response,int payTypeId) throws Exception;
	
	/**
	 * 保存用户信息
	 * @param request
	 */
	public void saveUserInfo(HttpServletRequest request);
	
	public OrderFormUnPay getOrderFormUnPay(HttpServletRequest request, HttpServletResponse response);
	

}