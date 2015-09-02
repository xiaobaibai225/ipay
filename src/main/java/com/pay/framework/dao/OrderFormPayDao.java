package com.pay.framework.dao;

import java.util.List;

import com.pay.model.BuyUserInfo;
import com.pay.model.ComplexOrderBean;
import com.pay.model.OrderFormFail;
import com.pay.model.OrderFormPay;
import com.pay.model.OrderFormUnPay;
import com.pay.model.ReceiptData;
import com.pay.model.Refund;

/**
 * 新创建（未支付）的订单表操作dao
 * 
 * @author PCCW
 * 
 */
public interface OrderFormPayDao {

	/**
	 * 通过orderid获取未支付订单，(unpay表)
	 * 
	 * @param orderId
	 * @return
	 */
	OrderFormUnPay queryOrderFormUnPaymentByOrderNumber(String orderId);

	/**
	 * 添加新账单(unpay表)
	 * 
	 * @param formUnpay
	 * @return
	 */
	int addUnpay(OrderFormUnPay formUnpay);

	/**
	 * 添加对象
	 * 
	 * @param formUnpay
	 * @return
	 */
	int addUserInfo(BuyUserInfo userInfo);

	/**
	 * 获取用户信息(主要是支付宝等第三方信息)
	 * 
	 * @param userId
	 * @return
	 */
	List<BuyUserInfo> getUserInfo(String userId);

	/**
	 * 分页查询支付宝用户信息
	 * 
	 * @param limit
	 * @param offset
	 * @return
	 */
	List<BuyUserInfo> queryRecords(int page, int rows);

	/**
	 * 获取用户信息(主要是支付宝等第三方信息)
	 * 
	 * @param phone
	 * @return
	 */
	BuyUserInfo getUserInfoByPhoneAndEmail(String phone, String email);

	/**
	 * 删除unpay表记录
	 * 
	 * @param orderNum
	 * @return
	 */
	int delUnpay(String orderNum);

	/**
	 * 根据订单号获取订单，(unpay表)
	 * 
	 * @param orderid
	 * @return
	 */
	OrderFormUnPay getOrderFormByOrderNum(String orderNum);

	/**
	 * 查询ComplexOrderBean 网关浏览器通知时显示
	 * 
	 * @param ordernumber
	 * @return
	 */
	ComplexOrderBean queryComplexOrderBeanByOrderNumber(String ordernumber);

	/**
	 * 根据订单号获取订单，(pay表)
	 * 
	 * @param orderid
	 * @return
	 */
	OrderFormPay getOrderFormByOrderNumPay(String orderNum);

	/**
	 * 通过商户id,和公司id获取订单
	 * 
	 * @param corderid
	 * @return
	 */
	OrderFormPay getOrderFormByCorderid(String corderid, int companyid);

	/**
	 * 通过商户id,和公司id获取订单(未支付)
	 * 
	 * @param corderid
	 * @return
	 */
	public OrderFormUnPay getOrderFormUnpayByCorderid(String corderid, int companyid);

	/**
	 * 添加pay表数据，这个时候是支付成功了
	 * 
	 * @param formUnpay
	 * @return
	 */
	int addPay(OrderFormPay formpay);

	/**
	 * 增加iap的串
	 * 
	 * @param receiptData
	 * @return
	 */
	int addReceiptData(ReceiptData receiptData);

	/**
	 * 获取记录的iap的串
	 * 
	 * @param receiptDataKey
	 * @return
	 */
	ReceiptData getReceiptData(String receiptDataKey);

	/**
	 * 修改支付订单的状态(用于在)
	 * 
	 * @param orderNum
	 * @param status
	 * @return
	 */
	int updatePayStatus(String orderNum, int status);

	public List<OrderFormPay> getOrderFormByType(int paytype);

	public List<OrderFormPay> getOrderFormWithIap(int paytype);

	public int updatePayExt(String orderNum, String ext);

