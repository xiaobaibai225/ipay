package com.pay.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.pay.model.OrderFormUnPay;


/**
 * 支付的service
 * @author PCCW
 *
 */
public interface PayService {

	/**
	 * 向第三方平台发送支付请求
	 * @param request
	 * @param response
	 * @return 请求是否成功
	 */
	boolean pay(int typeId , HttpServletRequest request,HttpServletResponse response);
	
	/**
	 * 接受服务端通知
	 * @param request
	 * @param response
	 * @return 通知是否成功
	 */
	boolean serverNotify(int typeId,HttpServletRequest request,HttpServletResponse response);
	
	/**
	 * 接受服务端通知
	 * @param request
	 * @param response
	 * @return 通知是否成功
	 */
	boolean serverMobileNotify(int typeId,HttpServletRequest request,HttpServletResponse response);
	/**
	 * 取消订单服务器通知
	 * @param typeId
	 * @param request
	 * @param response
	 * @return
	 */
	boolean cancelorderservernotify(int typeId,HttpServletRequest request,HttpServletResponse response);
	
	
	/**
	 * 接受服务端通知
	 * @param request
	 * @param response
	 * @return 通知是否成功
	 */
	boolean wapServerNotify(int typeId,HttpServletRequest request,HttpServletResponse response);
	
	/**
	 * 接受服务端退款通知通知
	 * @param request
	 * @param response
	 * @return 通知是否成功
	 */
	boolean refundNotify(int typeId,HttpServletRequest request,HttpServletResponse response);
	
	/**
	 * 接收页面跳转通知
	 * @param request
	 * @param response
	 * @return 页面展示的bean
	 */
	boolean pageNotify(int typeId,HttpServletRequest request,HttpServletResponse response);
	
	/**
	 * 退款接口
	 * @param request
	 * @param response
	 * @return
	 */
	String refund(HttpServletRequest request, HttpServletResponse response) throws Exception;
	
	/**
	 * 查询订单状态
	 * @param request
	 * @param response
	 * @return
	 */
	String querystat(HttpServletRequest request, HttpServletResponse response) throws Exception;
	
	/**
	 * 对账
	 * @param request
	 * @param response
	 * @return
	 */
	String check(HttpServletRequest request, HttpServletResponse response) throws Exception;
	
	/**
	 * 获取移动端的支付加密串
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	String getMobileForwardParameter(int pay_type_id, HttpServletRequest request, HttpServletResponse response) throws Exception;
	
	/**
	 * 获取wap端的支付加密串
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	String getWapForwardParameter(int pay_type_id, HttpServletRequest request, HttpServletResponse response) throws Exception;
	
	/**
	 * 获取wap端的支付加密串
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	String getPhoneMessageParameter(int pay_type_id, HttpServletRequest request, HttpServletResponse response) throws Exception;
	
	/**
	 * 获取wap端的支付加密串
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	String pointPay(int pay_type_id, HttpServletRequest request, HttpServletResponse response) throws Exception;
	
	/**
	 * 取消订单
	 * @param pay_type_id
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	String cancelorder(int pay_type_id, HttpServletRequest request, HttpServletResponse response) throws Exception;
	
	/**
	 * 获取wap端的支付加密串
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	String iap(int pay_type_id, HttpServletRequest request, HttpServletResponse response) throws Exception;
	
	public OrderFormUnPay getOrderFormUnPay(int typeId, HttpServletRequest request, HttpServletResponse response);
	
	
	/**
	 * 获取税率
	 * @param pay_type_id
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	String tax(int pay_type_id, HttpServletRequest request, HttpServletResponse response) throws Exception;
	
	/**
	 * 向第三方平台发送支付请求(只用于后台发送 无页面跳转响应)
	 * @param request
	 * @return 请求是否成功
	 */
	String autoPay(int typeId , HttpServletRequest request, HttpServletResponse response);
	
	/**
	 * 获取wap端的支付加密串
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	String getTwoDimensionCode(int pay_type_id, HttpServletRequest request, HttpServletResponse response) throws Exception;
	
	
	/**
	 * 返回iframe需要的url
	 * @param request
	 * @param response
	 * @return
	 */
	String iframePay(int typeId , HttpServletRequest request,HttpServletResponse response);


	/**
	 * 构造mispos付款请求参数
	 * @param request
	 * @param response
	 * @return
	 */
	String getMisposPayParam(int pay_type_id,String id,HttpServletRequest request,
			HttpServletResponse response);

	/**
	 * 构造mispos退款请求参数
	 * @param refundId 
	 * @param request
	 * @param response
	 * @return
	 */
	String getMisposRefundParam(int pay_type_id, String refundId, HttpServletRequest request,
			HttpServletResponse response);
	
	/**
	 * 保存mispos支付返回信息
	 * @param request
	 * @param response
	 * @return
	 */
	boolean saveMisposMsg(int pay_type_id, HttpServletRequest request,
			HttpServletResponse response);

	String getMisposPayJson(int pay_type_id, HttpServletRequest request,
			HttpServletResponse response);

	String getMisposReprintParam(int pay_type_id, HttpServletRequest request,
			HttpServletResponse response);

	String misposCheck(HttpServletRequest request, HttpServletResponse response)
			throws Exception;

	String searchPayStatus(HttpServletRequest request,
			HttpServletResponse response);

	
}
