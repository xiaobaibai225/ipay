package com.pay.framework.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pay.framework.dao.OrderFormPayDao;
import com.pay.framework.service.OrderFormService;
import com.pay.model.BuyUserInfo;
import com.pay.model.OrderFormPay;
import com.pay.model.OrderFormUnPay;
import com.pay.model.ReceiptData;
import com.pay.model.Refund;

@Component
public class OrderFormServiceImpl implements OrderFormService {

	@Autowired
	private OrderFormPayDao orderFormPayDao;

	@Override
	public OrderFormUnPay queryOrderFormUnPaymentByOrderNumber(String orderId) {
		return orderFormPayDao.queryOrderFormUnPaymentByOrderNumber(orderId);
	}

	@Override
	public boolean addOrderUnPay(OrderFormUnPay formUnpay) {
		int result = orderFormPayDao.addUnpay(formUnpay);
		if (result < 1) {
			return false;
		}
		return true;
	}

	@Override
	public OrderFormUnPay getOrderFormByOrderNum(String orderNum) {
		return orderFormPayDao.getOrderFormByOrderNum(orderNum);
	}

	@Override
	public OrderFormPay getOrderFormByCorderid(String corderid, int companyid) {
		return orderFormPayDao.getOrderFormByCorderid(corderid, companyid);
	}

	@Override
	public OrderFormUnPay getOrderFormUnpayByCorderid(String corderid, int companyid) {
		return orderFormPayDao.getOrderFormUnpayByCorderid(corderid, companyid);
	}
	
	@Override
	public OrderFormUnPay getOrderFormUnpayByCorderidAndStatus(String corderid, int companyid, int status) {
		return orderFormPayDao.getOrderFormUnpayByCorderidAndStatus(corderid, companyid, status);
	}

	@Override
	public int addRefund(Refund refund) {
		return orderFormPayDao.addRefund(refund);
	}

	@Override
	public int updateRefund(String outrefundno, int status) {
		return orderFormPayDao.updateRefund(outrefundno, status);
	}

	@Override
	public List<Refund> getRefundByCorderid(String corderid, int companyid) {
		return orderFormPayDao.getRefundByCorderid(corderid, companyid);
	}

	@Override
	public List<Refund> getRefundByOuterfundno(String outerfundno, int companyid) {
		return orderFormPayDao.getRefundByOuterfundno(outerfundno, companyid);
	}
	
	@Override
	public List<Refund> getRefundByOuterfundno(String outerfundno) {
		return orderFormPayDao.getRefundByOuterfundno(outerfundno);
	}

	@Override
	public boolean deleteOrderUnPay(String orderNum) {
		return orderFormPayDao.delUnpay(orderNum) == 1;
	}

	@Override
	public int addReceiptData(ReceiptData receiptData) {
		return orderFormPayDao.addReceiptData(receiptData);
	}

	@Override
	public ReceiptData getReceiptData(String receiptDataKey) {
		return orderFormPayDao.getReceiptData(receiptDataKey);
	}

	@Override
	public OrderFormPay getOrderByOrderNum(String orderNum) {
		return orderFormPayDao.getOrderFormByOrderNumPay(orderNum);
	}

	@Override
	public int updatePayStatus(String orderNum, int status) {
		return orderFormPayDao.updatePayStatus(orderNum, status);
	}

	@Override
	public int addUnpayExtFiled(String orderNum, String ext) {
		return orderFormPayDao.addUnpayExtFiled(orderNum, ext);
	}

	@Override
	public int addPayExtFiled(String orderNum, String ext) {
		return orderFormPayDao.addPayExtFiled(orderNum, ext);
	}
	
	public int addPayMemoFiled(String orderNum, String memo) {
		return orderFormPayDao.addPayMemoFiled(orderNum, memo);
	}

	@Override
	public List<OrderFormUnPay> queryPhoneCallsByUid(String userId) {
		return orderFormPayDao.getPhoneCallsByUid(userId);
	}

	@Override
	public OrderFormPay queryRecentOrderPayByUid(String userId) {
		return null;
	}

	@Override
	public OrderFormPay queryRecentBuyPackageByUid(String userId) {
		return null;
	}

	@Override
	public int addUserInfo(BuyUserInfo buyUserInfo) {
		BuyUserInfo info = getUserInfoByPhoneAndEmail(buyUserInfo.getPhone(), buyUserInfo.getEmail());
		if (info == null) {
			return orderFormPayDao.addUserInfo(buyUserInfo);
		}
		return 1;
	}

	@Override
	public List<BuyUserInfo> getUserInfo(String userId) {
		return orderFormPayDao.getUserInfo(userId);
	}

	@Override
	public BuyUserInfo getUserInfoByPhoneAndEmail(String phone, String email) {
		return orderFormPayDao.getUserInfoByPhoneAndEmail(phone, email);
	}

	@Override
	public OrderFormUnPay queryOrderFormUnPaymentByMemo(String orderId) {
		return orderFormPayDao.queryOrderFormUnPaymentByMemo(orderId);
	}

}
