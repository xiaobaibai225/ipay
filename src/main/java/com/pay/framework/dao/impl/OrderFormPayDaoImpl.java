package com.pay.framework.dao.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pay.framework.dao.OrderFormPayDao;
import com.pay.framework.db.DBManager;
import com.pay.model.BuyUserInfo;
import com.pay.model.ComplexOrderBean;
import com.pay.model.OrderFormFail;
import com.pay.model.OrderFormPay;
import com.pay.model.OrderFormUnPay;
import com.pay.model.ReceiptData;
import com.pay.model.Refund;

@Component
public class OrderFormPayDaoImpl implements OrderFormPayDao {

	@Autowired
	private DBManager db;

	@Override 
	public OrderFormUnPay queryOrderFormUnPaymentByOrderNumber(String orderId) {
		return db.queryForObject("select * from orderform_unpay where order_number = ?", OrderFormUnPay.class, orderId);
	}

	@Override
	public int addUnpay(OrderFormUnPay formUnpay) {
		int result = db.insertObject(formUnpay);
		return result;
	}

	@Override
	public int delUnpay(String orderNum) {
		String sql = "delete from orderform_unpay where order_number=? ";
		Object[] args = new Object[] { orderNum };
		int result = db.update(sql, args);
		return result;
	}

	@Override
	public int addUserInfo(BuyUserInfo userInfo) {
		int result = db.insertObject(userInfo);
		return result;
	}

	@Override
	public List<BuyUserInfo> getUserInfo(String userId) {
		return db.queryForListObject("select * from buyer_user_info where userid = ?", BuyUserInfo.class, userId);
	}

	@Override
	public int updataUserInfoForWx(String openid, String uid) {
		return db.update("update buyer_user_info set account=? where userid=? ", new Object[] { openid, uid });
	}

	@Override
	public OrderFormUnPay getOrderFormByOrderNum(String orderNum) {
		String sql = "select * from orderform_unpay where order_number=?";
		Object[] args = new Object[] { orderNum };
		return db.queryForObject(sql, OrderFormUnPay.class, args);
	}

	@Override
	public ComplexOrderBean queryComplexOrderBeanByOrderNumber(String ordernumber) {
		OrderFormUnPay orderFormUnPay = getOrderFormByOrderNum(ordernumber);
		if (orderFormUnPay == null) {
			OrderFormPay orderFormPay = getOrderFormByOrderNumPay(ordernumber);
			if (orderFormPay != null) {
				return new ComplexOrderBean(ComplexOrderBean.SUCCESSED, orderFormPay, null);
			} else {
				return new ComplexOrderBean(ComplexOrderBean.ERROR_ORDER_ISNULL, null, null);
			}
		} else {
			return new ComplexOrderBean(ComplexOrderBean.ERROR_ORDER_ISNULL, null, orderFormUnPay);
		}
	}

	@Override
	public OrderFormPay getOrderFormByOrderNumPay(String orderNum) {
		if(orderNum.contains("refund"))
		{
			Refund refund=getOrderNumberByOuterfundno(orderNum);
			if(null!=refund)
			orderNum=refund.getOrdernumber();
		}
		String sql = "select * from orderform where ordernumber=?";
		Object[] args = new Object[] { orderNum };
		return db.queryForObject(sql, OrderFormPay.class, args);
	}

	@Override
	public OrderFormPay getOrderFormByCorderid(String corderid, int companyid) {
		String sql = "select * from orderform where corderid=? and companyid=? order by paymentdate desc";
		Object[] args = new Object[] { corderid, companyid };
		List<OrderFormPay> retList = db.queryForListObject(sql, args, OrderFormPay.class);
		if (retList != null && retList.size() > 0) {
			return retList.get(0);
		} else {
			return null;
		}
	}

	@Override
	public OrderFormUnPay getOrderFormUnpayByCorderid(String corderid, int companyid) {
		String sql = "select * from orderform_unpay where corder_id=? and company_id=? and charge_type=0 order by submit_date desc";
		Object[] args = new Object[] { corderid, companyid };
		List<OrderFormUnPay> retList = db.queryForListObject(sql, args, OrderFormUnPay.class);
		if (retList != null && retList.size() > 0) {
			return retList.get(0);
		} else {
			return null;
		}
	}

	@Override
	public OrderFormUnPay getOrderFormUnpayByCorderidAndStatus(String corderid, int companyid, int status) {
		String sql = "select * from orderform_unpay where corder_id=? and company_id=? and charge_type=0 and status=? order by submit_date desc";
		Object[] args = new Object[] { corderid, companyid, status };
		List<OrderFormUnPay> retList = db.queryForListObject(sql, args, OrderFormUnPay.class);
		if (retList != null && retList.size() > 0) {
			return retList.get(0);
		} else {
			return null;
		}
	}

