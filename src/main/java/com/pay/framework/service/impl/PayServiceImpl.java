package com.pay.framework.service.impl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.pay.framework.dao.OrderFormPayDao;
import com.pay.framework.http.HttpRequester;
import com.pay.framework.http.HttpResponse;
import com.pay.framework.http.UrlUtil;
import com.pay.framework.log.LogFlags;
import com.pay.framework.log.LogManager;
import com.pay.framework.mq.MessageQueue;
import com.pay.framework.payment.BasePay;
import com.pay.framework.payment.alipay.Md5Encrypt;
import com.pay.framework.service.PayService;
import com.pay.framework.util.DateUtil;
import com.pay.framework.util.MD5Util;
import com.pay.model.ComplexOrderBean;
import com.pay.model.OrderFormPay;
import com.pay.model.OrderFormUnPay;
import com.pay.model.PayStatus;
import com.pay.model.Refund;

@Service
public class PayServiceImpl implements PayService {

	private static LogManager logger = LogManager.getLogger(PayServiceImpl.class);

	private static final String LOG_KEY = "Framework-PayServiceImpl";

	@Autowired
	private OrderFormPayDao orderFormPayDao;

	@Autowired
	private MessageQueue messageQueue;

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public OrderFormPay doPaySuccess(OrderFormUnPay orderFormUnpay, String transeq) {
		OrderFormPay formpay = new OrderFormPay();
		try {
			BeanUtils.copyProperties(formpay, orderFormUnpay);
		} catch (IllegalAccessException e) {
			logger.debug(
					LOG_KEY,
					String.format("doPaySuccess fail,corderid=%s,ordernumber=%s,svip=%s,error:{%s}", orderFormUnpay.getCorderid(),
							orderFormUnpay.getOrdernumber(), e.getMessage()));
		} catch (InvocationTargetException e) {
			logger.debug(
					LOG_KEY,
					String.format("doPaySuccess fail,corderid=%s,ordernumber=%s,svip=%s,error:{%s}", orderFormUnpay.getCorderid(),
							orderFormUnpay.getOrdernumber(), e.getMessage()));
		}
		formpay.setStatus(String.valueOf(PayStatus.PAY_SUCCESS_NOT_XIAOFEI));
		formpay.setPaymentdate(DateUtil.format(new Date(), DateUtil.C_TIME_PATTON_DEFAULT));
		if (!StringUtils.isEmpty(transeq)) {
			formpay.setTranseq(transeq);
		}

		// 增加到pay记录
		orderFormPayDao.addPay(formpay);
		// 删除unpay
		orderFormPayDao.delUnpay(orderFormUnpay.getOrdernumber());

		logger.debug(LOG_KEY, "doPaySuccess success,corderid=" + orderFormUnpay.getCorderid() + ",ordernumber=" + orderFormUnpay.getOrdernumber());
		return formpay;
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void doRefundSuccess(Refund refund)
	{
		orderFormPayDao.updateOrderStatus(refund.getOrdernumber(), PayStatus.REFUND_SUCCESS);
		logger.debug(LogFlags.TIME_PRINT, "更新退款单状态成功===" );
		orderFormPayDao.updatePayStatus(refund.getOrdernumber(), PayStatus.PAY_SUCCESS_REFUND);
		logger.debug(LogFlags.TIME_PRINT, "更新订单状态成功===" );
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void doServerNotify(OrderFormPay order) {
		String url = order.getBackurl();
		// TODO 支付成功通知商户处理后续业务
		Map<String, String> params = new HashMap<String, String>();
		params.put("corderid", order.getCorderid());//批次id
		params.put("ordernumber", order.getOrdernumber());//返回传给第三方支付机构的订单流水号
		params.put("stat", String.valueOf(order.getStatus()));
		params.put("money", String.valueOf(order.getPrice()));//金额，分为单位
		params.put("companyid", String.valueOf(order.getCompanyid()));//收款公司id
		params.put("paytime", order.getPaymentdate());//收款日期
		params.put("userid", order.getUserid() + "");//会员id
		params.put("channel", "" + order.getPaytype());//支付渠道
		params.put("ordernumber", order.getOrdernumber());
		params.put("producttype", order.getProducttype() + "");
		params.put("productid", order.getProductid());
		params.put("terminal", order.getDeptid());
		params.put("transeq", order.getTranseq());//交易流水号
		params.put("paybank", order.getDefaultbank());//付款银行
		params.put("currency", "CNY");//交易币种
		int paypay=order.getPaytype();
		if(paypay==3 || paypay==4 || paypay==11 || paypay==12)
		params.put("paysource", "APP");//支付来源
		else
		params.put("paysource", "WEB");//支付来源	
		String key = MD5Util.MD5Encode(BasePay.OPEN_SERVICE_KEY, "UTF-8");
		// TODO 签名规则可与商户协商定义好
		String sign = MD5Util.MD5Encode(key + order.getCorderid(), "UTF-8");
		params.put("sign", sign);
		url = UrlUtil.getFullUrl(url, params);
		// 调用消费接口
		HttpRequester httpRequester = new HttpRequester();
		HttpResponse httpResponse = null;
		try {
			httpResponse = httpRequester.sendGet(url.replaceAll("\\s", ""));
		} catch (IOException e) {
			logger.debug(LOG_KEY, "pdoServerNotify error url:" + url + ",corderid=" + order.getCorderid() + ",orderid=" + order.getOrdernumber()
					+ ", errormsg:" + e.getMessage());
			e.printStackTrace();
			return;
		}
		String content = httpResponse.getContent();
		logger.debug("调用完业务处理后返回的结果内容为######", content);
		if (content != null && content.contains("success") || content.contains("true")) {
			logger.debug(LOG_KEY, "pdoServerNotify success url:" + url + ",corderid=" + order.getCorderid() + ",ordernumber=" + order.getOrdernumber());
			orderFormPayDao.updatePayStatus(order.getOrdernumber(), 1);
		} else {
			logger.debug(LOG_KEY, "pdoServerNotify fail url:" + url + ",corderid=" + order.getCorderid() + ",ordernumber=" + order.getOrdernumber());
			orderFormPayDao.updatePayStatus(order.getOrdernumber(), 2);
			orderFormPayDao.addFail(order.getOrdernumber(), "通知失败", 1);

		}

	}

	@Override
	public void doPageNotify(OrderFormPay order, HttpServletResponse response) {
		try {
			// TODO 可先跳转至支付平台的支付成功页，然后跳转至商户页面
			response.sendRedirect(order.getFronturl()+"&delayFlag=N");
		} catch (IOException e) {
			logger.error(LOG_KEY,
					"pdoPageNotify failed url:" + order.getFronturl() + ",corderid=" + order.getCorderid() + ",orderid=" + order.getOrdernumber()
							+ "exception : " + e.getMessage());
		}
	}

	@Override
	public ComplexOrderBean queryComplexOrderBeanByOrderNumber(String ordernumber) {
		return orderFormPayDao.queryComplexOrderBeanByOrderNumber(ordernumber);
	}

	@Override
	public boolean updateOrderStatus(String ordernum, int status) {
		boolean result = false;
		if (orderFormPayDao.updateOrderStatus(ordernum, status) >= 1) {
			result = true;
		}
		return result;
	}

	@Override
	public boolean updateOrderUnpayStatus(String ordernum, int status) {
		boolean result = false;
		if (orderFormPayDao.updateOrderUnpayStatus(ordernum, status) >= 1) {
			result = true;
		}
		return result;
	}

	@Override
	public OrderFormPay getOrderFormByOrderNumPay(String orderNum) {
		return orderFormPayDao.getOrderFormByOrderNumPay(orderNum);
	}

	@Override
	/**
	 * 签名验证
	 */
	public boolean validateSign(OrderFormUnPay orderFormUnPay, String sign) {

		String MD5Value = getSignStr(orderFormUnPay);
		String MD5ValueSpecial = getSpecialSign(orderFormUnPay);
		if (sign != null && (sign.equalsIgnoreCase(MD5Value) || sign.equalsIgnoreCase(MD5ValueSpecial))) {
			return true;
		}
		return false;
	}

	/**
	 * 获取正常签名串
	 */
	@Override
	public String getSignStr(OrderFormUnPay orderFormUnPay) {
		String signedSecKey = MD5Util.MD5Encode(orderFormUnPay.getCompanyid() + BasePay.SECKEY_SUFFIX, "UTF-8");
		// 需要签名的串
		String validateStr = "corderid=" + orderFormUnPay.getCorderid() + "&userid=" + orderFormUnPay.getUserid() + "&price="
				+ orderFormUnPay.getMoney() + "&companyid=" + orderFormUnPay.getCompanyid() + "&" + signedSecKey + "&deptid="
				+ orderFormUnPay.getDeptid();
		String MD5Value = Md5Encrypt.md5(validateStr);
		return MD5Value;
	}

	/**
	 * 对外提供的接口有一些没有传userId
	 * 
	 * @param orderFormUnPay
	 * @return
	 */
	public String getSpecialSign(OrderFormUnPay orderFormUnPay) {
		String signedSecKey = MD5Util.MD5Encode(orderFormUnPay.getCompanyid() + BasePay.SECKEY_SUFFIX, "UTF-8");
		String validateStr2 = "corderid=" + orderFormUnPay.getCorderid() + "&userid=" + null + "&companyid=" + orderFormUnPay.getCompanyid() + "&"
				+ signedSecKey + "&deptid=" + orderFormUnPay.getDeptid();
		return Md5Encrypt.md5(validateStr2);
	}

	@Override
	public List<OrderFormPay> getPayListByUid(String userId, int payType) {
		return orderFormPayDao.queryPayListByUid(userId, payType);
	}

	@Override
	public ComplexOrderBean queryComplexOrderBeanByOrderNumberMemo(
			String ordernumber) {
		return orderFormPayDao.queryComplexOrderBeanByOrderNumberMemo(ordernumber);
	}


}