	public int updatePayPrice(String orderNum, String price);


	/**
	 * 添加到ORDERFORM_FAIL表中，在同时消费中心失败时调用
	 * 
	 * @param orderNum
	 * @param reason
	 * @param status
	 * @return
	 */
	int addFail(String orderNum, String reason, int status);

	List<OrderFormFail> getFail();

	int updateFail(String orderNum, int fail_counts);

	int delFail(String orderNum);

	/**
	 * 增加退款
	 * 
	 * @param refund
	 * @return
	 */
	int addRefund(Refund refund);

	/**
	 * 修改退款状态
	 * 
	 * @param outrefundno
	 * @param status
	 * @return
	 */
	int updateRefund(String outrefundno, int status);

	/**
	 * 更新订单状态
	 * 
	 * @param ordernum
	 * @param status
	 * @return
	 */
	int updateOrderStatus(String ordernum, int status);

	/**
	 * 更新订单状态
	 * 
	 * @param ordernum
	 * @param status
	 * @return
	 */
	int updateOrderUnpayStatus(String ordernum, int status);

	/**
	 * 获取退款列表
	 * 
	 * @param corderid
	 * @param companyid
	 * @return
	 */
	List<Refund> getRefundByCorderid(String corderid, int companyid);
	
	public List<Refund> getRefundByOrderNumber(String orderNumber);

	/**
	 * 获取退款列表
	 * 
	 * @param corderid
	 * @param companyid
	 * @return
	 */
	List<Refund> getRefundByOuterfundno(String outerfundno, int companyid);

	/**
	 * 获取退款列表
	 * 
	 * @param corderid
	 * @param companyid
	 * @return
	 */
	List<Refund> getRefundByOuterfundno(String outerfundno);
	/**
	 * 添加扩展字段(unpay表)
	 * 
	 * @param orderNum
	 * @param ext
	 * @return
	 */
	int addUnpayExtFiled(String orderNum, String ext);

	/**
	 * 添加扩展字段(pay表)
	 * 
	 * @param orderNum
	 * @param ext
	 * @return
	 */
	public int addPayExtFiled(String orderNum, String ext);

	public int addPayMemoFiled(String orderNum, String memo);
	/**
	 * 查询用户话费支付列表
	 * 
	 * @param userId
	 * @param ext
	 * @return
	 */
	List<OrderFormUnPay> getPhoneCallsByUid(String userId);

	/**
	 * 查询用户使用某支付通道记录列表
	 * 
	 * @param userId
	 * @param payType
	 * @return
	 */
	List<OrderFormPay> queryPayListByUid(String userId, int payType);

	/**
	 * 获取用户购买某个套餐列表
	 * 
	 * @param uid
	 * @param pid
	 * @param deptid
	 * @return
	 */
	List<OrderFormPay> queryPayListByUidAndPid(String uid, String pid, String deptid);

	/**
	 * 获取用户使用各种支付类型的订单
	 * 
	 * @param uid
	 * @return
	 */
	List<OrderFormPay> queryPayList(String uid, int paytype);

	/**
	 * 更新消费订单号
	 * 
	 * @param formPay
	 * @return
	 */
	int updateByOrderNumber(OrderFormPay formPay);

	int updataUserInfoForWx(String openid, String uid);

	OrderFormUnPay getOrderFormUnpayByCorderidAndStatus(String corderid,
			int companyid, int status);
	
	/**
	 * 招行专用
	 * 查询ComplexOrderBean 网关浏览器通知时显示
	 * 
	 * @param ordernumber 此处为备用字段memo
	 * @return
	 */
	ComplexOrderBean queryComplexOrderBeanByOrderNumberMemo(String ordernumber);
	OrderFormPay getOrderFormByMemoPay(String orderNum);
	OrderFormUnPay getOrderFormByMemo(String orderNum);
	OrderFormUnPay queryOrderFormUnPaymentByMemo(String orderId);

}
