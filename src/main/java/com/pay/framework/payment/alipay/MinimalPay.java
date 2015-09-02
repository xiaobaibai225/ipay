package com.pay.framework.payment.alipay;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.springframework.stereotype.Component;

import com.pay.framework.http.HttpRequester;
import com.pay.framework.http.HttpResponse;
import com.pay.framework.http.RequestUtil;
import com.pay.framework.log.LogManager;
import com.pay.framework.payment.BasePay;
import com.pay.framework.payment.IMobilePay;
import com.pay.framework.payment.IPay;
import com.pay.framework.payment.alipay.config.AlipayConfig;
import com.pay.framework.payment.alipay.sign.RSASignature;
import com.pay.framework.payment.alipay.util.AlipayCore;
import com.pay.framework.payment.alipay.util.AlipayNotify;
import com.pay.framework.payment.alipay.util.AlipaySubmit;
import com.pay.framework.payment.alipay.util.StringUtil;
import com.pay.framework.util.DateUtil;
import com.pay.framework.util.MD5Util;
import com.pay.framework.util.MoneyUtil;
import com.pay.framework.util.XmlUtil;
import com.pay.model.Check;
import com.pay.model.OrderFormPay;
import com.pay.model.OrderFormUnPay;
import com.pay.model.PayStatus;
import com.pay.model.PaymentBean;
import com.pay.model.Refund;

@Component("minimalpay")
public class MinimalPay extends BasePay implements IPay, IMobilePay {
	LogManager log = LogManager.getLogger(MinimalPay.class);
	private static final String LOG_KEY = "Framework-MinimalPay";
	@Override
	public String getMobileForwardParameter(HttpServletRequest request,
			HttpServletResponse response, int payTypeId) {
		JSONObject json = new JSONObject();
		json.put("status", "0");
		json.put("errormsg", "system error");
		try {
			OrderFormUnPay orderform = beforePay(request, payTypeId);
			if (null == orderform) {
				return json.toString();
			}
			PaymentBean payment = payTypeService.getPayType(orderform.getPaytype());
			// 把请求参数打包成数组
			Map<String, String> sParaTemp = new HashMap<String, String>();
			sParaTemp.put("service", "mobile.securitypay.pay");
			sParaTemp.put("partner", payment.getMerchantid());
			sParaTemp.put("_input_charset", AlipayConfig.input_charset);
			sParaTemp.put("notify_url", payment.getBgurl());
			sParaTemp.put("out_trade_no", orderform.getOrdernumber());
			sParaTemp.put("subject", StringUtil.isNotBlank(orderform.getProductname()) ? URLEncoder.encode(orderform.getProductname(), "UTF-8") : "");
			sParaTemp.put("payment_type", "1");
			sParaTemp.put("seller_id", payment.getExt());
			sParaTemp.put("total_fee", orderform.getMoney()+"");
			sParaTemp.put("body", StringUtil.isNotBlank(orderform.getProductdesc()) ? URLEncoder.encode(orderform.getProductdesc(), "UTF-8") : "");
			sParaTemp.put("sign_type", AlipayConfig.SIGN_TYPE_RSA);
			// 获取请求参数
			String sbHtml = AlipaySubmit.buildMobileForwardUrl(sParaTemp, payment.getPrikey());
			log.info("minimalpay-getMobileForwardParameter", "info:" + sbHtml);
			json.put("partnerId", payment.getMerchantid());
			json.put("corderid", orderform.getCorderid());
			json.put("info", sbHtml);
			json.put("errormsg", "");
			json.put("status", "1");
		} catch (Exception e) {
			log.error("支付宝手机请求异常", e.getMessage(), e);
		}
		return json.toString();
	}

