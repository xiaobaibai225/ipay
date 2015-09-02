package com.pay.framework.payment.alipay;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pay.framework.http.HttpRequester;
import com.pay.framework.http.HttpResponse;
import com.pay.framework.http.RequestUtil;
import com.pay.framework.log.LogFlags;
import com.pay.framework.log.LogManager;
import com.pay.framework.payment.BasePay;
import com.pay.framework.payment.ICodePay;
import com.pay.framework.payment.IMobilePay;
import com.pay.framework.payment.IPay;
import com.pay.framework.payment.alipay.config.AlipayConfig;
import com.pay.framework.payment.alipay.util.AlipayNotify;
import com.pay.framework.payment.alipay.util.AlipaySubmit;
import com.pay.framework.service.OrderFormService;
import com.pay.framework.service.PayService;
import com.pay.framework.util.DateUtil;
import com.pay.framework.util.MD5Util;
import com.pay.framework.util.XmlUtil;
import com.pay.model.Check;
import com.pay.model.ComplexOrderBean;
import com.pay.model.OrderFormPay;
import com.pay.model.OrderFormUnPay;
import com.pay.model.PayStatus;
import com.pay.model.PaymentBean;
import com.pay.model.Refund;

/**
 * 支付宝wap支付实现类
 * 
 * @author PCCW
 * 
 */
@Component("alipayWap")
public class AlipayWap extends BasePay implements IPay, IMobilePay, ICodePay {

	LogManager logManager = LogManager.getLogger(AlipayWap.class);

	@Autowired
	private OrderFormService orderFormService;

	@Autowired
	private PayService payService;

	public String getForwardUrl(HttpServletRequest request, HttpServletResponse response, int payTypeId) {
		/*//处理支付通用逻辑
		OrderFormUnPay orderform = beforePay(request, payTypeId);
		if (null == orderform) {
			return null;
		}
		PaymentBean payment = payTypeService.getPayType(orderform.getPaytype());
		// 支付类型
		String payment_type = "1";
		// 必填，不能修改
		// 服务器异步通知页面路径
		String notify_url = payment.getBgurl();
		// 页面跳转同步通知页面路径
		String return_url = payment.getPageurl();
		// 卖家支付宝帐户
		//String seller_email = payment.getExt();
		// 商户订单号
		String out_trade_no = orderform.getOrdernumber();
		// 订单名称
		String subject = orderform.getProductname();
		// 付款金额
		String total_fee = orderform.getMoney();
		// 订单描述
		String body = orderform.getProductdesc();
		// 商品展示地址
		String show_url = orderform.getFronturl();
		
		// 把请求参数打包成数组
		Map<String, String> sParaTemp = new HashMap<String, String>();
		sParaTemp.put("service", "alipay.wap.create.direct.pay.by.user");
		sParaTemp.put("partner", payment.getMerchantid());
		sParaTemp.put("_input_charset", AlipayConfig.input_charset);
		sParaTemp.put("notify_url", notify_url);
		sParaTemp.put("return_url", return_url);
		sParaTemp.put("out_trade_no", out_trade_no);
		sParaTemp.put("subject", subject);
		sParaTemp.put("total_fee", total_fee);
		sParaTemp.put("body", body);
		sParaTemp.put("seller_id", payment.getMerchantid());
		sParaTemp.put("payment_type", payment_type);
		sParaTemp.put("show_url", show_url);
		//sParaTemp.put("it_b_pay", "30m");
		// 建立请求
		String sHtmlText = AlipaySubmit.buildWapForwardUrl(payment.getPosturl(),sParaTemp, payment.getPrikey());
		return sHtmlText;*/
		return null;
	}

