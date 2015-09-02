package com.pay.framework.payment.unionpay;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pay.framework.log.LogManager;
import com.pay.framework.payment.BasePay;
import com.pay.framework.payment.IMobilePay;
import com.pay.framework.payment.IPay;
import com.pay.framework.service.OrderFormService;
import com.pay.framework.service.PayService;
import com.pay.model.Check;
import com.pay.model.ComplexOrderBean;
import com.pay.model.OrderFormPay;
import com.pay.model.OrderFormUnPay;
import com.pay.model.PayStatus;
import com.pay.model.PaymentBean;
import com.pay.model.Refund;
import com.unionpay.acp.sdk.CertUtil;
import com.unionpay.acp.sdk.HttpClient;
import com.unionpay.acp.sdk.LogUtil;
import com.unionpay.acp.sdk.SDKConfig;
import com.unionpay.acp.sdk.SDKUtil;

/**
 * 银联H5支付
 * 
 * @author scf
 * 
 */
@Component("unionh5pay")
public class UnionH5Pay extends BasePay implements IPay ,IMobilePay {

	LogManager logManager = LogManager.getLogger(UnionH5Pay.class);

	@Autowired
	private OrderFormService orderFormService;

	@Autowired
	private PayService payService;

	public static final String C_TIME_PATTON_DEFAULT = "yyyyMMddHHmmss";

	public static final DateFormat df = new SimpleDateFormat(
			C_TIME_PATTON_DEFAULT);

	@Override
	public synchronized String getForwardUrl(HttpServletRequest request,
			HttpServletResponse response, int payTypeId) {
		return null;
		
	}

