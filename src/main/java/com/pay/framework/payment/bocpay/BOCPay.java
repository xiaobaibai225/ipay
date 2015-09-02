package com.pay.framework.payment.bocpay;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pay.framework.http.HttpRequester;
import com.pay.framework.http.HttpResponse;
import com.pay.framework.log.LogFlags;
import com.pay.framework.log.LogManager;
import com.pay.framework.payment.BasePay;
import com.pay.framework.payment.IPay;
import com.pay.framework.payment.bocpay.util.PKCSTool;
import com.pay.framework.payment.ccbpay.CCBPaySubmit;
import com.pay.framework.payment.ccbpay.sign.RSASig;
import com.pay.framework.payment.unionpay.UnionpayUtil;
import com.pay.framework.service.OrderFormService;
import com.pay.framework.service.PayService;
import com.pay.model.Check;
import com.pay.model.ComplexOrderBean;
import com.pay.model.OrderFormPay;
import com.pay.model.OrderFormUnPay;
import com.pay.model.PayStatus;
import com.pay.model.PaymentBean;
import com.pay.model.Refund;

/**
 * 中行支付
 */
@Component("bocpay")
public class BOCPay extends BasePay implements IPay {

	static LogManager logger = LogManager.getLogger(BOCPay.class);

	@Autowired
	private OrderFormService orderFormService;

	@Autowired
	private PayService payService;
	public static final String yyyyMMddHHmmss = "yyyyMMddHHmmss";

