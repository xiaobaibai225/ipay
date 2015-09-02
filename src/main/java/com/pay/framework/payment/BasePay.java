package com.pay.framework.payment;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.pay.framework.http.RequestBean;
import com.pay.framework.http.RequestUtil;
import com.pay.framework.log.LogFlags;
import com.pay.framework.log.LogManager;
import com.pay.framework.service.OrderFormService;
import com.pay.framework.service.PayService;
import com.pay.framework.service.PayTypeService;
import com.pay.framework.util.CommonUtil;
import com.pay.framework.util.DateUtil;
import com.pay.framework.util.MD5Util;
import com.pay.framework.util.MoneyUtil;
import com.pay.model.OrderFormPay;
import com.pay.model.OrderFormUnPay;
import com.pay.model.PayStatus;
import com.pay.model.Refund;

/**
 * 支付类的基类，所有支付类必须继承此类 实现了支付必须做的工作
 * <p/>
 * 初始化支付基本属性
 * 
 * @author PCCW
 */
public class BasePay {

	private LogManager logger = LogManager.getLogger(BasePay.class);
	private static final String LOG_KEY = "BASE_PAY";
	@Autowired
	protected OrderFormService orderFormService;
	@Autowired
	protected PayService payService;
	@Autowired
	protected PayTypeService payTypeService;
	//支付校验签名秘钥
	public static final String SECKEY_SUFFIX = "@f34paweeewe131212dfdgfjy2014erer";
	//通知商户支付成功校验签名秘钥
	public final static String OPEN_SERVICE_KEY = "jsdfasdf23g355hGVsRruj7rKomSkDPj3FG";


	/**
	 * 支付前调用，
	 * 
	 * @param request
	 * @return
	 */
	protected OrderFormUnPay beforePay(HttpServletRequest request, int payTypeId) {
		OrderFormUnPay form = RequestBean.getObject(OrderFormUnPay.class, request);
		try {
			// TODO 验证用户身份有效性
			form.setPaytype(payTypeId);
			String now = DateUtil.getCurrentDateStr(DateUtil.C_TIME_PATTON_DEFAULT);
			form.setSubmitdate(now);
			form.setIp(RequestUtil.getClientIp(request));
			// 生成唯一支付订单号
			form.setOrdernumber(CommonUtil.getOrderFormNumber());
			// 获取签名数据项
			 String sign = request.getParameter("sign");
			// TODO 测试阶段先去掉签名验证 后续打开签名验证
			 if (!payService.validateSign(form, sign)) {
				 logger.error(LOG_KEY, "签名不通过：\t传过来的sign: " + sign + "\t正常的sign："
			+ payService.getSignStr(form));
			 return null;
			 }
			// TODO 考虑到安全性，需要去业务系统重新获取价格
			// 后台重新获取价格失败或者异常时 返回支付失败
			if (StringUtils.isBlank(form.getMoney())) {
				return null;
			}
			// 商户必须传订单号
			if (StringUtils.isBlank(form.getCorderid()) || form.getCorderid().equals("0")) {
				return null;
			}
			// 支付金额转化为分单位 最小为一分钱
			form.setPrice(Long.parseLong(MoneyUtil.getPayMoney(form.getMoney())));
			if (form.getPrice() < 1) {
				return null;
			}
			orderFormService.addOrderUnPay(form);
		} catch (Exception e) {
			try {
				form.setOrdernumber(CommonUtil.getOrderFormNumber());
				orderFormService.addOrderUnPay(form);
			} catch (Exception e2) {
				e.printStackTrace();
				return null;
			}
		}
		return form;
	}