	@Override
	public synchronized int doPaymentServerNotify(HttpServletRequest req,
			HttpServletResponse response) {
		logManager.debug("BackRcvResponse接收后台通知开始",null);

		try {
			req.setCharacterEncoding("ISO-8859-1");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String encoding = req.getParameter("encoding");

		Map reqParam = UnionpayUtil.getAllRequestParam(req);

		String order_no = (String) reqParam.get("orderId");
		OrderFormUnPay orderFormUnpay = orderFormService
				.queryOrderFormUnPaymentByOrderNumber(order_no);
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

		LogUtil.printRequestLog(reqParam);

		Map valideData = null;
		if ((null != reqParam) && (!reqParam.isEmpty())) {
			Iterator it = reqParam.entrySet().iterator();
			valideData = new HashMap(reqParam.size());
			while (it.hasNext()) {
				Map.Entry e = (Map.Entry) it.next();
				String key = (String) e.getKey();
				String value = (String) e.getValue();
				try {
					value = new String(value.getBytes("ISO-8859-1"), encoding);
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}
				valideData.put(key, value);
			}

		}
		PaymentBean payment = payTypeService.getPayType(orderFormUnpay.getPaytype());
		SDKConfig.getConfig().loadPropertiesFromPath(payment.getPubkey());
		CertUtil.init();
		if (!SDKUtil.validate(valideData, encoding)) {
			logManager.debug("后台通知验证签名结果[失败].",null);
			try {
				PrintWriter out = response.getWriter();
				out.println("error");
			out.flush();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return PayStatus.PAY_VALIDATE_FAIL;
		} else {
			logManager.debug("后台通知验证签名结果[成功].",null);

			String trade_status = (String) reqParam.get("respCode");
			if ("00".equals(trade_status)) {
				String total_fee = (String) reqParam.get("txnAmt");

				// 判断金额
				if ((new java.math.BigDecimal(total_fee)
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
				super.afterServerNotify(orderFormUnpay,
						(String)reqParam.get("queryId"));
			}
			try {
				PrintWriter out = response.getWriter();
				out.println("success");
				out.flush();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			logManager.debug("BackRcvResponse接收后台通知结束",null);
			return PayStatus.PAY_SUCCESS;
		}
	}

	@Override
	public synchronized int doPaymentPageNotify(HttpServletRequest req,
			HttpServletResponse response) {
		logManager.debug("FrontRcvResponse前台接收报文返回开始",null);

		try {
			req.setCharacterEncoding("ISO-8859-1");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		String encoding = req.getParameter("encoding");
		logManager.debug("返回报文中encoding=[" + encoding + "]",null);

		Map respParam = UnionpayUtil.getAllRequestParam(req);

		String orderNum = (String) respParam.get("orderId");
		ComplexOrderBean complexOrderBean = payService
				.queryComplexOrderBeanByOrderNumber(orderNum);
		OrderFormPay orderForm = complexOrderBean.getOrderform();
		if (null == orderForm) {
			return PayStatus.PAY_NO_ORDER;
		}

		LogUtil.printRequestLog(respParam);

		Map valideData = null;
		if ((null != respParam) && (!respParam.isEmpty())) {
			Iterator it = respParam.entrySet().iterator();
			valideData = new HashMap(respParam.size());
			while (it.hasNext()) {
				Map.Entry e = (Map.Entry) it.next();
				String key = (String) e.getKey();
				String value = (String) e.getValue();
				valideData.put(key, value);
			}
		}
		PaymentBean payment = payTypeService.getPayType(orderForm.getPaytype());
		SDKConfig.getConfig().loadPropertiesFromPath(payment.getPubkey());
		CertUtil.init();
		if (!SDKUtil.validate(valideData, encoding)) {
			logManager.debug("前台接收验证签名结果[失败].",null);
			return PayStatus.PAY_VALIDATE_FAIL;
		} else {
			logManager.debug("前台接收验证签名结果[成功].",null);
			String trade_status = (String) respParam.get("respCode");
			if ("00".equals(trade_status)) {
				if (afterPageNotify(orderForm, response)) {
					return PayStatus.PAY_SUCCESS;
				}
			}
		}
		logManager.debug("FrontRcvResponse前台接收报文返回结束",null);
		return PayStatus.PAY_FAIL; // 请不要修改或删除
	}

	@Override
	public synchronized String refund(HttpServletRequest req,
			HttpServletResponse response) throws Exception {
		Refund refund = super.beforeRefund(req);
		String result = "";
		 String encoding = "UTF-8";

		if (refund == null) {
			JSONObject json = new JSONObject();
			json.put("status", "0");
			json.put("errormsg", "already refund all");
			return json.toString();
		}
		PaymentBean payment = payTypeService.getPayType(refund.getPaytype());
		SDKConfig.getConfig().loadPropertiesFromPath(payment.getPubkey());
		CertUtil.init();
	    String requestUrl = payment.getExtCol1();
		Map data = new HashMap();

	    data.put("version", "5.0.0");

	    data.put("encoding", encoding);

	    data.put("signMethod", "01");

	    data.put("txnType", "04");

	    data.put("txnSubType", "00");

	    data.put("bizType", "000000");

	    data.put("channelType", "08");

	    data.put("backUrl", "/refundnotify/"+refund.getPaytype());

	    data.put("accessType", "0");

	    data.put("merId", payment.getMerchantid());

	    data.put("orderId", refund.getOuterfundno());// --商户订单号

	    data.put("origQryId", refund.getTranseq());

	    data.put("txnTime", df.format(new Date()));// --订单发送时间

	    data.put("txnAmt", refund.getRefundmoney()+"");

	    logManager.debug("请求ＵＲＬ=" , (String)data.get("backUrl"));

	    Map request = new HashMap();
	    request.putAll(data);
	    Set set = data.keySet();
	    Iterator iterator = set.iterator();
	    while (iterator.hasNext()) {
	      String key = (String)iterator.next();
	      if ((data.get(key) == null) || (data.get(key) == "")) {
	        request.remove(key);
	      }

	    }
		JSONObject json = new JSONObject();
		int status = 0;
		long price = refund.getRefundmoney();
		String errormsg = "";
	    SDKUtil.sign(request, encoding);

	    HttpClient hc = new HttpClient(requestUrl, 30000, 30000);
	    try {
	      int ss = hc.send(request, encoding);
	      if (200 == ss)
	        result = hc.getResult();
	    }
	    catch (Exception e) {
	      e.printStackTrace();
			status = PayStatus.PAY_FAIL;
			errormsg = e.getMessage();
	    }

	    if ((null != result) && (!"".equals(result)))
	    {
	      Map resData = SDKUtil.convertResultStringToMap(result);

	      if (SDKUtil.validate(resData, encoding))
	      {
	    	  logManager.debug("验证签名成功",null);
	    	  String trade_status = (String) resData.get("respCode");
	    	  if ("00".equals(trade_status)) {
				afterRefund(refund.getOrdernumber());
				orderFormService.updatePayStatus(refund.getOrdernumber(), PayStatus.PAY_SUCCESS_REFUND);
				status = PayStatus.PAY_SUCCESS;
				//TODO  此处加上退款成功后的业务处理逻辑
	    	  }
	      }
	      else {
	    	  logManager.debug("验证签名失败",null);
				status = PayStatus.PAY_FAIL;
				errormsg = "银联验证签名失败";
	      }

	      logManager.debug("打印返回报文：" , result);
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
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public synchronized List<Check> check(HttpServletRequest request,
			HttpServletResponse response, int payTypeId) throws Exception {
		PaymentBean payment = payTypeService.getPayType(payTypeId);
		SDKConfig.getConfig().loadPropertiesFromPath(payment.getPubkey());
		CertUtil.init();
		  String requestfileUrl = payment.getQuerykey();
			// 要查询的开始时间
		  String gmt_start_time = (String) request.getAttribute("starttime");
	    	String  settleDate= gmt_start_time.substring(4).replace("-", "");//清算日期
		  Map<String, Object> contentData = new HashMap<String, Object>();

			// 固定填写
			contentData.put("version", "5.0.0");// M
			// 默认取值：UTF-8
			contentData.put("encoding", "UTF-8");// M

			// 01RSA02 MD5 (暂不支持)
			contentData.put("signMethod", "01");// M

			// 取值:76
			contentData.put("txnType", "76");// M

			// 01：对账文件下载
			contentData.put("txnSubType", "01");// M

			// 默认:000000
			contentData.put("bizType", "000000");// M

			// 0：普通商户直连接入1. 收单机构接入
			contentData.put("accessType", "0");// M

			// 　商户类型为商户接入时必须上送
			contentData.put("merId", payment.getMerchantid());// C

			contentData.put("settleDate", settleDate);// M
			
			contentData.put("txnTime", df.format(new Date()));// M

			// 依据实际业务情况定义参考附录：商户索取的文件类型约定
			contentData.put("fileType", "00");// M
			Map<String, String> resmap = UnionpayUtil.submitDate(contentData,requestfileUrl);
			logManager.debug("拿到银联对账文件参数为",resmap.toString());
		    File file=UnionpayUtil.deCodeFileContent(resmap, payment.getExt());
			logManager.debug("下载对账文件成功",resmap.toString());
			List<Check> retCheck = new ArrayList<Check>();
		    if(null!=file)
		    {
		    	String year=file.getName().substring(16,20);
		    	logManager.debug("需要对账的年份为",year);
		    	File toReadFile=UnionpayUtil.deCompressFile(file);
		    	if(null!=toReadFile)
		    	{
		    		logManager.debug("解压对账文件成功",toReadFile.getName());
		    		retCheck=UnionpayUtil.readBillFile(toReadFile,year);
					Iterator<Check> it = retCheck.iterator();
					while (it.hasNext()) {

						Check check = it.next();
						OrderFormPay orderFormPay = payService.getOrderFormByOrderNumPay(check.getOrdernumber());
						if (orderFormPay != null) {
							//it.remove();
							//continue;
							if(check.getPrice().contains("-")) check.setCorderid(orderFormPay.getMemo());//如果是退款金额，则取退款的应收单
							else 	check.setCorderid(orderFormPay.getCorderid());//如果取得数据为付款金额，则取原账单
						}
						check.setPaytype(payTypeId+"");
					}
		    		logManager.debug("获取对账明细结束",null);
		    	}
		    }
			return retCheck;
	}


	@Override
	public OrderFormUnPay getOrderFormUnPay(HttpServletRequest request,
			HttpServletResponse response) {
		String out_trade_no = "";
		try {
			out_trade_no = new String(request.getParameter("orderId").getBytes(
					"ISO-8859-1"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ComplexOrderBean complexOrderBean = payService
				.queryComplexOrderBeanByOrderNumber(out_trade_no);
		OrderFormUnPay orderUnpay = complexOrderBean.getOrderformunpay();
		return orderUnpay;
	}

	@Override
	public String getMobileForwardParameter(HttpServletRequest request,
			HttpServletResponse response, int payTypeId) {
		logManager.debug("银联wap", "银联wap支付开始----------------");
		OrderFormUnPay orderform = beforePay(request, payTypeId);
		if (null == orderform) {
			return null;
		}
		PaymentBean payment = payTypeService.getPayType(orderform.getPaytype());
		SDKConfig.getConfig().loadPropertiesFromPath(payment.getPubkey());
		CertUtil.init();
		String postUrl=payment.getPosturl();
		String encoding = "UTF-8";
		// 把请求参数打包成数组
		Map<String, String> sParaTemp = new HashMap<String, String>();
		sParaTemp.put("version", "5.0.0");	// 版本号
		sParaTemp.put("encoding", encoding);// 字符集编码 默认"UTF-8"
		sParaTemp.put("signMethod", "01");// RSA
		sParaTemp.put("txnType", "01");	// 交易类型 01-消费
		sParaTemp.put("txnSubType", "01");		// 交易子类型 01:自助消费 02:订购 03:分期付款
		sParaTemp.put("bizType", payment.getMerchantpwd());		// 业务类型 000201 B2C网关支付 000202 B2B网关支付
		sParaTemp.put("channelType", "07");		// 渠道类型 07-互联网渠道
		sParaTemp.put("frontUrl", payment.getPageurl());		// 商户/收单前台接收地址 选送
		sParaTemp.put("backUrl", payment.getBgurl());		// 商户/收单后台接收地址 必送
		sParaTemp.put("accessType", "0");		// 接入类型:商户接入填0 0- 商户 ， 1： 收单， 2：平台商户
		sParaTemp.put("merId", payment.getMerchantid());		// 商户号码
		sParaTemp.put("orderId", orderform.getOrdernumber());		// 订单号 商户根据自己规则定义生成，每订单日期内不重复
		sParaTemp.put("txnTime", df.format(new Date()));		// 订单发送时间 格式： YYYYMMDDhhmmss 商户发送交易时间，根据自己系统或平台生成
		sParaTemp.put("txnAmt", orderform.getPrice() + "");		// 交易金额 分
		sParaTemp.put("currencyCode", "156");		// 交易币种
		sParaTemp.put("customerIp", request.getRemoteAddr());	// 持卡人ip 根据需求选送 参考接口规范 防钓鱼用
		logManager.debug("银联wap", "支付请求参数:"+sParaTemp.toString());
		//String sHtmlText = UnionpaySubmit.buildForwardUrl(postUrl,UnionpayUtil.signData(sParaTemp, encoding));
		String sHtmlText = UnionpaySubmit.createHtml(postUrl,UnionpayUtil.signData(sParaTemp, encoding));

	//	response.setHeader("Cache-Control", "no-cache, must-revalidate");
	//	response.setHeader("Pragma", "no-cache");
	//	response.setContentType("text/html;charset=utf-8");
//		try {
		//	response.getWriter().write(sHtmlText);
		//	response.setContentType("text/html");
		//	PrintWriter out;
		//	out = response.getWriter();
		//	out.println(sHtmlText);
		//	out.flush();
		//	out.close();
	//	} catch (IOException e) {
			// TODO Auto-generated catch block
	//		e.printStackTrace();
	//	}
		return sHtmlText;
	
	}

	@Override
	public int mobileServerNotify(int payTypeId, HttpServletRequest request,
			HttpServletResponse response) {
		// TODO Auto-generated method stub
		return 0;
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

}
