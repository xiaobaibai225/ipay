package com.pay.framework.payment.tenpay;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pay.framework.http.RequestUtil;
import com.pay.framework.log.LogManager;
import com.pay.framework.payment.BasePay;
import com.pay.framework.payment.IPay;
import com.pay.framework.service.OrderFormService;
import com.pay.framework.service.PayService;
import com.pay.framework.util.DateUtil;
import com.pay.framework.util.MD5Util;
import com.pay.framework.util.MoneyUtil;
import com.pay.framework.util.StringUtil;
import com.pay.model.Check;
import com.pay.model.ComplexOrderBean;
import com.pay.model.OrderFormPay;
import com.pay.model.OrderFormUnPay;
import com.pay.model.PayStatus;
import com.pay.model.PaymentBean;
import com.pay.model.Refund;

@Component("tenpay")
public class TenPay extends BasePay implements IPay{

	LogManager logManager = LogManager.getLogger(TenPay.class);

	@Autowired
	private OrderFormService orderFormService;

	@Autowired
	private PayService payService;

	public static final String C_TIME_PATTON_DEFAULT = "yyyyMMddhhmmss";

	public static final DateFormat df = new SimpleDateFormat(C_TIME_PATTON_DEFAULT);

	@Override
	public String getForwardUrl(HttpServletRequest request, HttpServletResponse response, int payTypeId) {
		OrderFormUnPay orderform = super.beforePay(request, payTypeId);
		if (null == orderform) {
			return null;
		}

		PaymentBean payment = payTypeService.getPayType(orderform.getPaytype());

		String partner = payment.getMerchantid();
		String key = payment.getSeckey();

		String return_url = payment.getPageurl();
		String notify_url = payment.getBgurl();

		String out_trade_no = orderform.getOrdernumber();

		RequestHandler reqHandler = new RequestHandler(request, response);
		reqHandler.init();
		reqHandler.setKey(key);
		reqHandler.setGateUrl(payment.getPosturl());

		reqHandler.setParameter("partner", partner); // 商户号
		reqHandler.setParameter("out_trade_no", out_trade_no); // 商家订单号

		reqHandler.setParameter("total_fee", orderform.getPrice()+"");
		reqHandler.setParameter("return_url", return_url); // 交易完成后跳转的URL
		reqHandler.setParameter("notify_url", notify_url); // 接收财付通通知的URL

		reqHandler.setParameter("bank_type", "WX"); 
		reqHandler.setParameter("spbill_create_ip", request.getRemoteAddr()); // 用户的公网ip
		reqHandler.setParameter("fee_type", "1");

		reqHandler.setParameter("sign_type", "MD5");
		reqHandler.setParameter("service_version", "1.0");
		reqHandler.setParameter("input_charset", "UTF-8");
		reqHandler.setParameter("sign_key_index", "1");
		reqHandler.setParameter("body", orderform.getProductname());


		reqHandler.setParameter("time_start", df.format(new Date()));

		reqHandler.setParameter("buyer_id", "");
		reqHandler.setParameter("goods_tag", "");


		// 请求的url
		String requestUrl = "";
		try {
			requestUrl = reqHandler.getRequestURL();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return requestUrl;
	}

	@Override
	public int doPaymentServerNotify(HttpServletRequest request, HttpServletResponse response) {
		Map<String, String> parameterMap = RequestUtil.getParamaterMap(request);

		String order_no = parameterMap.get("out_trade_no");
		OrderFormUnPay orderFormUnpay = orderFormService.queryOrderFormUnPaymentByOrderNumber(order_no);
		if (orderFormUnpay == null) {
			return PayStatus.PAY_NO_ORDER;
		}

		PaymentBean payment = payTypeService.getPayType(orderFormUnpay.getPaytype());

		String partner = payment.getMerchantid();

		String key = payment.getSeckey();


		ResponseHandler resHandler = new ResponseHandler(request, response);
		resHandler.setKey(key);


		if (resHandler.isTenpaySign()) {
		
			String notify_id = resHandler.getParameter("notify_id");

		
			RequestHandler queryReq = new RequestHandler(null, null);
		
			TenpayHttpClient httpClient = new TenpayHttpClient();
		
			ClientResponseHandler queryRes = new ClientResponseHandler();

			
			queryReq.init();
			queryReq.setKey(key);
			queryReq.setGateUrl("https://gw.tenpay.com/gateway/verifynotifyid.xml");
			queryReq.setParameter("partner", partner);
			queryReq.setParameter("notify_id", notify_id);

	
			httpClient.setTimeOut(5);
	
			try {
				httpClient.setReqContent(queryReq.getRequestURL());
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			// 后台调用
			if (httpClient.call()) {
			
				try {
					queryRes.setContent(httpClient.getResContent());
				} catch (Exception e) {
					e.printStackTrace();
				}
				queryRes.setKey(key);

			
				String retcode = queryRes.getParameter("retcode");
				String trade_state = resHandler.getParameter("trade_state");

				String trade_mode = resHandler.getParameter("trade_mode");

		
				if (queryRes.isTenpaySign() && "0".equals(retcode) && "0".equals(trade_state) && "1".equals(trade_mode)) {

					String total_fee = (String) parameterMap.get("total_fee");

					// 判断金额
					if ((new java.math.BigDecimal(total_fee).multiply(new java.math.BigDecimal(100)).intValue()) < (new java.math.BigDecimal(
							orderFormUnpay.getPrice()).multiply(new java.math.BigDecimal(100)).intValue())) {
						return PayStatus.PAY_NOT_MATCH;
					}

					logManager.debug(
							null,
							"payment success:from=tenpay,username=" + orderFormUnpay.getUsername() + ",ordernumber="
									+ orderFormUnpay.getOrdernumber() + ",payamount=" + total_fee + ",ip=" + request.getRemoteAddr());

					if (afterServerNotify(orderFormUnpay, (String) parameterMap.get("transaction_id"))) {
						try {
							resHandler.sendToCFT("success");
						} catch (IOException e) {
							e.printStackTrace();
						}
						return PayStatus.PAY_SUCCESS;
					}
					return PayStatus.PAY_FAIL;
				} else {
			
					return PayStatus.PAY_VALIDATE_FAIL;
				}

			} else {
				return PayStatus.PAY_FAIL;
			}
		} else {
			return PayStatus.PAY_FAIL;
		}

	}

	@Override
	public int doPaymentPageNotify(HttpServletRequest request, HttpServletResponse response) {

		String orderNum = request.getParameter("out_trade_no");
		ComplexOrderBean complexOrderBean = payService.queryComplexOrderBeanByOrderNumber(orderNum);
		OrderFormPay orderForm = complexOrderBean.getOrderform();
		if (null == orderForm) {
			return PayStatus.PAY_NO_ORDER;
		}

		PaymentBean payment = payTypeService.getPayType(orderForm.getPaytype());
		String key = payment.getSeckey();
		
		ResponseHandler resHandler = new ResponseHandler(request, response);

		resHandler.setKey(key);

	
		if (resHandler.isTenpaySign()) {


			String trade_state = resHandler.getParameter("trade_state");

			String trade_mode = resHandler.getParameter("trade_mode");

			if ("0".equals(trade_state) && "1".equals(trade_mode)) {

				if (afterPageNotify(orderForm, response)) {
					return PayStatus.PAY_SUCCESS;
				}
				return PayStatus.PAY_FAIL;
			} else {
				return PayStatus.PAY_NOT_MATCH;
			}

			// 
		} else {
			return PayStatus.PAY_VALIDATE_FAIL;
		}

	}

	@Override
	public String refund(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Refund refund = super.beforeRefund(request);
		if (refund == null) {
			JSONObject json = new JSONObject();
			json.put("status", "0");
			json.put("errormsg", "already refund all");
			return json.toString();
		}
		PaymentBean payment = payTypeService.getPayType(refund.getPaytype());

		String partner = payment.getMerchantid();
	
		String key = payment.getSeckey();

	
		RequestHandler reqHandler = new RequestHandler(null, null);
		
		TenpayHttpClient httpClient = new TenpayHttpClient();
	
		ClientResponseHandler resHandler = new ClientResponseHandler();


		reqHandler.init();
		reqHandler.setKey(key);
		reqHandler.setGateUrl("https://mch.tenpay.com/refundapi/gateway/refund.xml");

		// -----------------------------
		// 设置接口参数
		// -----------------------------
		reqHandler.setParameter("partner", partner);
		reqHandler.setParameter("out_trade_no", refund.getOrdernumber());
		reqHandler.setParameter("transaction_id", refund.getTranseq());
		reqHandler.setParameter("out_refund_no", refund.getOuterfundno());
		reqHandler.setParameter("total_fee",refund.getTotalmoney()+"");// 先不考虑分批退款，直接用refundfee
		String refund_fee = request.getParameter("refund_fee");
		if (StringUtils.isEmpty(refund_fee)) {
			reqHandler.setParameter("refund_fee", refund.getRefundmoney()+"");
		} else {
			reqHandler.setParameter("refund_fee", MoneyUtil.getPayMoney(refund_fee));
		}

		reqHandler.setParameter("op_user_id", payment.getExtCol1());	
		reqHandler.setParameter("op_user_passwd", payment.getQuerykey());

		httpClient.setTimeOut(5);
	
		httpClient.setCaInfo(new File(payment.getPubkey()));


		httpClient.setCertInfo(new File(payment.getPrikey()), payment.getMerchantpwd());

	
		httpClient.setMethod("POST");

	
		String requestUrl = reqHandler.getRequestURL();
		httpClient.setReqContent(requestUrl);
		String rescontent = "null";

		JSONObject json = new JSONObject();
		int status = 0;
		String price = refund.getRefundmoney()+"";
		String errormsg = "";

		if (httpClient.call()) {
	
			rescontent = httpClient.getResContent();
			resHandler.setContent(rescontent);
			resHandler.setKey(key);

	
			String retcode = resHandler.getParameter("retcode");

			
			if (resHandler.isTenpaySign() && "0".equals(retcode)) {
	
				String refund_status = resHandler.getParameter("refund_status");
	
				String[] successStat = { "4", "10", "8", "9", "11" };
				String[] failStat = { "1", "2", "7", "3", "5", "6", "9", "11" };

				if (refund_status != null && StringUtil.contains(successStat, refund_status)) {
					super.afterRefund(refund.getOrdernumber());
					orderFormService.updateRefund(refund.getOuterfundno(), 1);
					status = PayStatus.PAY_SUCCESS;

				} else if (refund_status != null && StringUtil.contains(failStat, refund_status)) {
					status = PayStatus.PAY_FAIL;
					errormsg = "pay failed!";
				} else {
					status = PayStatus.PAY_FAIL;
					errormsg = "unkown error!";
				}
			} else {
				status = PayStatus.PAY_FAIL;
				errormsg = "sign validation failed!";
			}
		}
		json.put("status", status);
		json.put("corderid", refund.getCorderid());
		json.put("money", price);
		json.put("errormsg", errormsg);
		return json.toString();
	}

	@Override
	public int refundNotify(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return 0;
	}

	@Override
	public List<Check> check(HttpServletRequest request, HttpServletResponse response, int payTypeId) throws Exception {


		String trans_time = (String) request.getAttribute("starttime");
	
		String gmt_end_time = (String) request.getAttribute("endtime");
		if (!beforeCheck(request)) {
			return null;
		}

		String companyId = request.getParameter("companyid");

		PaymentBean payment = payTypeService.getPayType(payTypeId);


		String partner = payment.getMerchantid();

	
		String key = payment.getSeckey();


		DownloadBillRequestHandler reqHandler = new DownloadBillRequestHandler(null, null);

		TenpayHttpClient httpClient = new TenpayHttpClient();


		reqHandler.init();
		reqHandler.setKey(key);
		reqHandler.setGateUrl("http://mch.tenpay.com/cgi-bin/mchdown_real_new.cgi");


		String timestamp = Long.toString(DateUtil.formatToDate(gmt_end_time, DateUtil.C_DATE_PATTON_DEFAULT).getTime());
		String sign = MD5Util.MD5Encode("spid=" + partner + "&trans_time=" + trans_time + "&stamp=" + timestamp + "&cft_signtype=0&mchtype=1&key="
				+ key, "UTF-8");
		reqHandler.setParameter("spid", partner);
		reqHandler.setParameter("trans_time", trans_time);
		reqHandler.setParameter("stamp", timestamp);
		reqHandler.setParameter("cft_signtype", "0");
		reqHandler.setParameter("mchtype", "1");
		reqHandler.setParameter("sign", sign);


		httpClient.setReqContent(reqHandler.getRequestURL());


		httpClient.setMethod("POST");
		List<Check> retCheck = new ArrayList<Check>();

		if (httpClient.call()) {
			String resContent = httpClient.getResContent();
			if (!StringUtils.isEmpty(resContent)) {

		
				String resArrStr[] = resContent.split("\\\n");
				for (int i = 1; i < resArrStr.length; i++) {
					String[] columns = resArrStr[i].split(",");
					if (columns.length < 3) {
					
						continue;
					}

			
					String ordernum=columns[2].trim().substring(1);
					OrderFormPay orderFormPay = payService.getOrderFormByOrderNumPay(ordernum);
					if (orderFormPay != null) {
						Check check = new Check();
						check.setCorderid(orderFormPay.getCorderid());
						check.setTransdate(columns[0]);
						check.setOrdernumber(ordernum);
						check.setPrice(columns[5]);
						retCheck.add(check);
					}
				}
			}
		} else {
			
		}
		int loop = 0;
		Iterator<Check> it = retCheck.iterator();
		while (it.hasNext()) {
			Check check = it.next();
			OrderFormPay orderFormPay = payService.getOrderFormByOrderNumPay(check.getOrdernumber());
			if (orderFormPay == null) {
				it.remove();
				continue;
			}
			check.setCorderid(orderFormPay.getCorderid());
			check.setTransdate(orderFormPay.getPaymentdate());
			check.setPrice(orderFormPay.getPrice()+"");
			loop++;
			if (loop % 500 == 0) {
				Thread.sleep(100);
			}
		}
		return retCheck;
	}

	@Override
	public OrderFormUnPay getOrderFormUnPay(HttpServletRequest request,
			HttpServletResponse response) {
		String out_trade_no = "";
		try {
			out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
	
			e.printStackTrace();
		}

		ComplexOrderBean complexOrderBean = payService.queryComplexOrderBeanByOrderNumber(out_trade_no);
		OrderFormUnPay orderUnpay = complexOrderBean.getOrderformunpay();
		return orderUnpay;
	}
}