	/**
	 * 支付前调用，
	 * 
	 * @param request
	 * @return
	 */
	protected Refund beforeRefund(HttpServletRequest request) {
		String corderid = request.getParameter("corderid");
		if (StringUtils.isEmpty(corderid)) {
			corderid = (String) request.getAttribute("corderid");
		}
		String companyid = request.getParameter("companyid");
		if (StringUtils.isEmpty(companyid)) {
			companyid = (String) request.getAttribute("companyid");
		}
		String newcorderid = request.getParameter("newcorderid");
		if (StringUtils.isEmpty(newcorderid)) {
			newcorderid = (String) request.getAttribute("newcorderid");
		}
		// 扩展字段回调通知电商接口地址
		String refundurl = request.getParameter("refundurl");
		if (StringUtils.isEmpty(refundurl)) {
			refundurl = (String) request.getAttribute("refundurl");
		}
		// 首先进行md5签名验证
		String secKey = companyid + SECKEY_SUFFIX;
		String signedSecKey = MD5Util.MD5Encode(secKey, "UTF-8");
		String md5Str = "corderid=" + corderid + "&" + signedSecKey + "&companyid=" + companyid;
		String md5Value = MD5Util.MD5Encode(md5Str, "UTF-8");
		String sign = request.getParameter("sign");
		if (sign == null || !sign.equalsIgnoreCase(md5Value)) {
			logger.error(LOG_KEY, "sign validate error : mine = " + md5Value + " their's" + sign);
			return null;
		}
		OrderFormPay orderFormPay = orderFormService.getOrderFormByCorderid(corderid, Integer.parseInt(companyid));
		if (orderFormPay == null) {
			return null;
		}
		// 保存通知电商接口地址
		if (StringUtils.isNotBlank(refundurl)) {
			orderFormService.addPayExtFiled(orderFormPay.getOrdernumber(), refundurl);
		}
		// 保存新的退款应收id
		if (StringUtils.isNotBlank(newcorderid)) {
			orderFormService.addPayMemoFiled(orderFormPay.getOrdernumber(), newcorderid);
		}
		List<Refund> refundList = orderFormService.getRefundByCorderid(corderid, Integer.parseInt(companyid));
		int totalRefund = 0;
		if (CollectionUtils.isNotEmpty(refundList)) {
			for (Refund refund : refundList) {
				totalRefund += refund.getRefundmoney();
			}
			// 已经退完了
			if (totalRefund >= Integer.parseInt(MoneyUtil.getPayMoney(orderFormPay.getMoney()))) {
				return null;
			}
		}
		Refund refund = new Refund();
		try {
			BeanUtils.copyProperties(refund, orderFormPay);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		refund.setStatus(0);
		refund.setOuterfundno(CommonUtil.getOrderFormNumber() + "refund" + orderFormPay.getCompanyid());
		refund.setTotalmoney(orderFormPay.getPrice());
		String refund_fee = request.getParameter("refund_fee");
		String now = DateUtil.getCurrentDateStr(DateUtil.C_TIME_PATTON_DEFAULT);
		refund.setRefundDate(now);
		if (StringUtils.isEmpty(refund_fee)) {
			refund.setRefundmoney(orderFormPay.getPrice());
		} else {
			refund.setRefundmoney(Long.parseLong(refund_fee));
		}
		orderFormService.addRefund(refund);
		return refund;
	}

	/**
	 * 中行外卡退款前调用，
	 * 
	 * @param request
	 * @return
	 */
	protected Refund bocOutBeforeRefund(HttpServletRequest request) {
		String corderid = request.getParameter("corderid");
		if (StringUtils.isEmpty(corderid)) {
			corderid = (String) request.getAttribute("corderid");
		}
		String companyid = request.getParameter("companyid");
		if (StringUtils.isEmpty(companyid)) {
			companyid = (String) request.getAttribute("companyid");
		}
		String newcorderid = request.getParameter("newcorderid");
		if (StringUtils.isEmpty(newcorderid)) {
			newcorderid = (String) request.getAttribute("newcorderid");
		}
		// 扩展字段回调通知电商接口地址
		String refundurl = request.getParameter("refundurl");
		if (StringUtils.isEmpty(refundurl)) {
			refundurl = (String) request.getAttribute("refundurl");
		}
		// 首先进行md5签名验证
		String secKey = companyid + SECKEY_SUFFIX;
		String signedSecKey = MD5Util.MD5Encode(secKey, "UTF-8");
		String md5Str = "corderid=" + corderid + "&" + signedSecKey + "&companyid=" + companyid;
		String md5Value = MD5Util.MD5Encode(md5Str, "UTF-8");
		String sign = request.getParameter("sign");
		if (sign == null || !sign.equalsIgnoreCase(md5Value)) {
			logger.error(LOG_KEY, "sign validate error : mine = " + md5Value + " their's" + sign);
			return null;
		}
		OrderFormPay orderFormPay = orderFormService.getOrderFormByCorderid(corderid, Integer.parseInt(companyid));
		if (orderFormPay == null) {
			return null;
		}
		// 保存通知电商接口地址
		if (StringUtils.isNotBlank(refundurl)) {
			orderFormService.addPayExtFiled(orderFormPay.getOrdernumber(), refundurl);
		}
		// 保存新的退款应收id
		if (StringUtils.isNotBlank(newcorderid)) {
			orderFormService.addPayMemoFiled(orderFormPay.getOrdernumber(), newcorderid);
		}
		List<Refund> refundList = orderFormService.getRefundByCorderid(corderid, Integer.parseInt(companyid));
		int totalRefund = 0;
		if (CollectionUtils.isNotEmpty(refundList)) {
			for (Refund refund : refundList) {
				totalRefund += refund.getRefundmoney();
			}
			// 已经退完了
			if (totalRefund >= Integer.parseInt(MoneyUtil.getPayMoney(orderFormPay.getMoney()))) {
				return null;
			}
		}
		Refund refund = new Refund();
		try {
			BeanUtils.copyProperties(refund, orderFormPay);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		refund.setStatus(0);
		String outerFundNo = CommonUtil.getOrderFormNumber();
		if(outerFundNo!=null && outerFundNo.length()>=20){
			outerFundNo = outerFundNo.substring(0,19);
			logger.info("中行外卡退款", "截取后的订单编号："+outerFundNo);
		}
		refund.setOuterfundno(outerFundNo);
		refund.setTotalmoney(orderFormPay.getPrice());
		String refund_fee = request.getParameter("refund_fee");
		String now = DateUtil.getCurrentDateStr(DateUtil.C_TIME_PATTON_DEFAULT);
		refund.setRefundDate(now);
		if (StringUtils.isEmpty(refund_fee)) {
			refund.setRefundmoney(orderFormPay.getPrice());
		} else {
			refund.setRefundmoney(Long.parseLong(refund_fee));
		}
		orderFormService.addRefund(refund);
		return refund;
	}
	
	/**
	 * 取消订单前
	 * 
	 * @param request
	 * @return
	 */
	protected OrderFormUnPay beforeCancelorder(HttpServletRequest request) {
		String corderid = request.getParameter("corderid");
		if (StringUtils.isEmpty(corderid)) {
			corderid = (String) request.getAttribute("corderid");
		}
		String companyid = request.getParameter("companyid");
		if (StringUtils.isEmpty(companyid)) {
			companyid = (String) request.getAttribute("companyid");
		}
		// 扩展字段回调通知电商接口地址
		String cancelurl = request.getParameter("cancelurl");
		if (StringUtils.isEmpty(cancelurl)) {
			cancelurl = (String) request.getAttribute("cancelurl");
		}
		if (StringUtils.isNotBlank(corderid) && StringUtils.isNotBlank(companyid)) {
			OrderFormUnPay orderFormPay = orderFormService.getOrderFormUnpayByCorderid(corderid, Integer.parseInt(companyid));
			// 已经成功或者没有这个订单
			if (orderFormPay == null) {
				return null;
			}
			// 保存通知电商接口地址
			if (StringUtils.isNotBlank(cancelurl)) {
				logger.info(LOG_KEY + ",cancelurl=", cancelurl);
				orderFormService.addUnpayExtFiled(orderFormPay.getOrdernumber(), cancelurl);
			}
			// 首先进行md5签名验证
			String secKey = orderFormPay.getCompanyid() + SECKEY_SUFFIX;
			String signedSecKey = MD5Util.MD5Encode(secKey, "UTF-8");
			String md5Str = "corderid=" + corderid + "&" + signedSecKey + "&companyid=" + companyid;
			String md5Value = MD5Util.MD5Encode(md5Str, "UTF-8");
			String sign = request.getParameter("sign");
			if (sign == null || !sign.equalsIgnoreCase(md5Value)) {
				logger.error(LOG_KEY, "sign validate error : mine = " + md5Value + " their's" + sign);
				return null;
			}

			return orderFormPay;
		}
		return null;
	}

	protected boolean beforeCheck(HttpServletRequest request) {
		// 签名验证
		String start = request.getParameter("start");
		String end = request.getParameter("end");
		String companyid = request.getParameter("companyid");
		if (companyid == null || start == null || end == null) {
			return false;
		}
		String secKey = companyid + SECKEY_SUFFIX;
		String signedSecKey = MD5Util.MD5Encode(secKey, "UTF-8");
		StringBuffer md5Str = new StringBuffer("");
		md5Str.append("start=").append(start).append("&").append(signedSecKey).append("&end=").append(end).append("&companyid=").append(companyid);

		String md5Value = MD5Util.MD5Encode(md5Str.toString(), "UTF-8");
		String sign = request.getParameter("sign");
		if (sign == null || !sign.equalsIgnoreCase(md5Value)) {
			logger.error(LOG_KEY, "sign validate error : mine = " + sign + " thire's:" + md5Value);
			return false;
		}
		return true;
	}

	protected OrderFormUnPay beforeServerNotify(String orderNo) {
		OrderFormUnPay orderFormUnpay = orderFormService.queryOrderFormUnPaymentByOrderNumber(orderNo);
		return orderFormUnpay;
	}

	/**
	 * 服务端通知处理
	 * 
	 * @param orderFormUnpay
	 * @param transeq
	 * @return
	 */
	protected boolean afterServerNotify(OrderFormUnPay orderFormUnpay, String transeq) {
		try {
			if (orderFormUnpay != null) {
				long start = System.currentTimeMillis();
				// 处理数据
				OrderFormPay form = payService.doPaySuccess(orderFormUnpay, transeq);
				long end = System.currentTimeMillis();
				logger.debug(LogFlags.TIME_PRINT, "add doPaySuccess time : " + (end - start));
				payService.doServerNotify(form);
				// messageQueue.putMessage(new ServerNotifyMessage(form));
				start = System.currentTimeMillis();
				logger.debug(LogFlags.TIME_PRINT, "add doServerNotify time : " + (start - end));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	protected boolean afterPageNotify(OrderFormPay orderForm, HttpServletResponse response) {
		try {
			payService.doPageNotify(orderForm, response);
		} catch (Exception e) {
			logger.error(LOG_KEY, "do page notify error, ordernumber: " + orderForm.getOrdernumber() + "  message:" + e.getMessage());
			return false;
		}

		return true;
	}

	protected boolean afterRefund(String orderNum) {
		boolean result = false;
		result = payService.updateOrderStatus(orderNum, PayStatus.REFUND_SUCCESS);
		return result;
	}

	/**
	 * 支付宝设置用户信息
	 * 
	 * @param form
	 */
	public void saveUserInfo(HttpServletRequest request) {
		// TODO 后续处理
	}
	/**
	 * 招行支付前调用，
	 * 
	 * @param request
	 * @return
	 */
	protected OrderFormUnPay CmbBeforePay(HttpServletRequest request, int payTypeId) {
		OrderFormUnPay form = RequestBean.getObject(OrderFormUnPay.class, request);
		try {
			// TODO 验证用户身份有效性
			form.setPaytype(payTypeId);
			String now = DateUtil.getCurrentDateStr(DateUtil.C_TIME_PATTON_DEFAULT);
			form.setSubmitdate(now);
			form.setIp(RequestUtil.getClientIp(request));
			// 生成唯一支付订单号
			form.setOrdernumber(CommonUtil.getCmbOrderFormNumber());
			// 获取签名数据项
			 String sign = request.getParameter("sign");
			// TODO 测试阶段先去掉签名验证 后续打开签名验证
			 if (!payService.validateSign(form, sign)) {
				 logger.error(LOG_KEY, "签名不通过：\t传过来的sign: " + sign + "\t正常的sign："
			+ payService.getSignStr(form));
			 return null;
			 }
			// TODO 考虑到安全性，需要去业务系统重新获取价格
			// 后台重新获取价格失败或者异常时 返回支付失败
			if (StringUtils.isBlank(form.getMoney())) {
				return null;
			}
			// 商户必须传订单号
			if (StringUtils.isBlank(form.getCorderid()) || form.getCorderid().equals("0")) {
				return null;
			}
			// 支付金额转化为分单位 最小为一分钱
			form.setPrice(Long.parseLong(MoneyUtil.getPayMoney(form.getMoney())));
			if (form.getPrice() < 1) {
				return null;
			}
			orderFormService.addOrderUnPay(form);
		} catch (Exception e) {
			try {
				form.setOrdernumber(CommonUtil.getCmbOrderFormNumber());
				orderFormService.addOrderUnPay(form);
			} catch (Exception e2) {
				e.printStackTrace();
				return null;
			}
		}
		return form;
	}
	/**
	 * 中国银行，支付前调用
	 * 
	 * @param request
	 * @return
	 */
	protected OrderFormUnPay BocBeforePay(HttpServletRequest request, int payTypeId) {
		OrderFormUnPay form = RequestBean.getObject(OrderFormUnPay.class, request);
		try {
			// TODO 验证用户身份有效性
			form.setPaytype(payTypeId);
			String now = DateUtil.getCurrentDateStr(DateUtil.C_TIME_PATTON_DEFAULT);
			form.setSubmitdate(now);
			form.setIp(RequestUtil.getClientIp(request));
			// 生成唯一支付订单号，系统默认使用的是 20位 时间+6 随机数，中行最大支持19位，去掉最后一个随机数
			String orderNumberTemp = CommonUtil.getOrderFormNumber();
			if(orderNumberTemp != null && orderNumberTemp.length() >= 20){
				orderNumberTemp = orderNumberTemp.substring(0, 19);
				logger.info("info","截取后的支付单号："+orderNumberTemp);
			}
			form.setOrdernumber(orderNumberTemp);
			// 获取签名数据项
			 String sign = request.getParameter("sign");
			// TODO 测试阶段先去掉签名验证 后续打开签名验证
			 if (!payService.validateSign(form, sign)) {
				 logger.error(LOG_KEY, "签名不通过：\t传过来的sign: " + sign + "\t正常的sign："
			+ payService.getSignStr(form));
			 return null;
			 }
			// TODO 考虑到安全性，需要去业务系统重新获取价格
			// 后台重新获取价格失败或者异常时 返回支付失败
			if (StringUtils.isBlank(form.getMoney())) {
				return null;
			}
			// 商户必须传订单号
			if (StringUtils.isBlank(form.getCorderid()) || form.getCorderid().equals("0")) {
				return null;
			}
			// 支付金额转化为分单位 最小为一分钱
			form.setPrice(Long.parseLong(MoneyUtil.getPayMoney(form.getMoney())));
			if (form.getPrice() < 1) {
				return null;
			}
			orderFormService.addOrderUnPay(form);
		} catch (Exception e) {
			try {
				form.setOrdernumber(CommonUtil.getOrderFormNumber());
				orderFormService.addOrderUnPay(form);
			} catch (Exception e2) {
				e.printStackTrace();
				return null;
			}
		}
		return form;
	}
	
	/**
	 * 中行外卡，支付前调用
	 * 
	 * @param request
	 * @return
	 */
	protected OrderFormUnPay BocOutBeforePay(HttpServletRequest request, int payTypeId) {
		OrderFormUnPay form = RequestBean.getObject(OrderFormUnPay.class, request);
		try {
			// TODO 验证用户身份有效性
			form.setPaytype(payTypeId);
			String now = DateUtil.getCurrentDateStr(DateUtil.C_TIME_PATTON_DEFAULT);
			form.setSubmitdate(now);
			form.setIp(RequestUtil.getClientIp(request));
			// 生成唯一支付订单号，系统默认使用的是 20位 时间+6 随机数，中行最大支持19位，去掉最后一个随机数
			String orderNumberTemp = CommonUtil.getOrderFormNumber();
			if(orderNumberTemp != null && orderNumberTemp.length() >= 20){
				orderNumberTemp = orderNumberTemp.substring(0, 19);
				logger.info("info","截取后的支付单号："+orderNumberTemp);
			}
			form.setOrdernumber(orderNumberTemp);
			// 获取签名数据项
			 String sign = request.getParameter("sign");
			// TODO 测试阶段先去掉签名验证 后续打开签名验证
			 if (!payService.validateSign(form, sign)) {
				 logger.error(LOG_KEY, "签名不通过：\t传过来的sign: " + sign + "\t正常的sign："
			+ payService.getSignStr(form));
			 return null;
			 }
			// TODO 考虑到安全性，需要去业务系统重新获取价格
			// 后台重新获取价格失败或者异常时 返回支付失败
			if (StringUtils.isBlank(form.getMoney())) {
				return null;
			}
			// 商户必须传订单号
			if (StringUtils.isBlank(form.getCorderid()) || form.getCorderid().equals("0")) {
				return null;
			}
			// 支付金额转化为分单位 最小为一分钱
			form.setPrice(Long.parseLong(MoneyUtil.getPayMoney(form.getMoney())));
			if (form.getPrice() < 1) {
				return null;
			}
			orderFormService.addOrderUnPay(form);
		} catch (Exception e) {
			try {
				form.setOrdernumber(CommonUtil.getOrderFormNumber());
				orderFormService.addOrderUnPay(form);
			} catch (Exception e2) {
				e.printStackTrace();
				return null;
			}
		}
		return form;
	}
	
}
