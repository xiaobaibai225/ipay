package com.pay.framework.service;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.pay.model.ComplexOrderBean;
import com.pay.model.OrderFormPay;
import com.pay.model.OrderFormUnPay;
import com.pay.model.Refund;

/**
 * 处理充值成功之后数据处理
 * @author PCCW
 *
 */
public interface PayService {
	/**
	 * 处理支付成功并开始充值
	 * @param ordernumber
	 */
	public  OrderFormPay doPaySuccess(OrderFormUnPay orderFormUnpay,String transeq);
	
	public void doRefundSuccess(Refund refund);
	
	/**
	 * 通知服务端
	 * @param OrderFormPay order
	 */
	public  void doServerNotify(OrderFormPay order);
	
	/**  cmbDoPageNotify
	 * 通知页面端
	 * @param OrderFormPay order
	 */
	public  void doPageNotify(OrderFormPay order,HttpServletResponse response);
	
	/**
	 * 查询ComplexOrderBean 网关浏览器通知时显示
	 * @param ordernumber
	 * @return
	 */
	ComplexOrderBean queryComplexOrderBeanByOrderNumber(String ordernumber);
	
	
	/**
	 * 更新订单状态
	 * @param ordernum
	 * @param status
	 * @return
	 */
	boolean updateOrderStatus(String ordernum , int status);
	
	/**
	 * 更新订单状态
	 * @param ordernum
	 * @param status
	 * @return
	 */
	public boolean updateOrderUnpayStatus(String ordernum, int status);
	
	/**
	 * 通过
	 * @param orderNum
	 * @return
	 */
	public OrderFormPay getOrderFormByOrderNumPay(String orderNum);
	
	/**
	 * 签名验证
	 */
	public boolean validateSign(OrderFormUnPay orderFormUnPay,String sign);
	
	/**
	 * 获取加密字符串
	 * @param orderFormUnPay
	 * @return
	 */
	public String getSignStr(OrderFormUnPay orderFormUnPay);

	/**
	 * 查询用户使用某支付通道记录列表
	 * @param userId
	 * @param payType
	 * @return
	 */
	List<OrderFormPay> getPayListByUid(String userId,int payType) ;
	/**
	 * 招行专用
	 * 查询ComplexOrderBean 网关浏览器通知时显示
	 * @param ordernumber 此处备用字段 memo
	 * @return
	 */
	ComplexOrderBean queryComplexOrderBeanByOrderNumberMemo(String ordernumber);
	
}