	@Override
	public int addPay(OrderFormPay formpay) {
		// 防止第三方重复通知时出现插入异常
		OrderFormPay pay = getOrderFormByOrderNumPay(formpay != null ? formpay.getOrdernumber() : "");
		if (pay == null) {
			return db.insertObject(formpay);
		}
		return 0;
	}

	@Override
	public int updatePayStatus(String orderNum, int status) {
		String sql = "update orderform set status =? where ordernumber=? ";
		Object[] args = new Object[] { status, orderNum };
		int result = db.update(sql, args);
		return result;
	}

	@Override
	public int updatePayExt(String orderNum, String ext) {
		String sql = "update orderform set ext =? where ordernumber=? ";
		Object[] args = new Object[] { ext, orderNum };
		int result = db.update(sql, args);
		return result;
	}

	@Override
	public int updatePayPrice(String orderNum, String ext) {
		String sql = "update orderform set price =? where ordernumber=? ";
		Object[] args = new Object[] { ext, orderNum };
		int result = db.update(sql, args);
		return result;
	}

	@Override
	public int updateOrderUnpayStatus(String ordernum, int status) {
		String sql = "update orderform_unpay set status =? where order_number=? ";
		Object[] args = new Object[] { status, ordernum };
		int result = db.update(sql, args);
		return result;
	}

	@Override
	public int addFail(String orderNum, String reason, int status) {
		String sql = "insert into orderform_fail (ordernumber,reason,status) " + "values (?,?,?)";
		Object[] args = new Object[] { orderNum, reason, status };
		int result = db.update(sql, args);
		return result;
	}

	@Override
	public List<OrderFormFail> getFail() {
		String sql = "select * from orderform_fail";
		return db.queryForListObject(sql, OrderFormFail.class);
	}

	public List<OrderFormPay> getOrderFormByType(int paytype) {
		String sql = "select * from orderform where paytype=?";
		return db.queryForListObject(sql, OrderFormPay.class, paytype);
	}

	public List<OrderFormPay> getOrderFormWithIap(int paytype) {
		String sql = "select * from orderform where paytype=? and ext is not null";
		return db.queryForListObject(sql, OrderFormPay.class, paytype);
	}

	@Override
	public int delFail(String orderNum) {
		String sql = "delete from orderform_fail where ordernumber=? ";
		Object[] args = new Object[] { orderNum };
		int result = db.update(sql, args);
		return result;
	}

	@Override
	public int updateFail(String orderNum, int fail_counts) {
		String sql = "update orderform_fail set fail_counts =? where ordernumber=? ";
		Object[] args = new Object[] { fail_counts, orderNum };
		int result = db.update(sql, args);
		return result;
	}

	@Override
	public int addRefund(Refund refund) {
		int result = db.insertObject(refund);
		return result;
	}

	@Override
	public int addReceiptData(ReceiptData receiptData) {
		int result = db.insertObject(receiptData);
		return result;
	}

	@Override
	public ReceiptData getReceiptData(String receiptDataKey) {
		String sql = "select * from IAP_RECEIPT_DATA where RECEIPT_DATA=?";
		Object[] args = new Object[] { receiptDataKey };
		return db.queryForObject(sql, ReceiptData.class, args);
	}

	@Override
	public int updateRefund(String outrefundno, int status) {
		String sql = "update pay_refund set status =? where outer_fund_no=? ";
		Object[] args = new Object[] { status, outrefundno };
		int result = db.update(sql, args);
		return result;
	}

	@Override
	public int updateOrderStatus(String ordernum, int status) {
		String sql = "update pay_refund set status = ? where order_number = ?";
		Object[] args = new Object[] { status, ordernum };
		return db.update(sql, args);
	}

	@Override
	public List<Refund> getRefundByCorderid(String corderid, int companyid) {
		String sql = "select * from pay_refund where corder_id=? and company_id=? and status=1";
		Object[] args = new Object[] { corderid, companyid };
		List<Refund> retList = db.queryForListObject(sql, args, Refund.class);
		return retList;
	}

	@Override
	public List<Refund> getRefundByOuterfundno(String outerfundno, int companyid) {
		String sql = "select * from pay_refund where outer_fund_no=? and company_id=? and status=1";
		Object[] args = new Object[] { outerfundno, companyid };
		List<Refund> retList = db.queryForListObject(sql, args, Refund.class);
		return retList;
	}
	@Override
	public List<Refund> getRefundByOuterfundno(String outerfundno) {
		String sql = "select * from pay_refund where outer_fund_no=?  and status=0";
		Object[] args = new Object[] { outerfundno };
		List<Refund> retList = db.queryForListObject(sql, args, Refund.class);
		return retList;
	}
	@Override
	public List<Refund> getRefundByOrderNumber(String orderNumber) {
		String sql = "select * from pay_refund where order_number=?  and status=0";
		Object[] args = new Object[] { orderNumber };
		List<Refund> retList = db.queryForListObject(sql, args, Refund.class);
		return retList;
	}
	public Refund getOrderNumberByOuterfundno(String outerfundno) {
		String sql = "select * from pay_refund where outer_fund_no=?  and status=1";
		Object[] args = new Object[] { outerfundno };
		List refundList = db.queryForListObject(sql, args, Refund.class);
		if(null !=refundList && refundList.size()>0) 
			return (Refund)refundList.get(0);
		else return null;
	}

