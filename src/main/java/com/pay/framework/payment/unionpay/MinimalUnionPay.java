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

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.pay.framework.http.HttpRequester;
import com.pay.framework.http.HttpResponse;
import com.pay.framework.log.LogManager;
import com.pay.framework.payment.BasePay;
import com.pay.framework.payment.IMobilePay;
import com.pay.framework.payment.IPay;
import com.pay.framework.util.DateUtil;
import com.pay.framework.util.XmlUtil;
import com.pay.model.Check;
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

@Component("minimalunionpay")
public class MinimalUnionPay extends BasePay implements IPay, IMobilePay {
	LogManager log = LogManager.getLogger(MinimalUnionPay.class);
	
	public static final String C_TIME_PATTON_DEFAULT = "yyyyMMddHHmmss";
	public static final DateFormat df = new SimpleDateFormat(
			C_TIME_PATTON_DEFAULT);
	private static final String LOG_KEY = "Framework-MinimalUnionPay";
	@Override
	public synchronized String getMobileForwardParameter(HttpServletRequest request,
			HttpServletResponse response, int payTypeId) {
		JSONObject json = new JSONObject();
		json.put("status", "0");
		json.put("errormsg", "system error");
		try {
			PaymentBean payment = payTypeService.getPayType(payTypeId);
			SDKConfig.getConfig().loadPropertiesFromPath(payment.getPubkey());
			CertUtil.init();
			String postUrl=payment.getPosturl();
			String encoding = "UTF-8";
			// 把请求参数打包成数组
			/**
			 * 组装请求报文
			 */
			Map<String, String> data = new HashMap<String, String>();
			// 版本号
			data.put("version", "5.0.0");
			data.put("encoding", encoding);
			// 签名方法 01 RSA
			data.put("signMethod", "01");
			// 交易类型 01-消费
			data.put("txnType", "01");
			// 交易子类型 01:自助消费 02:订购 03:分期付款
			data.put("txnSubType", "01");
			// 业务类型 000201 B2C业务  000202 B2B网关支付
			data.put("bizType", payment.getMerchantpwd());
			// 渠道类型 07-互联网渠道
			data.put("channelType", "07");
			// 商户/收单前台接收地址 选送
			//data.put("frontUrl", "http://localhost:8080/ACPTest/acp_front_url.do");
			// 商户/收单后台接收地址 必送
			data.put("backUrl",  payment.getBgurl());
			// 接入类型:商户接入填0 0- 商户 ， 1： 收单， 2：平台商户
			data.put("accessType", "0");
			// 商户号码
			data.put("merId",payment.getMerchantid());
			// 订单号 商户根据自己规则定义生成，每订单日期内不重复
			data.put("orderId", request.getParameter("orderId"));
			// 订单发送时间 格式： YYYYMMDDhhmmss 商户发送交易时间，根据自己系统或平台生成
			data.put("txnTime", df.format(new Date()));
			// 交易金额 分
			data.put("txnAmt", request.getParameter("price"));
			// 交易币种
			data.put("currencyCode", "156");
			// 持卡人ip 根据需求选送 参考接口规范 防钓鱼用
			data.put("customerIp", request.getRemoteAddr());

			Map<String, String> resmap = UnionpayUtil.submitDate(data,postUrl);
			String tn=resmap.get("tn");
			if (!StringUtils.isEmpty(tn)) {
				json.put("status", "1");
				json.put("tn", tn);
			} else {
				json.put("errormsg", "无法获取交易流水号");
				json.put("tn", "");
			}

		} catch (Exception e) {
			log.debug("获取银联交易号异常", e.getMessage());
		}
		return json.toString();
	}