	public String getForwardUrl(HttpServletRequest request,
			HttpServletResponse response, int payTypeId) {
		logger.debug("进入中行支付", "进入中行支付。。。。。。payTypeId："+payTypeId);
		// 处理支付通用逻辑
		OrderFormUnPay orderform = BocBeforePay(request, payTypeId);
		if (null == orderform) {
			return null;
		}
		PaymentBean payment = payTypeService.getPayType(orderform.getPaytype());
		// 把请求参数打包成数组
		Map<String, String> m = new HashMap<String, String>();
		m.put("merchantNo", payment.getMerchantid());
		m.put("payType", "1");
		m.put("orderNo", orderform.getOrdernumber());
		m.put("curCode", "001");
		m.put("orderAmount", getMoneyStr(orderform.getPrice()/100.0));
		logger.debug("orderaccount:", "orderaccount:"+getMoneyStr(orderform.getPrice()/100.00));
		m.put("orderTime", getTimeStr(new Date(), yyyyMMddHHmmss));
		m.put("orderNote", orderform.getProductdesc());
		m.put("orderUrl", payment.getBgurl());
		try {
			if (orderform.getExpiredtime() != null) {
				m.put("orderTimeoutDate",getTimeStr(orderform.getExpiredtime(), yyyyMMddHHmmss));
			}
			// String keyStorePath: 证书库路径
			// String keyStorePassword: 证书库口令
			// String keyPassword: 签名私钥口令，一般与证书库口令相同
			PKCSTool tool = PKCSTool.getSigner(payment.getPubkey(),payment.getPrikey(), payment.getMerchantpwd(), "PKCS7");
			
			//PKCS7Tool tool = PKCS7Tool.getSigner("", "", "");
			// 签名，返回signature：base64格式的签名结果
			// byte[] data: 明文字符串

			// "orderNo|orderTime|curCode|orderAmount|merchantNo"
			String data = m.get("orderNo") + "|" + m.get("orderTime") + "|"
					+ m.get("curCode") + "|" + m.get("orderAmount") + "|"
					+ m.get("merchantNo");
			logger.debug("中行支付", "中行支付签名源数据+"+data);
			String signature = tool.p7Sign(data.getBytes("UTF-8"));
			logger.debug("中行支付", "中行支付签名数据+"+signature);
			m.put("signData", signature);
			logger.debug("中行跳转到支付页面请求参数", "中行跳转到支付页面请求参数："+m);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 建立请求
		//String sHtmlText = BOCPaySubmit.buildForwardUrl(m, payment.getPosturl());
		logger.debug("中行支付请求页面URL", "中行支付请求页面URL:"+payment.getPosturl());
		String sHtmlText = BOCPaySubmit.createHtml(payment.getPosturl(), m);
		logger.debug("中行建立请求", "中行建立请求:"+sHtmlText);
		return sHtmlText;
	}
	public int doPaymentServerNotify(HttpServletRequest request,
			HttpServletResponse response) {
		logger.debug("doPaymentServerNotify:", "进入主动通知......");
		try {
			Map reqParam = BOCPayUtil.getAllRequestParam(request);
			logger.debug("中行支付", "中行支付主动通知返回参数:"+reqParam.toString());
			String orderNo = (String) reqParam.get("orderNo");
			logger.debug("中行支付", "订单号："+orderNo);
			//OrderFormPay orderform = payService.getOrderFormByOrderNumPay(orderNo);
			
			OrderFormUnPay orderFormUnpay = orderFormService.queryOrderFormUnPaymentByOrderNumber(orderNo);
			
			if (orderFormUnpay == null) {
				try {
					OrderFormPay orderForm=orderFormService.getOrderByOrderNum(orderNo);
					String url="www.soho3q.com";
					if(null!=orderForm)
						url=orderForm.getFronturl()+"&delayFlag=N";
						PrintWriter out = response.getWriter();
						response.setHeader("Cache-Control", "no-cache");
						//out.println("<URL>"+url+"</URL>");
						out.println("<HTML>");
						out.println("<HEAD>");
						out.println("<meta http-equiv=\"refresh\" content=\"0; url='"+url+"'\">");
						out.println("</HEAD>");
						out.println("</HTML>");
						out.flush();
						out.close();
				} catch (IOException e) {
					logger.debug("中行在没有unpay的情况下跳转前台地址失败",null);
				}
				logger.debug("info","支付后台-----中行支付接收后台通知结束......,状态：【"+PayStatus.PAY_NO_ORDER+"】");
				return PayStatus.PAY_NO_ORDER;
			}
			
			PaymentBean payment = payTypeService.getPayType(orderFormUnpay.getPaytype());
			// Map<String, String> parameterMap = RequestUtil
			// .getParamaterMap(request);
			// merchantNo|orderNo|orderSeq|cardTyp|payTime|orderStatus|payAmount
			// String rootCertificatePath: 根证书路径
			logger.debug("验证根证书:", "验证根证书开始......");
			
			
			InputStream rootCertStream = new FileInputStream(payment.getSeckey());
			InputStream verifyCertStream = null;
			
			PKCSTool tool = PKCSTool.getVerifier(rootCertStream,verifyCertStream);
			// 验签名，如果失败抛出异常
			// String signature：签名
			// byte[] data：明文数据
			// String dn：银行签名证书DN，如果为空则不验证DN
			String signature = (String) reqParam.get("signData");
			logger.debug("中行支付","中行返回的签名");
			String merchantNo = (String) reqParam.get("merchantNo");
			String orderSeq = (String) reqParam.get("orderSeq");
			String cardTyp = (String) reqParam.get("cardTyp");
			String payTime = (String) reqParam.get("payTime");
			String orderStatus = (String) reqParam.get("orderStatus");
			String payAmount = (String) reqParam.get("payAmount");
			String data =  merchantNo + "|" +orderNo+"|"+ orderSeq + "|"
					+ cardTyp + "|" + payTime + "|" + orderStatus + "|"
					+ payAmount;
			logger.debug("中行支付", "验签明文："+data);
			
			//tool.p1Verify(signature, data.getBytes("UTF-8"));
			tool.p7Verify(signature, data.getBytes("UTF-8"));
			logger.debug("验证根证书:", "验证根证书结束......");
			
			// 判断是否扣款成功
			if (orderStatus == null || "".equals(orderStatus.trim())) {
				return PayStatus.PAY_FAIL;
			} else {
				orderStatus = orderStatus.trim();
			}
			if (!"1".equals(orderStatus)) {// 状态不为1，返回失败。状态为1 将 orderform_unpay 赋值到 orderform。
				return PayStatus.PAY_FAIL;
			}
			logger.debug("中行支付", "中行扣款成功!");
			
			// 判断订单号是否为空
			if (orderNo == null || "".equals(orderNo.trim())) {
				return PayStatus.PAY_NO_ORDER;
			} else {
				orderNo = orderNo.trim();
			}
			// 判断金额是否为空
			String orderAmount = (String) reqParam.get("payAmount");
			if (orderAmount == null || "".equals(orderAmount.trim())) {
				return PayStatus.PAY_FAIL;
			} else {
				orderAmount = orderAmount.trim();
			}
			
			double money = 0.0;
			//money = getMoneyByStr(orderAmount) * 100;
			BigDecimal transactionAmount= new BigDecimal(orderAmount);
			logger.debug("中行支付", "中行支付返回的金额：transactionAmount:"+transactionAmount);
			if(transactionAmount.multiply(new BigDecimal(100)).compareTo(new BigDecimal(orderFormUnpay.getPrice()))<0){
				try {
					PrintWriter out = response.getWriter();
					out.println("fail");
					out.flush();
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return PayStatus.PAY_NOT_MATCH;
			}else{
				logger.debug("中行支付", "更改支付表开始......");
				// 更新支付订单表
				super.afterServerNotify(orderFormUnpay,(String)reqParam.get("orderSeq"));
				logger.debug("中行支付", "更改成功");
				//return PayStatus.PAY_SUCCESS;
			}
			try {
				String url=orderFormUnpay.getFronturl()+"&delayFlag=N";;
				PrintWriter out = response.getWriter();
				response.setHeader("Cache-Control", "no-cache");
				//out.println("<URL>"+url+"</URL>");
				out.println("<HTML>");
				out.println("<HEAD>");
				out.println("<meta http-equiv=\"refresh\" content=\"0; url='"+url+"'\">");
				out.println("</HEAD>");
				out.println("</HTML>");
				out.flush();
				out.close();
			} catch (IOException e) {
				logger.debug("中行支付","中行跳转失败！");
			}
			return PayStatus.PAY_SUCCESS;
			//return payMatched(request, response,reqParam);
		} catch (Exception e) {
			// 验签失败 单独查询订单 TODO
			e.printStackTrace();
		}
		return PayStatus.PAY_FAIL;
	}
	public int payMatched(HttpServletRequest request,HttpServletResponse response,Map reqParam) {
		logger.debug("中行支付", "进入中行支付orderformunpay -- > orderform .....开始。。。");
		String orderStatus = (String) reqParam.get("orderStatus");
		if (orderStatus == null || "".equals(orderStatus.trim())) {
			return PayStatus.PAY_FAIL;
		} else {
			orderStatus = orderStatus.trim();
		}
		if (!"1".equals(orderStatus)) {// 状态不为1，返回失败。状态为1 将 orderform_unpay 赋值到 orderform。
			return PayStatus.PAY_FAIL;
		}
		String orderNo = (String) reqParam.get("orderNo");
		if (orderNo == null || "".equals(orderNo.trim())) {
			return PayStatus.PAY_NO_ORDER;
		} else {
			orderNo = orderNo.trim();
		}
		
		// 
		
		 OrderFormPay order = orderFormService.getOrderByOrderNum(orderNo);
		
		if (order == null) {
			return PayStatus.PAY_NO_ORDER;
		}
		String orderAmount = (String) reqParam.get("orderAmount");
		if (orderAmount == null || "".equals(orderAmount.trim())) {
			return PayStatus.PAY_FAIL;
		} else {
			orderAmount = orderAmount.trim();
		}
		double money = 0.0;
		try {
			money = getMoneyByStr(orderAmount) * 100;
			BigDecimal transactionAmount= new BigDecimal(money);
				
				logger.debug("更改支付表:", "更改支付表开始......");
				// 更新支付订单表
				OrderFormUnPay orderFormUnpay = orderFormService.queryOrderFormUnPaymentByOrderNumber(orderNo);
				logger.debug("更改支付表:", "被更改");
				super.afterServerNotify(orderFormUnpay,(String)reqParam.get("orderSeq"));
				return PayStatus.PAY_SUCCESS;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};
		return PayStatus.PAY_FAIL;
	}
	public int doPaymentPageNotify(HttpServletRequest req,
			HttpServletResponse response) {
		//中行支付页面通知
		/*
		logger.debug("info","支付页面返回-----中行支付接收页面通知开始......");
		try {
			req.setCharacterEncoding("UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		String encoding = req.getParameter("encoding");
		
		logger.debug("info","支付页面返回-----中行支付接收页面通知开始......返回报文中encoding=[" + encoding + "]");
		
		Map reqParam = BOCPayUtil.getAllRequestParam(req);

		String orderNum = (String) reqParam.get("orderNo");
		ComplexOrderBean complexOrderBean = payService.queryComplexOrderBeanByOrderNumber(orderNum);
		
		OrderFormPay orderForm = complexOrderBean.getOrderform();
		if (null == orderForm) {
			logger.debug("info","支付后台-----中行支付接收页面通知结束......,状态：【"+PayStatus.PAY_NO_ORDER+"】");
			return PayStatus.PAY_NO_ORDER;
		}

		PaymentBean payment = payTypeService.getPayType(orderForm.getPaytype());
		
		RSASig rsa = new RSASig();
		String strSrc ="POSID="+reqParam.get("POSID")+"&BRANCHID="+reqParam.get("BRANCHID")+"&ORDERID="+reqParam.get("ORDERID")+"&PAYMENT="+reqParam.get("PAYMENT")+
		"&CURCODE="+reqParam.get("CURCODE")+"&REMARK1=&REMARK2="+
		"&SUCCESS="+reqParam.get("SUCCESS")+"&TYPE="+reqParam.get("TYPE")+"&REFERER="+(reqParam.get("REFERER")==null?"":reqParam.get("REFERER"))+"&CLIENTIP="+reqParam.get("CLIENTIP")+"&ACCDATE="+reqParam.get("ACCDATE");
		
		if(reqParam.get("USRMSG")!=null)strSrc = strSrc+"&USRMSG="+reqParam.get("USRMSG");
		logger.debug("info","支付后台-----中行支付接收页面通知......,strSrc【"+strSrc+"】");
		
		String strSign = (String)reqParam.get("SIGN");//签名
		String strPubkey = payment.getPubkey();//公钥
			
		if (strSrc == null) {
			strSrc = "";
		}
		if (strSign == null) {
			strSign = "";
		}
		if (strPubkey == null) {
			strPubkey = "";
		}
		rsa.setPublicKey(strPubkey);
		boolean bRet = rsa.verifySigature(strSign, strSrc);
		String strRet;
		if (bRet) {
			String trade_status = (String) reqParam.get("orderStatus");//0：未处理   1：支付   2：撤销  3：退货  4：未明  5：失败
			if (trade_status.equals("1")) {
				logger.debug("info","支付后台-----中行支付接收页面通知......,中行返回状态：SUCCESS=Y");
				if (afterPageNotify(orderForm, response)) {
					logger.debug("info","支付后台-----中行支付接收页面通知结束......,状态：【"+PayStatus.PAY_SUCCESS+"】");
					return PayStatus.PAY_SUCCESS;
				}
			}
		}else{
			logger.debug("info","支付后台-----中行支付接收页面通知结束...验签失败...,状态：【"+PayStatus.PAY_VALIDATE_FAIL+"】");
			return PayStatus.PAY_VALIDATE_FAIL;
		}
		logger.debug("info","支付后台-----中行支付接收页面通知结束......,状态：【"+PayStatus.PAY_FAIL+"】");
		return PayStatus.PAY_FAIL; // 请不要修改或删除  */
		
		return PayStatus.PAY_FAIL; 
	}

	public int refundNotify(HttpServletRequest request,
			HttpServletResponse response) {
		return PayStatus.PAY_SUCCESS;
	}

	@Override
	public String refund(HttpServletRequest req,
			HttpServletResponse response) {
		//退货接口
		logger.debug("中行支付退货", "中行支付退货接口开始。。。。。");
		Refund refund = super.beforeRefund(req);
		JSONObject json = new JSONObject();
		int statu = PayStatus.REFUND_FAIL;
		String errorMsg = "";
		if (refund == null) {
			json.put("status", statu);
			json.put("errormsg", "already refund all");
			return json.toString();
		}
		PaymentBean payment = payTypeService.getPayType(refund.getPaytype());
		
		// 把请求参数打包成数组
		Map<String, String> sParaTemp = new HashMap<String, String>();
		
		try {
			sParaTemp.put("merchantNo", URLEncoder.encode(payment.getMerchantid(),"UTF-8"));
			sParaTemp.put("mRefundSeq", URLEncoder.encode(BOCPayUtil.getRefundSeqNo(),"UTF-8"));//商户退款交易流水号 30
			sParaTemp.put("curCode", URLEncoder.encode("001","UTF-8"));//退款币种
			sParaTemp.put("refundAmount", URLEncoder.encode(getMoneyStr(refund.getRefundmoney()/100.00),"UTF-8"));//退款金额
			sParaTemp.put("orderNo", URLEncoder.encode(refund.getOrdernumber(),"UTF-8"));//商户订单号
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}//商户号
		
		// 签名源数据
		/*StringBuilder signDataSrc = new StringBuilder();
		signDataSrc.append(sParaTemp.get("merchantNo"));
		signDataSrc.append("|");
		signDataSrc.append(sParaTemp.get("mRefundSeq"));
		signDataSrc.append("|");
		signDataSrc.append("001");
		signDataSrc.append("|");
		signDataSrc.append(sParaTemp.get("refundAmount"));
		signDataSrc.append("|");
		signDataSrc.append(sParaTemp.get("orderNo"));
		logger.debug("中行支付退货", "中行支付退货签名源数据"+signDataSrc.toString());*/
		String data = sParaTemp.get("merchantNo")+"|"+sParaTemp.get("mRefundSeq")+"|"+"001"+"|"+sParaTemp.get("refundAmount")+"|"+sParaTemp.get("orderNo");//signDataSrc.toString();
		logger.debug("中行支付退货", "中行支付退货签名源数据:"+data);
		try {
			PKCSTool tool = PKCSTool.getSigner(payment.getPubkey(),payment.getPrikey(), payment.getMerchantpwd(), "PKCS7");
			//tool = PKCSTool.getSigner(payment.getPubkey(),payment.getPrikey(), payment.getMerchantpwd(), "PKCS7");
			String signature = tool.p7Sign(data.getBytes("UTF-8"));
			//String signature = tool.p1Sign(data.getBytes("UTF-8"));
			sParaTemp.put("signData", URLEncoder.encode(signature,"UTF-8"));//商户签名数据
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.debug("中行退货", "中行退款签名成功!");
		// 构建退款表单，提交退款请求。
		try {
			// 添加中行post 请求
			String urlString = payment.getExt();//退款请求url
			logger.debug("中行退货", "中行退货请求url:"+urlString);
			logger.debug("中行退货","中行退货请求参数："+sParaTemp.toString());
			// 表单请求
			//String url = BOCPaySubmit.createRefundHtml(urlString,sParaTemp);
			HttpRequester httpRequester = new HttpRequester();
			HttpResponse httpRse = httpRequester.sendPost(urlString, sParaTemp);
			logger.debug("中行退货", "中行退货发送退货请求");
			if(httpRse.getCode() == 200){
				// 请求成功
				logger.debug("中行退货", "中行退货请求成功！");
				String flag = dealReturnNotify(httpRse, payment,refund);
				if("2".equals(flag)){
					// 退款成功，通知成功 TODO 
					errorMsg = "send req success";
					afterRefund(refund.getOrdernumber());
					orderFormService.updatePayStatus(refund.getOrdernumber(), PayStatus.PAY_SUCCESS_REFUND);
					statu = PayStatus.PAY_SUCCESS_REFUND;
					logger.debug("中行退货", "退款成功，通知成功");
				}else if("1".equals(flag)){
					// 退款成功，退款金额不同
					errorMsg = "send req success,not match";
					afterRefund(refund.getOrdernumber());
					orderFormService.updatePayStatus(refund.getOrdernumber(), PayStatus.PAY_SUCCESS_REFUND);
					statu = PayStatus.PAY_SUCCESS_REFUND;
					logger.debug("中行退货", "退款成功，金额不同");
				}else if("3".equals(flag)){
					// 退款失败
					json.put("status", statu);
					json.put("errormsg", "send search fail");
					logger.debug("中行退货", "签名失败，查询失败");
					return json.toString();
				}else if("4".equals(flag)){
					// 退款失败
					json.put("status", statu);
					json.put("errormsg", "send req fail1");
					logger.debug("中行退货", "退款失败1");
					return json.toString();
				}else{
					// 退款失败
					json.put("status", statu);
					json.put("errormsg", flag);
					logger.debug("中行退货", "退款失败"+flag);
					return json.toString();
				}
			}else{
				// 请求失败
				json.put("status", statu);
				json.put("errormsg", "send req fail");
				logger.debug("中行退货", "请求失败");
				return json.toString();
			}
		} catch (IOException e) {
			json.put("status", statu);
			json.put("errormsg", "send req exception");
			logger.debug("中行退货", "退货异常");
			e.printStackTrace();
			return json.toString();
		}
		
		json.put("status", statu);
		json.put("errormsg", errorMsg);
		json.put("corderid", refund.getCorderid());
		json.put("money", refund.getRefundmoney());
	    return json.toString();
	}

	@Override
	public List<Check> check(HttpServletRequest request,
			HttpServletResponse response, int payTypeId) throws Exception {
		logger.debug("中行支付", "中行支付对账开始.......");
		if (!beforeCheck(request)) {
			return null;
		}
		List<Check> resultcheck = new ArrayList<Check>();
		List<Check> payCheck = new ArrayList<Check>();
		List<Check> backCheck = new ArrayList<Check>();
		PaymentBean payment = payTypeService.getPayType(payTypeId);
		String [] obj = getTicketId(payment,request);
		if(obj != null && obj.length == 2){
			String ticketId = obj[0];
			String uri = obj[1];
			// 成功获取票号。// 下载对账文件
			Map requestFileMap = new HashMap();
			requestFileMap.put("uri", uri);
			requestFileMap.put("ticketId", ticketId);
			// 获取中行对账文件
			String urlStr = payment.getExt2()+"?uri="+uri+"&ticketId="+ticketId;
			logger.debug("中行支付", "获取对账文件url:"+urlStr);
			File checkFile = BOCPayUtil.getCheckFile(urlStr,payment.getExt3());
			// 解析对账文件
			if(checkFile == null){
				logger.debug("中行支付","获取对账文件失败!");
			}else{
				logger.debug("中行支付","解析对账文件开始！"+checkFile.getAbsolutePath()+"--"+checkFile.getName());
				//  解压包
				File unzipFile = BOCPayUtil.unZipFiles(checkFile,payment.getExt3()+"/");
				logger.debug("中行支付", "解压成功:"+unzipFile.getAbsolutePath()+"--"+unzipFile.getName());
				// 解析内容，获取支付和退货的list
				Map map = BOCPayUtil.checkFileParse(unzipFile);
				payCheck = (ArrayList)map.get("payList");
				backCheck = (ArrayList)map.get("refundList");
				logger.debug("中行支付", "支付size:"+payCheck.size());
				logger.debug("中行支付", "退货size:"+backCheck.size());
			}
		}
		try {
			if(payCheck!=null&&payCheck.size()>0){
				for(int i =0;i<payCheck.size();i++){
					Check check = payCheck.get(i);
					OrderFormPay orderFormPay = payService.getOrderFormByOrderNumPay(check.getOrdernumber());
					if (orderFormPay != null) {
						if(check.getPrice().contains("-")) check.setCorderid(orderFormPay.getMemo());//如果是退款金额，则取退款的应收单
						else 	check.setCorderid(orderFormPay.getCorderid());//如果取得数据为付款金额，则取原账单
					}
					check.setPaytype(payTypeId+"");
					resultcheck.add(check);
				}
			}
			if(backCheck!=null&&backCheck.size()>0){
				for(int i =0;i<backCheck.size();i++){
					Check check = backCheck.get(i);
					OrderFormPay orderFormPay = payService.getOrderFormByOrderNumPay(check.getOrdernumber());
					if (orderFormPay != null) {
						if(check.getPrice().contains("-")) check.setCorderid(orderFormPay.getMemo());//如果是退款金额，则取退款的应收单
						else 	check.setCorderid(orderFormPay.getCorderid());//如果取得数据为付款金额，则取原账单
					}
					check.setPaytype(payTypeId+"");
					resultcheck.add(check);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
		
		return resultcheck;
	}
	private String getTimeStr(Date date, String format) {
		return new SimpleDateFormat(format).format(date);
	}
	private String getMoneyStr(double val) {
		DecimalFormat fmt = new DecimalFormat("########0.00");
		return fmt.format(val);
	}
	private double getMoneyByStr(String val) throws ParseException {
		DecimalFormat fmt = new DecimalFormat("########0.00");
		return fmt.parse(val).doubleValue();
	}
	@Override
	public OrderFormUnPay getOrderFormUnPay(HttpServletRequest request,
			HttpServletResponse response) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String dealReturnNotify(HttpResponse httpRse,PaymentBean payment,Refund refund){
		// 先判断验签是否成功，验签成功后 判断交易是否成功 ，验签不成功 查询订单。
		// 0 失败，1 扣款成功 验签失败 2.扣款成功 验签成功
		String flag = "0";
		String strContent = httpRse.getContent();// 返回的xml文件
		logger.debug("中行退货", "中行支付退货返回的源消息消息"+strContent);
		// 退货成功，验签 做操作，退款失败，返回结果
		//解析xml文件
		Map mapRe = BOCPayUtil.readStringXmlOut(strContent);
		logger.debug("中行退货", "中行退货f返回的信息Map:"+mapRe);
		String dealStatus = (String)mapRe.get("dealStatus");
		if("0".equals(dealStatus)){ // TODO
			// 成功
			logger.debug("中行退货", "中行退货成功！");
			flag = "1";
			try{
				// 验签
				String signature = (String)mapRe.get("signData");
				logger.debug("中行退货", "中行退货验签的源数据"+signature);
				InputStream rootCertStream = new FileInputStream(
						payment.getSeckey());
				InputStream verifyCertStream = null;
				
				PKCSTool tool = PKCSTool.getVerifier(rootCertStream,verifyCertStream);
				// merchantNo|mRefundSeq|refundAmount|orderNo|orderSeq|orderAmount|bankTranSeq|tranTime|dealStatus
				String data = mapRe.get("merchantNo")+"|"+mapRe.get("mRefundSeq")+"|"+mapRe.get("refundAmount")+"|"+mapRe.get("orderNo")+"|"+mapRe.get("orderSeq")+"|"+mapRe.get("orderAmount")+"|"+mapRe.get("bankTranSeq")+"|"+mapRe.get("tranTime")+"|"+mapRe.get("dealStatus");
				logger.debug("中行退货", "中行退货验签源数据:"+data);
				tool.p7Verify(signature, data.getBytes("UTF-8"));
				// 判断金额是否相同
				if("0".equals(dealStatus)){
					if((new java.math.BigDecimal((String)mapRe.get("refundAmount")).multiply(new java.math.BigDecimal(100)).intValue()) < (new java.math.BigDecimal(refund.getRefundmoney()).intValue())){
						//if( && (Long)mapRe.get("refundAmount")*100 == refund.getRefundmoney()){// 交易成功，返回的退款金额和申请的退款金额相同
							logger.debug("中行退货", "中行退货成功，退款金额不相同！");
							flag = "1";
						}else{
							logger.debug("中行退货", "中行退货成功，退款金额相同！");
							flag = "2";
						}
				}else{
					logger.debug("中行退货", "中行退货失败！");
					flag = "4";
				}
				
			}catch(Exception ex){
				// 验签失败，单独查询订单 TODO
				logger.debug("中行退货","中行退货验签失败，主动发起订单查询!");
				String searchResult = orderSearch(payment,refund);
				if("success".equals(searchResult)){
					// 查询成功，进行退货处理
					flag = "2";
				}else if("fail".equals(searchResult)){
					// 查询失败
					flag = "3";
				}else if("notMatch".equals(searchResult)){
					// 金额不匹配
					flag = "1";
				}else{
					flag = "4";
				}
				ex.printStackTrace();
			}
		}else {
			flag = (String)mapRe.get("exception");
		}
		return flag;
	}
	
	public String orderSearch(PaymentBean payment,Refund refund){
		String orderSearchUrl = payment.getExtCol1(); // 订单查询url
		Map mapOrderSearch = new HashMap();
		mapOrderSearch.put("merchantNo",payment.getMerchantid()+"");
		mapOrderSearch.put("orderNos",refund.getOrdernumber());// 商户生产的订单号
		try {
			PKCSTool tool = PKCSTool.getSigner(payment.getPubkey(),payment.getPrikey(), payment.getMerchantpwd(), "PKCS7");
			String orders = (String)mapOrderSearch.get("orderNos");
			String data = mapOrderSearch.get("merchantNo")+":"+orders;//要签名的源数据
			logger.debug("中行支付", "中行订单查询签名源数据："+data);
			String signature = tool.p7Sign(data.getBytes("UTF-8"));
			logger.debug("中行支付", "中行订单查询签名数据"+signature);
			mapOrderSearch.put("signData",URLEncoder.encode(signature,"utf-8"));
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
		}
		
		// 发起订单查询请求
		HttpRequester httpRequester = new HttpRequester();
		try {
			HttpResponse httpRsSearch = httpRequester.sendPost(orderSearchUrl, mapOrderSearch);
			if(httpRsSearch.getCode() == 200){
				// 获得响应报文，
				String orderSearchContent = httpRsSearch.getContent();
				logger.debug("中行支付", "中行订单查询返回的报文"+orderSearchContent);
				// 解析订单查询报文
				Map map = BOCPayUtil.readStringXmlOut(orderSearchContent);
				String exception = (String)map.get("exception");
				String orderStatus = (String)map.get("orderStatus");
				if(exception == null && "3".equals(orderStatus)){
					// 支付成功 ， 验证金额，TODO
					String total_fee = (String)map.get("payAmount");
					if ((new java.math.BigDecimal(total_fee).multiply(new java.math.BigDecimal(100)).intValue()) < (new java.math.BigDecimal(refund.getRefundmoney()).intValue())) {
						logger.debug("中行支付", "订单查询，金额不匹配");
						// 金额不匹配
						return "notMatch";
					}else{
						// 支付成功
						return "success";
					}
				}else{
					logger.debug("中行支付", "中行订单查询失败");
					return "fail";
				}
			}else{
				logger.debug("中行支付", "中行订单查询请求失败");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	public String[] getTicketId(PaymentBean payment,HttpServletRequest request){
		// 支付请求开始 取票
		Map<String, String> sParaTemp = new HashMap<String, String>();
		sParaTemp.put("merchantNo", payment.getMerchantid());
		sParaTemp.put("handleType", "1");
		sParaTemp.put("fileType", "CC");// 必填 可参考7.3文件类型说明 清算对账文件 获得清算对账文件
		// 获取所需要的文件的日期
		String dateFile = (String)request.getAttribute("starttime");
		dateFile = dateFile.replace("-", "");
		sParaTemp.put("fileDate", dateFile);
		// YYYYMMDD24HHMMSS
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		sParaTemp.put("submitTime", sdf.format(new Date()));
		sParaTemp.put("extend", "111");
		// 签名源文件extend=${extend}|fileDate=${fileDate}|fileType=${fileType}|
		//handleType=${handleType}|merchantNo=${merchantNo}|submitTime=${submitTime}
		StringBuilder sbSign = new StringBuilder();
		sbSign.append("extend="+sParaTemp.get("extend"));
		sbSign.append("|fileDate="+sParaTemp.get("fileDate"));
		sbSign.append("|fileType="+sParaTemp.get("fileType"));
		sbSign.append("|handleType="+sParaTemp.get("handleType"));
		sbSign.append("|merchantNo="+sParaTemp.get("merchantNo"));
		sbSign.append("|submitTime="+sParaTemp.get("submitTime"));
		logger.debug("中行支付", "签名源数据"+sbSign.toString());
		String signature = "";
		try {
			PKCSTool tool = PKCSTool.getSigner(payment.getPubkey(),payment.getPrikey(), payment.getMerchantpwd(), "PKCS7");
			signature = tool.p7Sign(sbSign.toString().getBytes("UTF-8"));
		} catch (GeneralSecurityException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		logger.debug("中行支付", "签名后数据"+signature);
		try {
			sParaTemp.put("signData", java.net.URLEncoder.encode(signature, "UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		//提交请求 获取取票
		String ticketUrl = payment.getExt1();
		HttpRequester httpRequester = new HttpRequester();
		try {
			HttpResponse httpRsSearch = httpRequester.sendPost(ticketUrl, sParaTemp);
			//获取银行返回内容 xml.
			String content = httpRsSearch.getContent();
			logger.debug("中行支付", "中行支付获取取票源数据"+content);
			// 
			Map contentMap = BOCPayUtil.readStringXmlOut(content);
			//验签
			String bankSignData = (String)contentMap.get("signData");// 获得签名数据
			InputStream rootCertStream = new FileInputStream(
					payment.getSeckey());
			InputStream verifyCertStream = null;
			
			// 验签源数据 extend=${extend}|fileDate=$fileDate}|fileType=${fileType}|
			//handleType=${handleType}|invalidTime=${invalidTime}|merchantNo=${merchantNo}|
			//submitTime=${submitTime}|ticketId=${ticketId}|uri=${ticketId}
			StringBuilder datav = new StringBuilder();
			datav.append("extend="+contentMap.get("extend"));
			datav.append("|fileDate="+contentMap.get("fileDate"));
			datav.append("|fileType="+contentMap.get("fileType"));
			datav.append("|handleType="+contentMap.get("handleType"));
			datav.append("|invalidTime="+contentMap.get("invalidTime"));
			datav.append("|merchantNo="+contentMap.get("merchantNo"));
			datav.append("|submitTime="+contentMap.get("submitTime"));
			datav.append("|ticketId="+contentMap.get("ticketId"));
			datav.append("|uri="+contentMap.get("uri"));
			PKCSTool toolv = PKCSTool.getVerifier(rootCertStream,verifyCertStream);
			toolv.p7Verify(bankSignData, datav.toString().getBytes("UTF-8"));
			
			// 验签成功，获取票号，
			String ticketId = (String)contentMap.get("ticketId");
			String uri = (String)contentMap.get("uri");
			String [] obj = new String[2];
			obj[0] = ticketId;
			obj[1] = uri;
			return obj;
		}catch (Exception e) {
			logger.debug("中行支付", "中行支付获得ticketid出错。");
			e.printStackTrace();
		}
		return null;
	}
	
	public File byte2File(String content,String path){
		 if(content != null && content.length() > 0){
			 try {
				byte[] imgByte = hex2byte(content);
				InputStream in = new ByteArrayInputStream(imgByte);
				File file=new File(path);//可以是任何图片格式.jpg,.png等 
				FileOutputStream fos=new FileOutputStream(file);
				byte[] b = new byte[1024];
				int nRead = 0;
				while ((nRead = in.read(b)) != -1) {
					fos.write(b, 0, nRead);
				}
				fos.flush();
				fos.close();
				in.close();
				return file;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
			}
		}
		return null;
	}
	
	 /**
	  * 二进制转字符串
	  *  @param 
	  *   @return      */
	public static String byte2hex(byte[] b) // 二进制转字符串
	{         StringBuffer sb = new StringBuffer();
	String stmp = "";
	for (int n = 0; n < b.length; n++) {
		stmp = Integer.toHexString(b[n] & 0XFF);
		if (stmp.length() == 1) {
			sb.append("0" + stmp);             
			} else {
				sb.append(stmp);             }
		}         return sb.toString();     
		}
	 /**
		字符串转二进制 
		@param str 要转换的字符串
		@return  转换后的二进制数组
	*/
	public static byte[] hex2byte(String str) { 
		// 字符串转二进制
		if (str == null)
			return null;
		str = str.trim();
		int len = str.length();
		if (len == 0 || len % 2 == 1)
			return null;
		byte[] b = new byte[len / 2];
		try {
			for (int i = 0; i < str.length(); i += 2) {
				b[i / 2] = (byte) Integer.decode("0X" + str.substring(i, i + 2)).intValue();
				}
			return b;
		} catch (Exception e) {
			return null;
		}
	}
}