	public int doPaymentServerNotify(HttpServletRequest request, HttpServletResponse response) {
		Map<String, String> parameterMap = RequestUtil.getParamaterMap(request);
		String order_no = parameterMap.get("out_trade_no");
		OrderFormUnPay orderFormUnpay = orderFormService.queryOrderFormUnPaymentByOrderNumber(order_no);
		if (orderFormUnpay == null) {
			try {
				PrintWriter out = response.getWriter();
				out.println("success");
				out.flush();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return PayStatus.PAY_NO_ORDER;
		}
		PaymentBean payment = payTypeService.getPayType(orderFormUnpay.getPaytype());

		try {
			if (StringUtils.isNotEmpty(parameterMap.get("body"))) {
				String body = URLDecoder.decode(parameterMap.get("body"), "utf-8");
				request.setAttribute("body", body);
			} else {
				request.setAttribute("body", "");
			}

		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		try {
			String subject = URLDecoder.decode(parameterMap.get("subject"), "utf-8");
			request.setAttribute("subject", subject);
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		String trade_status = "";
		try {
			trade_status = new String(parameterMap.get("trade_status").getBytes("ISO-8859-1"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		if (AlipayNotify.verifyWap(parameterMap, payment.getMerchantid(), payment.getPubkey())) {// 验证成功
			boolean notifySuccess =true;
			if (trade_status.equals("TRADE_FINISHED") || trade_status.equals("TRADE_SUCCESS")) {

				String total_fee = (String) parameterMap.get("total_fee");

				if ((new java.math.BigDecimal(total_fee).multiply(new java.math.BigDecimal(100)).intValue()) < (new java.math.BigDecimal(
						orderFormUnpay.getPrice()).intValue())) {
					try {
						PrintWriter out = response.getWriter();
						out.println("fail");
						out.flush();
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					return PayStatus.PAY_NOT_MATCH;
				}
			  	super.afterServerNotify(orderFormUnpay, parameterMap.get("trade_no"));
			}
			try {
				PrintWriter out = response.getWriter();
				if(notifySuccess){
					out.println("success");
				}else{
					out.println("fail");
				}
				out.flush();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return PayStatus.PAY_SUCCESS;
		} else {// 验证失败
			try {
				PrintWriter out = response.getWriter();
				out.println("fail");
				out.flush();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return PayStatus.PAY_FAIL;
		}
	}
	
	public OrderFormUnPay getOrderFormUnPay(HttpServletRequest request, HttpServletResponse response) {
		String out_trade_no = "";
		try {
			out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ComplexOrderBean complexOrderBean = payService.queryComplexOrderBeanByOrderNumber(out_trade_no);
		OrderFormUnPay orderUnpay = complexOrderBean.getOrderformunpay();
		return orderUnpay;
	}

	public int doPaymentPageNotify(HttpServletRequest request, HttpServletResponse response) {
		//获取支付宝GET过来反馈信息
		Map<String, String> params = new HashMap<String, String>();
		@SuppressWarnings("unchecked")
		Map<String, String[]> requestParams = request.getParameterMap();
		for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			String[] values = (String[]) requestParams.get(name);
			String valueStr = "";
			for (int i = 0; i < values.length; i++) {
				valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
			}
			params.put(name, valueStr);
		}

		String out_trade_no = "";
		try {
			out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ComplexOrderBean complexOrderBean = payService.queryComplexOrderBeanByOrderNumber(out_trade_no);
		OrderFormPay orderForm = complexOrderBean.getOrderform();
		if (null == orderForm) {
			return PayStatus.PAY_NO_ORDER;
		}
		PaymentBean payment = payTypeService.getPayType(orderForm.getPaytype());
		String trade_status = "";
		try {
			trade_status = new String(request.getParameter("trade_status").getBytes("ISO-8859-1"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		if (AlipayNotify.verifyWap(params, payment.getMerchantid(), payment.getPubkey())) {// 验证成功
			
			if (trade_status.equals("TRADE_FINISHED")) {
				
				if (afterPageNotify(orderForm, response)) {
					return PayStatus.PAY_SUCCESS;
				}
			} else if (trade_status.equals("TRADE_SUCCESS")) {
				
				if (afterPageNotify(orderForm, response)) {
					return PayStatus.PAY_SUCCESS;
				}
			}
		}else{
			return PayStatus.PAY_FAIL; // 请不要修改或删除
		}
		return PayStatus.PAY_FAIL; // 请不要修改或删除
	}

	public int refundNotify(HttpServletRequest request, HttpServletResponse response) {
		// 获取支付宝POST过来反馈信息
		Map<String, String> params = new HashMap<String, String>();
		Map requestParams = request.getParameterMap();
		for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			String[] values = (String[]) requestParams.get(name);
			String valueStr = "";
			for (int i = 0; i < values.length; i++) {
				valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
			}

			params.put(name, valueStr);
		}

		String result_details = request.getParameter("result_details"); // 处理结果详情
		String batch_no = request.getParameter("batch_no"); // 处理结果详情

		List<Refund> refundList = orderFormService.getRefundByOuterfundno(batch_no);
		if (refundList == null || refundList.size() < 1) {
			return PayStatus.PAY_FAIL;
		}
		Refund refund = refundList.get(0);

		PaymentBean payment = payTypeService.getPayType(refund.getPaytype());


		if (AlipayNotify.verify(params, payment.getMerchantid(), payment.getSeckey())) {// 验证成功

			String[] resultArray = result_details.split("\\^");
			String resuletSuccess = resultArray[resultArray.length - 1];
			if (resuletSuccess != null && resuletSuccess.equalsIgnoreCase("SUCCESS")) {
				orderFormService.updatePayStatus(refund.getOrdernumber(), PayStatus.PAY_SUCCESS_REFUND);
				orderFormService.updateRefund(refund.getOuterfundno(), PayStatus.REFUND_SUCCESS);
				OrderFormPay orderFormPay = orderFormService.getOrderByOrderNum(refund.getOrdernumber());
				// kapstoy 调用消费接口
				HttpRequester httpRequester = new HttpRequester();
				HttpResponse httpResponse = null;
				String url = orderFormPay.getExt();
				url = url + "&corderid=" + orderFormPay.getCorderid();
				url=url+"&newcorderid="+orderFormPay.getMemo();
				BigDecimal aa=new BigDecimal(resultArray[1]).multiply(new BigDecimal(100));
				url = url + "&money=" +aa.intValue();
				String secKey = orderFormPay.getCompanyid() + SECKEY_SUFFIX;
				String signedSecKey = MD5Util.MD5Encode(secKey, "UTF-8");
				String md5Str = "corderid=" + orderFormPay.getCorderid() + "&" + signedSecKey + "&companyid=" + orderFormPay.getCompanyid();
				String md5Value = MD5Util.MD5Encode(md5Str, "UTF-8");
				url = url + "&sign=" + md5Value;
				try {
					httpResponse = httpRequester.sendGet(url.replaceAll("\\s", ""));
				} catch (IOException e) {
					logManager.debug("ALIPAY", "支付宝退款通知处理业务 server error url:" + url + ",corderid=" + orderFormPay.getCorderid()
							+ ",orderid=" + orderFormPay.getOrdernumber() + ", errormsg:" + e.getMessage());
					e.printStackTrace();
					return PayStatus.PAY_FAIL;
				}
				String content = httpResponse.getContent();
				if (content != null && content.equalsIgnoreCase("success")) {
					logManager.debug("alipay refund,", "支付宝退款通知处理业务 成功 url:" + url + ",corderid=" + orderFormPay.getCorderid() );
					orderFormService.updateRefund(refund.getOuterfundno(), 3);//3的状态时退款成功，业务处理也成功
				}
				else
				{
					logManager.debug("alipay refund,", "支付宝退款通知处理业务 失败 url:" + url + ",corderid=" +orderFormPay.getCorderid());
					return PayStatus.PAY_FAIL;
				}
				return PayStatus.PAY_SUCCESS; // 请不要修改或删除
			}

		} else {// 验证失败
			return PayStatus.PAY_FAIL;
		}
		return PayStatus.PAY_SUCCESS;
	}

	@Override
	public String refund(HttpServletRequest request, HttpServletResponse response) {

		Refund refund = super.beforeRefund(request);
		logManager.debug("保存退款信息成功", null);
		if (refund == null) {
			JSONObject json = new JSONObject();
			json.put("status", "0");
			json.put("errormsg", "already refund all");
			return json.toString();
		}
		PaymentBean payment = payTypeService.getPayType(refund.getPaytype());

		String batch_no = refund.getOuterfundno();

		String refund_date = DateUtil.getCurrentDateStr(DateUtil.C_TIME_PATTON_DEFAULT);

		String batch_num = "1";
		String detail_data = refund.getTranseq() + "^" + refund.getRefundmoney()*1.00/100 + "^商户退款";

		Map<String, String> sParaTemp = new HashMap<String, String>();
		sParaTemp.put("batch_no", batch_no);
		sParaTemp.put("seller_email", payment.getExt());
		sParaTemp.put("refund_date", refund_date);
		sParaTemp.put("batch_num", batch_num);
		sParaTemp.put("notify_url", payment.getExtCol1());
		sParaTemp.put("detail_data", detail_data);
		// 构造函数，生成请求URL
		String sHtmlText = "";

		JSONObject json = new JSONObject();
		int status = 0;
		long price = refund.getRefundmoney();
		String errormsg = "";
		logManager.debug("开始组装退款报文", null);
		try {
			sHtmlText = AlipayService.refund_fastpay_by_platform_pwd(payment.getPosturl(),sParaTemp, payment.getMerchantid(), payment.getSeckey());
			logManager.debug("请求退款的posturl为", sHtmlText);
			response.sendRedirect(sHtmlText);
			List<String> confirm = XmlUtil.getContentByKey(sHtmlText, "//alipay/is_success");
			if (confirm == null || confirm.size() < 1) {
				status = PayStatus.PAY_FAIL;
				errormsg = "return message is null!";
			} else {
				String confirmValue = confirm.get(0);
				if (confirmValue.toLowerCase().equals("t")) {
					status = PayStatus.PAY_SUCCESS;
					afterRefund(refund.getOrdernumber());
					orderFormService.updatePayStatus(refund.getOrdernumber(), PayStatus.PAY_SUCCESS_REFUND);
				} else {
					List<String> errormessage = XmlUtil.getContentByKey(sHtmlText, "//alipay/error");
					if (errormessage == null || confirm.size() < 1) {
						status = PayStatus.PAY_FAIL;
						for (String one : errormessage) {
							errormsg += (one + " ");
						}
					} else {
						status = PayStatus.PAY_FAIL;
						errormsg = "return message is null";
					}
				}
			}
		} catch (Exception e) {
			status = PayStatus.PAY_FAIL;
			errormsg = "unk";
		}
		if (status == PayStatus.PAY_SUCCESS) {
			json.put("success", true);
		}
		json.put("status", status);
		json.put("corderid", refund.getCorderid());
		json.put("money", price);
		json.put("errormsg", errormsg);
		return json.toString();
	}

	@Override
	public List<Check> check(HttpServletRequest request, HttpServletResponse response, int payTypeId) throws Exception {
		if (!beforeCheck(request)) {
			return null;
		}
		String companyId = request.getParameter("companyid");

		PaymentBean payment = payTypeService.getPayType(payTypeId);

		String logon_id = payment.getExt();
		String page_no = "1";

		// 要查询的开始时间
		String gmt_start_time = (String) request.getAttribute("starttime");

		// 要查询的结束时间
		String gmt_end_time = (String) request.getAttribute("endtime");

		// 把请求参数打包成数组
		Map<String, String> sParaTemp = new HashMap<String, String>();
		sParaTemp.put("gmt_start_time", gmt_start_time + " 00:00:00");
		sParaTemp.put("gmt_end_time", gmt_end_time + " 00:00:00");
		sParaTemp.put("logon_id", logon_id);
		sParaTemp.put("page_no", page_no);
		sParaTemp.put("page_size", "50000");

		// 构造函数，生成请求URL
		String sHtmlText = "";
		List<Check> retCheck = new ArrayList<Check>();
		try {
			sHtmlText = AlipayService.account_page_query(sParaTemp, payment.getMerchantid(), payment.getSeckey());
			List<String> confirm = XmlUtil.getContentByKey(sHtmlText, "//alipay/is_success");
			if (confirm == null || confirm.size() < 1) {
				return retCheck;
			} else {
				String confirmValue = confirm.get(0);
				if (confirmValue.toLowerCase().equals("t")) {
					retCheck = XmlUtil.getCheckResult(sHtmlText,
							"//alipay/response/account_page_query_result/account_log_list/AccountQueryAccountLogVO");
					if (retCheck == null) {
						return new ArrayList<Check>();
					}
					int loop = 0;
					Iterator<Check> it = retCheck.iterator();
					while (it.hasNext()) {

						Check check = it.next();
						OrderFormPay orderFormPay = payService.getOrderFormByOrderNumPay(check.getOrdernumber());
						if (orderFormPay != null) {
						
							if(check.getPrice().contains("-")) check.setCorderid(orderFormPay.getMemo());//如果是退款金额，则取退款的应收单
							else 	check.setCorderid(orderFormPay.getCorderid());//如果取得数据为付款金额，则取原账单
						}
						check.setPaytype(payTypeId+"");
						loop++;
						if (loop % 500 == 0) {
							Thread.sleep(100);
						}
					}

					return retCheck;
				} else {
					return retCheck;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retCheck;
	}

	@Override
	public String getMobileForwardParameter(HttpServletRequest request, HttpServletResponse response, int payTypeId) {
		logManager.debug("获取支付宝wap URL开始","获取支付宝wap URL开始 typeId="+payTypeId);
		//处理支付通用逻辑
		OrderFormUnPay orderform = beforePay(request, payTypeId);
		if (null == orderform) {
			return null;
		}
		PaymentBean payment = payTypeService.getPayType(orderform.getPaytype());
		// 支付类型
		String payment_type = "1";
		// 必填，不能修改
		// 服务器异步通知页面路径
		String notify_url = payment.getBgurl();
		// 页面跳转同步通知页面路径
		String return_url = payment.getPageurl();
		// 卖家支付宝帐户
		//String seller_email = payment.getExt();
		// 商户订单号
		String out_trade_no = orderform.getOrdernumber();
		// 订单名称
		String subject = orderform.getProductname();
		// 付款金额
		String total_fee = orderform.getMoney();
		// 订单描述
		String body = orderform.getProductdesc();
		// 商品展示地址
		String show_url = orderform.getFronturl();
		
		// 把请求参数打包成数组
		Map<String, String> sParaTemp = new HashMap<String, String>();
		sParaTemp.put("service", "alipay.wap.create.direct.pay.by.user");
		sParaTemp.put("partner", payment.getMerchantid());
		sParaTemp.put("_input_charset", AlipayConfig.input_charset);
		sParaTemp.put("notify_url", notify_url);
		sParaTemp.put("return_url", return_url);
		sParaTemp.put("out_trade_no", out_trade_no);
		sParaTemp.put("subject", subject);
		sParaTemp.put("total_fee", total_fee);
		sParaTemp.put("body", body);
		sParaTemp.put("seller_id", payment.getMerchantid());
		sParaTemp.put("payment_type", payment_type);
		sParaTemp.put("show_url", show_url);
		//sParaTemp.put("it_b_pay", "30m");
		logManager.debug("请求参数","notify_url="+notify_url+"return_url="+return_url+"subject="+subject+"total_fee="+total_fee+"body="+body+"show_url="+show_url+"out_trade_no="+out_trade_no);
		// 建立请求
		String sHtmlText = AlipaySubmit.buildWapForwardUrl(payment.getPosturl(),sParaTemp, payment.getPrikey());
		logManager.debug("获取支付宝wap URL结束","获取支付宝wap URL结束 url="+sHtmlText);
		return sHtmlText;
	}

	@Override
	public int mobileServerNotify(int payTypeId,HttpServletRequest request, HttpServletResponse response) {
			return PayStatus.PAY_FAIL;
	}

	@Override
	public String getWapForwardParameter(HttpServletRequest request, HttpServletResponse response, int payTypeId) {
		return null ;
	}

	public String getPhoneMessageForwardParameter(HttpServletRequest request, HttpServletResponse response, int payTypeId) {
		return null ;
	}


	@Override
	public int wapServerNotify(HttpServletRequest request, HttpServletResponse response) {
		return PayStatus.PAY_FAIL;

	}

	@Override
	public String getIapResult(HttpServletRequest request, HttpServletResponse response, int payTypeId) {
		return null;
	}

	@Override
	public String getCodeForwardParameter(HttpServletRequest request, HttpServletResponse response, int payTypeId) {
		return null;
	}

	@Override
	public int codeServerNotify(HttpServletRequest request, HttpServletResponse response) {
		return 0;
	}

}