	@Override
	public synchronized int mobileServerNotify(int payTypeId,HttpServletRequest req,
			HttpServletResponse response) {
		log.debug("手机银联接收后台通知开始",null);

		try {
			req.setCharacterEncoding("ISO-8859-1");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String encoding = req.getParameter("encoding");
		 PaymentBean payment =
		 payTypeService.getPayType(
				 payTypeId);
		Map reqParam = UnionpayUtil.getAllRequestParam(req);
		String trade_status = (String) reqParam.get("respCode");
		String order_no = (String) reqParam.get("orderId");
		String total_fee = (String) reqParam.get("txnAmt");
		log.debug("get the mobile status,order_no,total_fee",trade_status+":"+order_no+""+total_fee);
		if ("00".equals(trade_status)) {
			//因为银联手机发起支付时，没有调用支付平台，故需调下业务系统，往unpay里面补录订单信息
			String busUrl=payment.getPageurl()+"&paytype="+payTypeId+"&orderId="+order_no+"&amount="+total_fee;
			log.debug("手机银联业务处理busUrl is", busUrl);
			// 调用业务接口
			HttpRequester httpRequester = new HttpRequester();
			HttpResponse httpResponse = null;
			try {
				httpResponse = httpRequester.sendGet(busUrl.replaceAll("\\s", ""));
			} catch (IOException e) {
				log.debug(LOG_KEY, "手机银联通调用处理业务****** 失败 url:" + busUrl + ",corderid=" + order_no
						+ ", errormsg:" + e.getMessage());
				e.printStackTrace();
				return 0;
			}
			String content = httpResponse.getContent();
			if (content != null && content.contains("success") || content.contains("true")) {
				log.debug(LOG_KEY, "手机银联通知处理业务****** 成功 url:" + busUrl + ",corderid=" + order_no );
			} else {
				log.debug(LOG_KEY, "手机银联通知处理业务****** 失败 url:" + busUrl + ",corderid=" +order_no);
				return 0;
			}
		}

		
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
		SDKConfig.getConfig().loadPropertiesFromPath(payment.getPubkey());
		CertUtil.init();
		if (!SDKUtil.validate(valideData, encoding)) {
			log.debug("手机银联后台通知验证签名结果[失败].",null);
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
			log.debug("手机银联后台通知验证签名结果[成功]. trade_status:",trade_status);

		
			if ("00".equals(trade_status)) {

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
			log.debug("银联手机业务处理结果正常######################", null);
			return PayStatus.PAY_SUCCESS;
		}
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
	public  synchronized  String refund(HttpServletRequest req,
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

	    log.debug("请求ＵＲＬ=" , (String)data.get("backUrl"));

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
	    	  log.debug("手机银联退款验证签名成功",null);
	    	  String trade_status = (String) resData.get("respCode");
	    	  if ("00".equals(trade_status)) {
				afterRefund(refund.getOrdernumber());
				orderFormService.updatePayStatus(refund.getOrdernumber(), PayStatus.PAY_SUCCESS_REFUND);
				status = PayStatus.PAY_SUCCESS;
				//TODO  此处加上退款成功后的业务处理逻辑
	    	  }
	      }
	      else {
	    	  log.debug("手机银联退款验证签名失败",null);
				status = PayStatus.PAY_FAIL;
				errormsg = "手机银联退款银联验证签名失败";
	      }

	      log.debug("手机银联退款打印返回报文：" , result);
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
	public List<Check> check(HttpServletRequest request,
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
			log.debug("拿到手机银联对账文件参数为",resmap.toString());
		    File file=UnionpayUtil.deCodeFileContent(resmap, payment.getExt());
		    log.debug("下载手机对账文件成功",resmap.toString());
			List<Check> retCheck = new ArrayList<Check>();
		    if(null!=file)
		    {
		    	String year=file.getName().substring(16,20);
		    	log.debug("需要手机对账的年份为",year);
		    	File toReadFile=UnionpayUtil.deCompressFile(file);
		    	if(null!=toReadFile)
		    	{
		    		log.debug("解压手机对账文件成功",toReadFile.getName());
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
		    		log.debug("获取手机对账明细结束",null);
		    	}
		    }
			return retCheck;
	}

	@Override
	public OrderFormUnPay getOrderFormUnPay(HttpServletRequest request,
			HttpServletResponse response) {
		// TODO Auto-generated method stub
		return null;
	}

}
