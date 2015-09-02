package com.pay.framework.payment.bocpay;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pay.framework.http.HttpRequester;
import com.pay.framework.http.HttpResponse;
import com.pay.framework.log.LogManager;
import com.pay.framework.payment.BasePay;
import com.pay.framework.payment.IPay;
import com.pay.framework.payment.bocpay.util.PKCSTool;
import com.pay.framework.service.OrderFormService;
import com.pay.framework.service.PayService;
import com.pay.model.Check;
import com.pay.model.OrderFormPay;
import com.pay.model.OrderFormUnPay;
import com.pay.model.PayStatus;
import com.pay.model.PaymentBean;
import com.pay.model.Refund;

/*
 * 中行B2B 支付
 */
@Component("bocpayb2b")
public class BOCPayB2B extends BasePay implements IPay {
	static LogManager logger = LogManager.getLogger(BOCPay.class);
	@Autowired
	private OrderFormService orderFormService;

	@Autowired
	private PayService payService;
	public static final String yyyyMMddHHmmss = "yyyyMMddHHmmss";
	
	@Override
	public String getForwardUrl(HttpServletRequest request,
			HttpServletResponse response, int payTypeId) {
		logger.debug("进入中行B2B支付", "进入中行支付。。。。。。payTypeId："+payTypeId);
		// 处理支付通用逻辑
		OrderFormUnPay orderform = BocBeforePay(request, payTypeId);
		if (null == orderform) {
			return null;
		}
		PaymentBean payment = payTypeService.getPayType(orderform.getPaytype());
		// 把请求参数打包成数组
		Map<String, String> m = new HashMap<String, String>();
		m.put("bocNo", payment.getMerchantid());
		//m.put("payType", "1");
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

			// bocNo|orderNo|curCode|orderAmount|orderTime
			String data = m.get("bocNo") + "|" + m.get("orderNo") + "|"
					+ m.get("curCode") + "|" + m.get("orderAmount") + "|"
					+ m.get("orderTime");
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
	private String getMoneyStr(double val) {
		DecimalFormat fmt = new DecimalFormat("########0.00");
		return fmt.format(val);
	}
	private double getMoneyByStr(String val) throws ParseException {
		DecimalFormat fmt = new DecimalFormat("########0.00");
		return fmt.parse(val).doubleValue();
	}
	private String getTimeStr(Date date, String format) {
		return new SimpleDateFormat(format).format(date);
	}
	@Override
	public int doPaymentServerNotify(HttpServletRequest request,
			HttpServletResponse response) {

		logger.debug("中行B2B支付:", "进入主动通知......");
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
			String tranSeq = (String) reqParam.get("tranSeq");
			String tranCode = (String) reqParam.get("tranCode");
			String tranStatus = (String) reqParam.get("tranStatus");
			String tranTime = (String) reqParam.get("tranTime");
			String tranAmount = (String) reqParam.get("tranAmount");
			String feeAmount = (String) reqParam.get("feeAmount");
			String bocNo = (String) reqParam.get("bocNo");
			String curCode = (String) reqParam.get("curCode");
			// tranSeq| tranCode| tranStatus| tranTime| tranAmount| feeAmount| bocNo| orderNo| curCode
			String data =  tranSeq + "|" +tranCode+"|"+ tranStatus + "|"
					+ tranTime + "|" + tranAmount + "|" + feeAmount + "|"
					+ bocNo +"|"+orderNo+"|"+curCode;
			logger.debug("中行支付", "验签明文："+data);
			
			//tool.p1Verify(signature, data.getBytes("UTF-8"));
			tool.p7Verify(signature, data.getBytes("UTF-8"));
			logger.debug("验证根证书:", "验证根证书结束......");
			
			// 判断是否扣款成功  只有终态:成功/失败
			logger.debug("中行支付", "中行支付状态："+tranStatus);
			if (tranStatus == null || "".equals(tranStatus.trim())) {
				return PayStatus.PAY_FAIL;
			} else {
				tranStatus = tranStatus.trim();
			}
			if (!"1".equals(tranStatus)) {// 状态不为1，返回失败。状态为1 将 orderform_unpay 赋值到 orderform。
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
			if (tranAmount == null || "".equals(tranAmount.trim())) {
				return PayStatus.PAY_FAIL;
			} else {
				tranAmount = tranAmount.trim();
			}
			
			double money = 0.0;
			//money = getMoneyByStr(orderAmount) * 100;
			BigDecimal transactionAmount= new BigDecimal(tranAmount);
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

	@Override
	public int doPaymentPageNotify(HttpServletRequest request,
			HttpServletResponse response) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String refund(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		//退货接口
		logger.debug("中行支付退货", "中行支付退货接口开始。。。。。");
		Refund refund = super.beforeRefund(request);
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
			sParaTemp.put("orderSeq", URLEncoder.encode(refund.getTranseq(),"UTF-8"));
			sParaTemp.put("bocNo", URLEncoder.encode(payment.getMerchantid(),"UTF-8"));//商户退款交易流水号 30
			sParaTemp.put("curCode", URLEncoder.encode("001","UTF-8"));//退款币种
			sParaTemp.put("tranAmount", URLEncoder.encode(getMoneyStr(refund.getRefundmoney()/100.00),"UTF-8"));//退款金额
			sParaTemp.put("orderNo", URLEncoder.encode(refund.getOrdernumber(),"UTF-8"));//商户订单号
			sParaTemp.put("traceNo", URLEncoder.encode(BOCPayUtil.getRefundSeqNoB2B(),"UTF-8"));//商户向网关发送联机交易时避免重复提交时使用的参考号
			sParaTemp.put("orderUrl", URLEncoder.encode(payment.getExt4(),"UTF-8"));//网关完成交易获得明确交易状态后向该URL发送通知 // TODO
			
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}//商户号
		//orderSeq|bocNo|orderNo|curCode|tranAmount
		String data = sParaTemp.get("orderSeq")+"|"+sParaTemp.get("bocNo")+"|"+sParaTemp.get("orderNo")+"|"+sParaTemp.get("curCode")+"|"+sParaTemp.get("tranAmount");//signDataSrc.toString();
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
				logger.debug("中行退货", "中行退货请求成功！等待通知。。。。。");
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
		String rtnCode = (String)mapRe.get("rtnCode"); // 头信息00通讯成功, 有异常时返回网关错误码

		if("00".equals(rtnCode)){ // TODO
			// 成功
			logger.debug("中行退货", "中行退货返回成功成功！");
			flag = "1";
			try{
				// 判断金额是否相同
				String tranStatus = (String)mapRe.get("tranStatus");
				if("1".equals(tranStatus)){
					if((new java.math.BigDecimal((String)mapRe.get("tranAmount")).multiply(new java.math.BigDecimal(100)).intValue()) < (new java.math.BigDecimal(refund.getRefundmoney()).intValue())){
						//if( && (Long)mapRe.get("refundAmount")*100 == refund.getRefundmoney()){// 交易成功，返回的退款金额和申请的退款金额相同
							logger.debug("中行退货", "中行退货成功，退款金额不相同！");
							flag = "1";
						}else{
							logger.debug("中行退货", "中行退货成功，退款金额相同！");
							flag = "2";
						}
				}else{
					logger.debug("中行退货", "中行退货【"+tranStatus+"】");
					flag = tranStatus;
				}
				
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}else {
			// 未知 TODO
			flag = (String)mapRe.get("rtnMsg");
		}
		return flag;
	}

	@Override
	public int refundNotify(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		logger.debug("中行B2B退货", "中行B2B退货接收通知开始....");
		// 网关完成交易获得明确交易状态后向该URL发送通知 ，获取通知信息
		Map map = BOCPayUtil.getAllRequestParam(request);
		System.out.println("中行B2B退货:"+map.toString());
		logger.debug("中行B2B退货", "银行返回的退货参数:"+map.toString());
//		InputStream ins = request.getInputStream();
		// TODO 解析
		InputStream in = request.getInputStream();
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
		StringBuffer temp = new StringBuffer();
		String line = bufferedReader.readLine();
		while (line != null) {
			temp.append(line).append("\r\n");
			line = bufferedReader.readLine();
		}
		bufferedReader.close();
		in.close();
		logger.debug("中行B2B退货:", "银行返回的退货参数:"+temp);
		return 0;
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
				Map map = BOCPayUtil.checkFileParseB2B(unzipFile);
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

	public String[] getTicketId(PaymentBean payment,HttpServletRequest request){
		// 支付请求开始 取票
		Map<String, String> sParaTemp = new HashMap<String, String>();
		sParaTemp.put("merchantNo", payment.getMerchantid());
		sParaTemp.put("handleType", "1");
		sParaTemp.put("fileType", "MCC");// 必填 可参考7.3文件类型说明 清算对账文件 获得清算对账文件
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
	@Override
	public OrderFormUnPay getOrderFormUnPay(HttpServletRequest request,
			HttpServletResponse response) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