	@Override
	public int addUnpayExtFiled(String orderNum, String ext) {
		String sql = "update orderform_unpay set ext = ? where order_number = ?";
		Object[] args = new Object[] { ext, orderNum };
		return db.update(sql, args);
	}

	@Override
	public int addPayExtFiled(String orderNum, String ext) {
		String sql = "update orderform set ext = ? where ordernumber = ?";
		Object[] args = new Object[] { ext, orderNum };
		return db.update(sql, args);
	}
	
	@Override
	public int addPayMemoFiled(String orderNum, String memo) {
		String sql = "update orderform set memo = ? where ordernumber = ?";
		Object[] args = new Object[] { memo, orderNum };
		return db.update(sql, args);
	}

	@Override
	public List<OrderFormUnPay> getPhoneCallsByUid(String userId) {
		String sql = "select * from  orderform_unpay where user_id = ?";
		Object[] args = new Object[] { userId };
		return db.queryForListObject(sql, args, OrderFormUnPay.class);
	}

	@Override
	public List<OrderFormPay> queryPayListByUid(String userId, int payType) {
		String sql = "select * from  orderform where userid = ? and paytype = ? order by paymentdate desc";
		Object[] args = new Object[] { userId, payType };
		return db.queryForListObject(sql, args, OrderFormPay.class);
	}

	@Override
	public List<OrderFormPay> queryPayListByUidAndPid(String uid, String pid, String deptid) {
		String sql = "select * from  orderform where status > 0 and userid = ? and productid = ? and deptid = ? order by paymentdate desc";
		Object[] args = new Object[] { uid, pid, deptid };
		return db.queryForListObject(sql, args, OrderFormPay.class);
	}

	@Override
	public List<OrderFormPay> queryPayList(String uid, int paytype) {
		String sql = "select * from orderform where userid = ? and productid = 2  and paytype = ?  order by paymentdate desc";
		Object[] args = new Object[] { uid, paytype };
		return db.queryForListObject(sql, args, OrderFormPay.class);
	}

	@Override
	public int updateByOrderNumber(OrderFormPay formPay) {
		String sql = "update orderform set corderid = ? where ordernumber = ?";
		Object[] args = new Object[] { formPay.getCorderid(), formPay.getOrdernumber() };
		return db.update(sql, args);
	}

	@Override
	public List<BuyUserInfo> queryRecords(int page, int rows) {
		String sql = "select * from buyer_user_info limit :offset,:limit";
		return db.queryForListObject(sql, BuyUserInfo.class, new Object[] { page * rows, rows });
	}

	@Override
	public BuyUserInfo getUserInfoByPhoneAndEmail(String phone, String email) {
		String sql = "select * from buyer_user_info where 1 = 1 ";	
		List<String> conditions = new ArrayList<String>();
		if (StringUtils.isNotBlank(phone)) {
			sql += " and phone = ?";
			conditions.add(phone);
		}
		if (StringUtils.isNotBlank(email)) {
			sql += " and email = ?";
			conditions.add(email);
		}
		return db.queryForObject(sql, BuyUserInfo.class, conditions.toArray());
	}

	@Override
	public ComplexOrderBean queryComplexOrderBeanByOrderNumberMemo(
			String ordernumber) {
			OrderFormUnPay orderFormUnPay = getOrderFormByMemo(ordernumber);
			if (orderFormUnPay == null) {
				OrderFormPay orderFormPay = getOrderFormByMemoPay(ordernumber);
				if (orderFormPay != null) {
					return new ComplexOrderBean(ComplexOrderBean.SUCCESSED, orderFormPay, null);
				} else {
					return new ComplexOrderBean(ComplexOrderBean.ERROR_ORDER_ISNULL, null, null);
				}
			} else {
				return new ComplexOrderBean(ComplexOrderBean.ERROR_ORDER_ISNULL, null, orderFormUnPay);
			}
	}
	@Override
	
	public OrderFormUnPay getOrderFormByMemo(String orderNum) {
		String sql = "select * from orderform_unpay where memo=?";
		Object[] args = new Object[] { orderNum };
		return db.queryForObject(sql, OrderFormUnPay.class, args);
	}
	@Override
	public OrderFormPay getOrderFormByMemoPay(String orderNum) {
		String sql = "select * from orderform where memo=?";
		Object[] args = new Object[] { orderNum };
		return db.queryForObject(sql, OrderFormPay.class, args);
	}
	@Override 
	public OrderFormUnPay queryOrderFormUnPaymentByMemo(String orderId) {
		return db.queryForObject("select * from orderform_unpay where memo = ?", OrderFormUnPay.class, orderId);
	}
}
