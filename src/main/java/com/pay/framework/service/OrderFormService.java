package com.pay.framework.service;

import java.util.List;

import com.pay.model.BuyUserInfo;
import com.pay.model.OrderFormPay;
import com.pay.model.OrderFormUnPay;
import com.pay.model.ReceiptData;
import com.pay.model.Refund;

/**
 * 订单service
 * @author PCCW
 *
 */
public interface OrderFormService {
	
	/**
	 * 获取未支付订单
	 * @param orderId
	 * @return 
	 */
	public OrderFormUnPay queryOrderFormUnPaymentByOrderNumber(String orderId);
	
	/**
	 * 获取未支付订单 招行
	 * @param orderId
	 * @return 
	 */
	public OrderFormUnPay queryOrderFormUnPaymentByMemo(String orderId);
	
	/**
	 * 添加新订单（未支付的订单）
	 * @param formUnpay
	 * @return 是否添加成功
	 */
	public boolean addOrderUnPay(OrderFormUnPay formUnpay);
	
	/**
	 * 根据订单号获取订单
	 * @param orderid 
	 * @return
	 */
	OrderFormUnPay getOrderFormByOrderNum(String orderNum);
	
	
	/**
	 * 根据订单号获取订单
	 * @param orderid 
	 * @return
	 */
	OrderFormPay getOrderByOrderNum(String orderNum);
	
	/**
	 * 根据订单号获取订单
	 * @param orderid 
	 * @return
	 */
	boolean deleteOrderUnPay(String orderNum);
	
	
	/**
	 * 通过商户id,和公司id获取订单
	 * @param corderid
	 * @return
	 */
	OrderFormPay getOrderFormByCorderid(String corderid,int companyid);
	
	/**
	 * 通过商户id,和公司id获取订单(未支付)
	 * @param corderid
	 * @return
	 */
	public OrderFormUnPay getOrderFormUnpayByCorderid(String corderid, int companyid);
	
	/**
	 * 增加退款
	 * @param refund
	 * @return
	 */
	int addRefund(Refund refund);
	
	/**
	 * 增加iap的串
	 * @param receiptData
	 * @return
	 */
	int addReceiptData(ReceiptData receiptData);
	
	/**
	 * 获取记录的iap的串
	 * @param receiptDataKey
	 * @return
	 */
	ReceiptData getReceiptData(String receiptDataKey);
	
	/**
	 * 修改退款状态
	 * @param outrefundno
	 * @param status
	 * @return
	 */
	int updateRefund(String outrefundno,int status);
	
	/**
	 * 获取退款列表
	 * @param corderid
	 * @param companyid
	 * @return
	 */
	List<Refund> getRefundByCorderid(String corderid,int companyid);
	
	/**
	 * 获取退款列表
	 * @param corderid
	 * @param companyid
	 * @return
	 */
	List<Refund> getRefundByOuterfundno(String outerfundno,int companyid);
	
	/**
	 * 获取退款列表
	 * @param corderid
	 * @param companyid
	 * @return
	 */
	List<Refund> getRefundByOuterfundno(String outerfundno);
	/**
	 * 更新订单状态
	 * @param orderNum
	 * @param status
	 * @return
	 */
	int updatePayStatus(String orderNum, int status) ;
	
	/**
	 * 设置扩展字段
	 * @param orderNum
	 * @param ext
	 * @return
	 */
	public int addUnpayExtFiled(String orderNum, String ext) ;
	
	
	/**
	 * 设置扩展字段
	 * @param orderNum
	 * @param ext
	 * @return
	 */
	public int addPayExtFiled(String orderNum, String ext) ;
	public int addPayMemoFiled(String orderNum, String memo) ;
	
	List<OrderFormUnPay> queryPhoneCallsByUid(String userId) ;
	
	/**
	 * 获取用户最近使用银行卡支付的记录
	 * @param userId
	 * @return
	 */
	OrderFormPay queryRecentOrderPayByUid(String userId) ;
	
	/**
	 * 获取上次成功开通的服务
	 * @param userId
	 * @return
	 */
	OrderFormPay queryRecentBuyPackageByUid(String userId) ;
	
	/**
	 * 增加用户信息
	 * @param orderNum
	 * @param status
	 * @return
	 */
	public int addUserInfo(BuyUserInfo buyUserInfo) ;

	/**
	 * 获取用户信息
	 * @param userId
	 * @return
	 */
	public List<BuyUserInfo> getUserInfo(String userId);
	
	/**
	 * 获取用户信息
	 * @param userId
	 * @return
	 */
	public BuyUserInfo getUserInfoByPhoneAndEmail(String phone,String email);

	OrderFormUnPay getOrderFormUnpayByCorderidAndStatus(String corderid,
			int companyid, int status);	
	
}