	@Override
	public int mobileServerNotify(int payTypeId,HttpServletRequest request,
			HttpServletResponse response) {
		log.debug("支付宝手机支付后台通知开始。。。",null);
		try {
			
			 PaymentBean payment =
			 payTypeService.getPayType(
					 payTypeId);

			Map<String, String> map = RequestUtil.getParamaterMap(request);
			String trade_status = map.get("trade_status");
			String out_trade_no = map.get("out_trade_no");
			String trade_no = map.get("trade_no");
			String fee=MoneyUtil.getPayMoney(map.get("total_fee"));
			log.debug("获得支付宝手机通知的的交易结果信息",trade_status+":"+out_trade_no+":"+fee);
			if (trade_status.equals("TRADE_FINISHED") || trade_status.equals("TRADE_SUCCESS")) {
				String busUrl=payment.getPageurl()+"&paytype="+payTypeId+"&orderId="+out_trade_no+"&amount="+fee;
				log.debug("手机支付宝业务处理busUrl is", busUrl);
				HttpRequester httpRequester = new HttpRequester();
				HttpResponse httpResponse = null;
				try {
					httpResponse = httpRequester.sendGet(busUrl.replaceAll("\\s", ""));
				} catch (IOException e) {
					log.debug(LOG_KEY, "手机支付宝通知处理业务 错误 url:" + busUrl + ",corderid=" + out_trade_no
							+ ", errormsg:" + e.getMessage());
					e.printStackTrace();
					return 0;
				}
				String content = httpResponse.getContent();
				if (content != null && content.contains("success") || content.contains("true")) {
					log.debug(LOG_KEY, "手机支付宝通知处理业务 成功 url:" + busUrl + ",corderid=" + out_trade_no );
				} else {
					log.debug(LOG_KEY, "手机支付宝通知处理业务 失败 url:" + busUrl + ",corderid=" +out_trade_no);
					return 0;
				}
			}
			OrderFormUnPay orderFormUnpay = orderFormService.queryOrderFormUnPaymentByOrderNumber(out_trade_no);
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

			// 获得通知签名
			String sign = map.get("sign");
			boolean verified = true;
			String verifyData = AlipayCore.createLinkString(AlipayCore.paraFilter(map));
			String pubKey = payment.getPubkey() ;
			log.debug("支付宝手机验证支付签名信息", "sign-->"+sign+"-->verifydata-->"+verifyData+"-->pubKey-->"+pubKey);
			verified = RSASignature.doCheck(verifyData, sign, pubKey);
			log.debug("支付宝手机验证结果", verified+"");
			if (verified) {
				
				// 判断金额
				if ((new java.math.BigDecimal(fee)
						.multiply(new java.math.BigDecimal(100)).intValue()) < (new java.math.BigDecimal(
						orderFormUnpay.getPrice()).intValue())) {
					try {
						PrintWriter out = response.getWriter();
						out.println("error");
						out.flush();
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					return PayStatus.PAY_NOT_MATCH;
				}
				
				// 服务端通知
				this.afterServerNotify(orderFormUnpay, trade_no);
				try {
					PrintWriter out = response.getWriter();
					out.println("success");
					out.flush();
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				log.debug("支付宝手机业务处理结果正常######################", null);
				return PayStatus.PAY_SUCCESS;
			} else {
				return PayStatus.PAY_FAIL;
			}
		} catch (Exception e) {
			log.debug("手机支付宝通知异常", e.getMessage());
		}
		return PayStatus.PAY_FAIL;
	}

	@Override
	public String getWapForwardParameter(HttpServletRequest request,
			HttpServletResponse response, int payTypeId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPhoneMessageForwardParameter(HttpServletRequest request,
			HttpServletResponse response, int payTypeId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int wapServerNotify(HttpServletRequest request,
			HttpServletResponse response) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getIapResult(HttpServletRequest request,
			HttpServletResponse response, int payTypeId)
			throws MalformedURLException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getForwardUrl(HttpServletRequest request,
			HttpServletResponse response, int payTypeId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int doPaymentServerNotify(HttpServletRequest request,
			HttpServletResponse response) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int doPaymentPageNotify(HttpServletRequest request,
			HttpServletResponse response) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String refund(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Refund refund = super.beforeRefund(request);
		log.debug("保存退款信息成功", null);
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
		log.debug("开始组装退款报文", null);
		try {
			sHtmlText = AlipayService.refund_fastpay_by_platform_pwd(payment.getPosturl(),sParaTemp, payment.getMerchantid(), payment.getSeckey());
			log.debug("请求退款的posturl为", sHtmlText);
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
					//TODO  此处加上退款成功后的业务处理逻辑
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
	public int refundNotify(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
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
							log.debug("ALIPAY", "手机支付宝退款通知处理业务 server error url:" + url + ",corderid=" + orderFormPay.getCorderid()
									+ ",orderid=" + orderFormPay.getOrdernumber() + ", errormsg:" + e.getMessage());
							e.printStackTrace();
							return PayStatus.PAY_FAIL;
						}
						String content = httpResponse.getContent();
						if (content != null && content.equalsIgnoreCase("success")) {
							log.debug("alipay refund,", "手机支付宝退款通知处理业务 成功 url:" + url + ",corderid=" + orderFormPay.getCorderid() );
							orderFormService.updateRefund(refund.getOuterfundno(), 3);//3的状态时退款成功，业务处理也成功
						}
						else
						{
							log.debug("alipay refund,", "手机支付宝退款通知处理业务 失败 url:" + url + ",corderid=" +orderFormPay.getCorderid());
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
	public List<Check> check(HttpServletRequest request,
			HttpServletResponse response, int payTypeId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OrderFormUnPay getOrderFormUnPay(HttpServletRequest request,
			HttpServletResponse response) {
		// TODO Auto-generated method stub
		return null;
	}

}
