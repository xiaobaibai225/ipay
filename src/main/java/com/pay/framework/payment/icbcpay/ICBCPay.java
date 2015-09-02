package com.pay.framework.payment.icbcpay;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
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

import cn.com.infosec.icbc.ReturnValue;

import com.pay.framework.log.LogManager;
import com.pay.framework.payment.BasePay;
import com.pay.framework.payment.IPay;
import com.pay.framework.payment.ccbpay.CCBPaySubmit;
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
 * 工行支付
 * @author shaochangfu
 *
 */
@Component("icbcpay")
public class ICBCPay extends BasePay implements IPay {
	LogManager logger = LogManager.getLogger(ICBCPay.class);

	@Autowired
	private OrderFormService orderFormService;

	@Autowired
	private PayService payService;
	public static final String yyyyMMddHHmmss = "yyyyMMddHHmmss";
	
	@Override
	public String getForwardUrl(HttpServletRequest request,
			HttpServletResponse response, int payTypeId) {
		logger.debug("进入工行支付", "进入工行支付。。。。。。payTypeId："+payTypeId);
		OrderFormUnPay orderform = beforePay(request, payTypeId);
		logger.debug("1", "11");
		if (null == orderform) {
			logger.debug("2", "22");
			return null;
		}
		logger.debug("3", "33");
		PaymentBean payment = payTypeService.getPayType(orderform.getPaytype());
		// 把请求参数打包成数组
		Map<String, String> m = new HashMap<String, String>();
		m.put("interfaceName", payment.getExt());//接口名称 
		m.put("interfaceVersion", payment.getExtCol1());//接口版本号
		
		//交易数据
		//交易时间
		Date orderDate = new Date();
		orderDate.setTime(orderDate.getTime());
		DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		logger.debug("工行签名", "支付订单时间:"+df.format(orderDate));
		String tranData = "<?xml version=\"1.0\" encoding=\"GBK\" standalone=\"no\"?>"
		+"<B2CReq>"
		+"<interfaceName>"+payment.getExt()+"</interfaceName>"
		+"<interfaceVersion>"+payment.getExtCol1()+"</interfaceVersion>"
		+"<orderInfo>"
		+"<orderDate>"+df.format(orderDate)+"</orderDate>"
		+"<curType>001</curType>"
		+"<merID>"+payment.getMerchantid()+"</merID>"
		+"<subOrderInfoList>"
		+"<subOrderInfo>"
		+"<orderid>"+orderform.getOrdernumber()+"</orderid>"
		+"<amount>"+orderform.getPrice()+"</amount>"
		+"<installmentTimes>1</installmentTimes>"
		+"<merAcct>"+payment.getQuerykey()+"</merAcct>"
		+"<goodsID></goodsID>"
		+"<goodsName>"+orderform.getProductname()+"</goodsName>"
		+"<goodsNum></goodsNum>"
		+"<carriageAmt></carriageAmt>"
		+"</subOrderInfo>"
		+"</subOrderInfoList>"
		+"</orderInfo>"
		+"<custom>"
			+"<verifyJoinFlag>0</verifyJoinFlag>"
			+"<Language>ZH_CN</Language>"
		+"</custom>"
		+"<message>"
			+"<creditType>2</creditType>"
			+"<notifyType>HS</notifyType>"
			+"<resultType>1</resultType>"
			+"<merReference>"+payment.getExt8()+"</merReference>"
			+"<merCustomIp></merCustomIp>"
			+"<goodsType>1</goodsType>"
			+"<merCustomID></merCustomID>"
			+"<merCustomPhone></merCustomPhone>"
			+"<goodsAddress></goodsAddress>"
			+"<merOrderRemark></merOrderRemark>"
			+"<merHint></merHint>"
			+"<remark1></remark1>"
			+"<remark2></remark2>"
			+"<merURL>"+payment.getBgurl()+"</merURL>"
			+"<merVAR></merVAR>"
		+"</message>"
		+"<extend>"
		+"<e_isMerFlag></e_isMerFlag>"
		+"<e_Name></e_Name>"
		+"<e_TelNum></e_TelNum>"
		+"<e_CredType></e_CredType>"
		+"<e_CredNum></e_CredNum>"
		+"</extend>"
		+"</B2CReq>";
		String password = payment.getMerchantpwd();// 商户密码 
		
		try{
			byte[] byteSrc = tranData.getBytes("GBK");
			byte[] byteStrEnc = ReturnValue.base64enc(byteSrc);
			String TranDataBase64 = new String(byteStrEnc).toString();// 交易明文数据 64 位编码
			m.put("tranData", TranDataBase64);//交易数据
			
			String merSignMsg = "";// 交易明文二进制编码64位编码
		
			FileInputStream in2 = new FileInputStream(payment.getPrikey());//user.key
			byte[] bkey = new byte[in2.available()];
			in2.read(bkey);
			in2.close();
			char[] keyPass = password.toCharArray();
			
			logger.debug("工行签名", bkey.toString()+"---"+keyPass.toString()+"---"+password+"---"+byteSrc);
			byte[] sign =ReturnValue.sign(byteSrc,byteSrc.length,bkey,keyPass);
			if(sign==null){
				logger.debug("工行签名", "签名失败");
				return null;
			}else{
				byte[] EncSign = ReturnValue.base64enc(sign);
				String SignMsgBase64=new String(EncSign).toString();
				m.put("merSignMsg", SignMsgBase64);//订单签名数据
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		try{
			FileInputStream in1 = new FileInputStream(payment.getPubkey());//user.crt
			byte[] bcert = new byte[in1.available()];
			in1.read(bcert);
			in1.close();
			byte[] EncCert=ReturnValue.base64enc(bcert);
			m.put("merCert",new String(EncCert).toString());//商城证书公钥, 商户用二进制方式读取证书公钥文件后，进行BASE64编码后产生的字符串；
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		logger.debug("交易数据", tranData);
		
		// 建立请求
		logger.debug("工行支付请求页面URL", "工行支付请求页面URL:"+payment.getPosturl());
		String sHtmlText = ICBCPaySubmit.createHtml(payment.getPosturl(), m);
		logger.debug("工行建立请求", "工行建立请求:"+sHtmlText);
		return sHtmlText;
	}

	@Override
	public int doPaymentServerNotify(HttpServletRequest request,
			HttpServletResponse response) {
		logger.debug("info","支付后台-----工行支付接收后台通知开始......");
		try {
			request.setCharacterEncoding("GBK");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		String encoding = request.getParameter("encoding");
		
		Map reqParam = ICBCPayUtil.getAllRequestParam(request);// TODO 处理银行通知返回结果
		// 解析xml 文件，
		String signMsg = (String)reqParam.get("signMsg");//获得银行签名数据
		String notifyData = (String)reqParam.get("notifyData");
		byte[] nofifyDataMW = ReturnValue.base64dec(notifyData.getBytes());
		try {
			nofifyDataMW = ReturnValue.base64dec(notifyData.getBytes("GBK"));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}//讲银行返回的通知解码 TODO scf tranData.getBytes("GBK")
		logger.debug("工行支付", "工行返回的交易数据："+new String(nofifyDataMW).toString());
		
		Map notifyDataMap = ICBCPayUtil.readStringXmlOut(new String(nofifyDataMW).toString());
		
		String orderNum = (String) notifyDataMap.get("orderid");
				
		OrderFormUnPay orderFormUnpay = orderFormService.queryOrderFormUnPaymentByOrderNumber(orderNum);
		
		if (orderFormUnpay == null) {
			try {
				PrintWriter out = response.getWriter();
				out.println("success");
				out.flush();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			logger.debug("info","支付后台-----工行支付接收后台通知结束......,状态：【"+PayStatus.PAY_NO_ORDER+"】");
			return PayStatus.PAY_NO_ORDER;
		}

		PaymentBean payment = payTypeService.getPayType(orderFormUnpay.getPaytype());
		int a = 1;
		try{
			/* FileInputStream in1 = new FileInputStream(payment.getPubkey());
			byte[] bcert = new byte[in1.available()];
			in1.read(bcert);
			in1.close();
			FileInputStream in2 = new FileInputStream(payment.getPrikey());
			byte[] bkey = new byte[in2.available()];
			in2.read(bkey);
			in2.close();
			byte[] EncCert=ReturnValue.base64enc(bcert);
			byte[] DecCert = ReturnValue.base64dec(EncCert);
			String password = payment.getMerchantpwd();
			char[] keyPass = password.toCharArray();
			byte[] sign = ReturnValue.sign(nofifyDataMW,nofifyDataMW.length,bkey,keyPass);
			////
			byte[] EncSign = ReturnValue.base64enc(sign);
			byte[] DecSign = ReturnValue.base64dec(EncSign);
			
			// 对银行返回的数据进行验签，
			a = ReturnValue.verifySign(nofifyDataMW,nofifyDataMW.length,DecCert,DecSign);
			*/
			// 验签，对返回的数据进行验签
			byte[] sign = signMsg.getBytes("GBK");// 获得银行签名数据
			logger.info("工行验签", "工行返回的签名源数据"+new String(sign).toString());
			//byte[] EncSign = ReturnValue.base64enc(sign);
			byte[] DecSign = ReturnValue.base64dec(sign);
			logger.info("工行验签", "工行返回的签名源数据64解码"+new String(DecSign).toString());
			
			FileInputStream in1 = new FileInputStream(payment.getSeckey());
			byte[] bcert = new byte[in1.available()];
			in1.read(bcert);
			in1.close();
			
			byte[] EncCert=ReturnValue.base64enc(bcert);
			byte[] DecCert = ReturnValue.base64dec(EncCert);
			
			a = ReturnValue.verifySign(nofifyDataMW,nofifyDataMW.length,DecCert,DecSign);
			
		}catch(Exception ex){
			ex.printStackTrace();
			logger.debug("工行验签", "验签失败！");
			try {
				PrintWriter out = response.getWriter();
				out.println("error");
				out.flush();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return PayStatus.PAY_FAIL;
		}
		if(a==0){
			// 验签成功
			String tranStat = (String)notifyDataMap.get("tranStat");
			if("1".equals(tranStat)){
				logger.debug("工行验签", "工行交易成功，已经清算");
				// 交易成功
				// 判断金额
				String total_fee = (String)notifyDataMap.get("amount");
				if ((new java.math.BigDecimal(total_fee).multiply(new java.math.BigDecimal(100)).intValue()) < (new java.math.BigDecimal(orderFormUnpay.getPrice()).intValue())) {
					try {
						PrintWriter out = response.getWriter();
						out.println("error");
						out.flush();
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					logger.debug("info","支付后台-----工行支付接收后台通知结束......,状态：【"+PayStatus.PAY_NOT_MATCH+"】");
					return PayStatus.PAY_NOT_MATCH;
				}
				logger.debug("工行支付", "工行支付流水单号："+(String)notifyDataMap.get("tranSerialNo"));
				logger.debug("工行支付", "工行支付订单："+orderFormUnpay.getOrdernumber());
				logger.debug("工行支付", "order back url:"+orderFormUnpay.getFronturl());
				logger.debug("工行支付", "orderDate:"+(String)notifyDataMap.get("orderDate"));
				//TODO  要放开后台通知
				super.afterServerNotify(orderFormUnpay,(String)notifyDataMap.get("tranSerialNo"));
				// 工行后台通知方式是 ： 工行实时通知给商户，商户返回供货连接或者中断，银行会把返回的供货连接发送给客户。
				//构造取货连接
				String backurl = orderFormUnpay.getFronturl();
				try {
					PrintWriter out = response.getWriter();
					response.addHeader("HTTP/1.1 200", "OK");
					response.addHeader("Server", "Apache/1.39");
					response.addHeader("Content-Length",backurl.length()+"");
					response.addHeader("Content-type", "text/html");
					out.write(backurl);
					out.flush();
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				logger.debug("info","支付后台-----工行支付接收后台通知结束......,状态：【"+PayStatus.PAY_SUCCESS+"】");
				return PayStatus.PAY_SUCCESS;
			}else if("2".equals(tranStat)){
				logger.debug("工行验签", "工行交易失败");
				try {
					PrintWriter out = response.getWriter();
					out.println("error");
					out.flush();
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return PayStatus.PAY_FAIL;
			}else{
				logger.debug("工行验签", "工行交易可疑");
				try {
					PrintWriter out = response.getWriter();
					out.println("error");
					out.flush();
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return PayStatus.PAY_FAIL;
			}
		}else{
			//验签失败
			logger.debug("info","支付后台-----工行支付接收后台通知，签名验证失败......");
			try {
				PrintWriter out = response.getWriter();
				out.println("error");
				out.flush();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			logger.debug("info","支付后台-----工行支付接收后台通知结束......,状态：【"+PayStatus.PAY_VALIDATE_FAIL+"】");
			return PayStatus.PAY_VALIDATE_FAIL;
		}
	}

	/**
	 * 页面通知
	 */
	@Override
	public int doPaymentPageNotify(HttpServletRequest request,
			HttpServletResponse response) {
		// 判断后台通知，通知通过 后 校验前台通知
		int serverPage = PayStatus.PAY_SUCCESS;//doPaymentServerNotify(request, response);
		if(serverPage==PayStatus.PAY_SUCCESS){
			logger.debug("info","支付页面返回-----工行支付接收页面通知开始......");

			logger.debug("info","支付后台-----工行支付接收后台通知开始......");
			try {
				request.setCharacterEncoding("GBK");
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
			String encoding = request.getParameter("encoding");
			
			Map reqParam = ICBCPayUtil.getAllRequestParam(request);// TODO 处理银行通知返回结果
			// 解析xml 文件，
			String notifyData = (String)reqParam.get("notifyData");
			byte[] nofifyDataMW = ReturnValue.base64dec(notifyData.getBytes());//讲银行返回的通知解码 TODO scf
			
			logger.debug("工行支付", "页面通知：返回数据"+new String(nofifyDataMW).toString());
			Map notifyDataMap = ICBCPayUtil.readStringXmlOut(new String(nofifyDataMW).toString());
			
			String orderNum = (String) notifyDataMap.get("orderid");
			logger.debug("工行支付", "页面通知：orderNum"+orderNum);
			//OrderFormUnPay orderFormUnpay = orderFormService.queryOrderFormUnPaymentByOrderNumber(orderNum);
			
			ComplexOrderBean complexOrderBean = payService.queryComplexOrderBeanByOrderNumber(orderNum);
			
			OrderFormPay orderForm = complexOrderBean.getOrderform();
			
			if (null == orderForm) {
				logger.debug("info","支付后台-----工行支付接收页面通知结束......,状态：【"+PayStatus.PAY_NO_ORDER+"】");
				return PayStatus.PAY_NO_ORDER;
			}

			PaymentBean payment = payTypeService.getPayType(orderForm.getPaytype());
			int a = 0;
			try{
				FileInputStream in1 = new FileInputStream(payment.getPubkey());
				byte[] bcert = new byte[in1.available()];
				in1.read(bcert);
				in1.close();
				FileInputStream in2 = new FileInputStream(payment.getPrikey());
				byte[] bkey = new byte[in2.available()];
				in2.read(bkey);
				in2.close();
				byte[] EncCert=ReturnValue.base64enc(bcert);
				byte[] DecCert = ReturnValue.base64dec(EncCert);
				String password = payment.getMerchantpwd();
				char[] keyPass = password.toCharArray();
				byte[] sign =ReturnValue.sign(nofifyDataMW,nofifyDataMW.length,bkey,keyPass);
				byte[] EncSign = ReturnValue.base64enc(sign);
				byte[] DecSign = ReturnValue.base64dec(EncSign);
				
				// 对银行返回的数据进行验签，
				a = ReturnValue.verifySign(nofifyDataMW,nofifyDataMW.length,DecCert,DecSign);
			}catch(Exception ex){
				ex.printStackTrace();
				logger.debug("工行验签", "验签失败！");
				try {
					PrintWriter out = response.getWriter();
					out.println("error");
					out.flush();
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return PayStatus.PAY_FAIL;
			}
			if(a==0){
				// 验签成功
				String tranStat = (String)notifyDataMap.get("tranStat");
				if("1".equals(tranStat)){
					logger.debug("工行验签", "工行交易成功，已经清算");
					// 交易成功
					// 判断金额
					String total_fee = (String)notifyDataMap.get("amount");
					if ((new java.math.BigDecimal(total_fee).multiply(new java.math.BigDecimal(100)).intValue()) < (new java.math.BigDecimal(orderForm.getPrice()).intValue())) {

						try {
							PrintWriter out = response.getWriter();
							out.println("error");
							out.flush();
							out.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						logger.debug("info","支付后台-----工行支付接收页面通知结束......,状态：【"+PayStatus.PAY_NOT_MATCH+"】");
						return PayStatus.PAY_NOT_MATCH;
					}
					super.afterPageNotify(orderForm, response);
					try {
						PrintWriter out = response.getWriter();
						out.println("success");
						out.flush();
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					logger.debug("info","支付后台-----工行支付接收页面通知结束......,状态：【"+PayStatus.PAY_SUCCESS+"】");
					return PayStatus.PAY_SUCCESS;
				}else if("2".equals(tranStat)){
					logger.debug("工行验签", "工行交易失败");
					try {
						PrintWriter out = response.getWriter();
						out.println("error");
						out.flush();
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					return PayStatus.PAY_FAIL;
				}else{
					logger.debug("工行验签", "工行交易可疑");
					try {
						PrintWriter out = response.getWriter();
						out.println("error");
						out.flush();
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					return PayStatus.PAY_FAIL;
				}
			}else{
				//验签失败
				logger.debug("info","支付后台-----工行支付接收页面通知，签名验证失败......");
				try {
					PrintWriter out = response.getWriter();
					out.println("error");
					out.flush();
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				logger.debug("info","支付后台-----工行支付接收页面通知结束......,状态：【"+PayStatus.PAY_VALIDATE_FAIL+"】");
				return PayStatus.PAY_VALIDATE_FAIL;
			}
		}else{
			return serverPage;// 后台通知错误
		}
	}

	@Override
	public String refund(HttpServletRequest req,
			HttpServletResponse response) throws Exception {
		// 
		logger.debug("工行退款", "工行退款开始...");
		Refund refund = super.beforeRefund(req);
		JSONObject json = new JSONObject();
		int statu = PayStatus.REFUND_FAIL;
		String errorMsg = "";
		if (refund == null) {
			logger.debug("工行退款", "退款对象为空");
			json.put("status", statu);
			json.put("errormsg", "already refund all");
			return json.toString();
		}
		PaymentBean payment = payTypeService.getPayType(refund.getPaytype());
		OrderFormPay orderForPay = orderFormService.getOrderByOrderNum(refund.getOrdernumber());
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat sdf2 = new SimpleDateFormat("HHmmssSSS");//TODO 精确到毫秒
		SimpleDateFormat sdf3 = new SimpleDateFormat("yyyyMMddHHmmssSSS");//yyyyMMddHHmmssSSS
		SimpleDateFormat sdf4 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		Date datenow = new Date();
		
		// 把请求参数打包成数组
		Map<String, String> sParaTemp = new HashMap<String, String>();
		sParaTemp.put("TransCode", "EBUSCOM");
		sParaTemp.put("CIS",payment.getExt6());
		sParaTemp.put("BankCode", payment.getExt5());
		sParaTemp.put("ID", payment.getExt4());
		sParaTemp.put("TranDate", sdf1.format(datenow));
		sParaTemp.put("TranTime", sdf2.format(datenow)+"000");
		sParaTemp.put("fSeqno",ICBCPayUtil.getFSeqno());//fSeqnoERP系统产生的指令包序列号，一个集团永远不能重复
		sParaTemp.put("TranType", "0");
		sParaTemp.put("ShopType", "2");//1：B2B商城  2：B2C商城
		sParaTemp.put("ShopCode", payment.getMerchantid());
		sParaTemp.put("ShopAcct", payment.getQuerykey());
		sParaTemp.put("OrderNum", refund.getOrdernumber());//订单号
		sParaTemp.put("PayType", "0");//支付速度
		sParaTemp.put("PayDate", sdf1.format(sdf4.parse(orderForPay.getSubmitdate())));// 原订购支付日期
		sParaTemp.put("TransferName", "");
		sParaTemp.put("TransferAccNo", "");
		sParaTemp.put("PayAmt", refund.getRefundmoney()+"");
		sParaTemp.put("SignTime", sdf3.format(datenow));
		sParaTemp.put("ReqReserved1", "");
		sParaTemp.put("ReqReserved2", "");
		sParaTemp.put("AcctSeq", "");
		errorMsg = ICBCPaySubmit.refundSocketEBSS(sParaTemp, payment);
		
		if("success".equals(errorMsg)){
			afterRefund(refund.getOrdernumber());
			orderFormService.updatePayStatus(refund.getOrdernumber(), PayStatus.PAY_SUCCESS_REFUND);
			statu = PayStatus.PAY_SUCCESS_REFUND;
		}
		json.put("status", statu);
		json.put("errormsg", errorMsg);
		json.put("corderid", refund.getCorderid());
		json.put("money", refund.getRefundmoney());
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
		if (!beforeCheck(request)) {
			return null;
		}
		List<Check> resultcheck = new ArrayList<Check>();
		logger.debug("工行对账","查询对账信息开始......");
		PaymentBean payment = payTypeService.getPayType(payTypeId);
		// 把请求参数打包成数组
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat sdf2 = new SimpleDateFormat("HHmmssSSS");//TODO 精确到毫秒
		SimpleDateFormat sdf3 = new SimpleDateFormat("yyyyMMddHHmmssSSS");//yyyyMMddHHmmssSSS
		Date datenow = new Date();
		
		
		String dateFile = (String)request.getAttribute("starttime");
		dateFile = dateFile.replace("-", "");
		
		logger.debug("工行对账", "工行对账日期："+dateFile);
		// 支付
		Map<String, String> sParaTemp = new HashMap<String, String>();
		sParaTemp.put("TransCode", "B2CPAYINF");
		sParaTemp.put("CIS", payment.getExt6());
		sParaTemp.put("BankCode", payment.getExt5());
		sParaTemp.put("ID", payment.getExt4());
		sParaTemp.put("TranDate", sdf1.format(datenow));
		sParaTemp.put("TranTime", sdf2.format(datenow)+"000");
		sParaTemp.put("fSeqno", ICBCPayUtil.getFSeqno());//ERP系统产生的指令包序列号，一个集团永远不能重复 // TODO
		sParaTemp.put("QryFlag", "0");//0：使用北京时间查询   1：使用工行主机系统时间查询
		sParaTemp.put("ShopType", "2");//1：B2B商城  2：B2C商城 3：C2C商城
		sParaTemp.put("ShopCode", payment.getMerchantid());
		sParaTemp.put("ShopAcct", payment.getQuerykey());
		sParaTemp.put("QrySerialNo", "");
		sParaTemp.put("QryOrderNum", "");
		sParaTemp.put("BeginDate", dateFile);// 对账，前一天的
		sParaTemp.put("EndDate", dateFile);// 对账，前一天的
		sParaTemp.put("BeginTime", "");
		sParaTemp.put("EndTime", "");
		sParaTemp.put("ResultType", "010");// 成功
		sParaTemp.put("NextTag", "");
		sParaTemp.put("ErpOrder", "");
		sParaTemp.put("QueryType", "1");// 订单对账查询
		sParaTemp.put("AcctSeq", "");
		
		List<Check> payCheck = ICBCPaySubmit.checkSocketEBSS(sParaTemp, payment,"1");
		//sParaTemp.put("ResultType", "0");// 成功 2-B2C退货订单查询（融e购）
		//sParaTemp.put("QueryType", "2");
		
		// 退货查询
		Map<String, String> sParaRefundTemp = new HashMap<String, String>();
		sParaRefundTemp.put("TransCode", payment.getExt7());
		sParaRefundTemp.put("CIS",payment.getExt6());
		sParaRefundTemp.put("BankCode", payment.getExt5());
		sParaRefundTemp.put("ID", payment.getExt4());
		sParaRefundTemp.put("TranDate", sdf1.format(datenow));
		sParaRefundTemp.put("TranTime", sdf2.format(datenow)+"000");
		sParaRefundTemp.put("fSeqno", ICBCPayUtil.getFSeqno());
		sParaRefundTemp.put("Ordertype", "0");//0：退货 1：返还 2：转付
		sParaRefundTemp.put("ShopType", "2");///1：B2B商城  2：B2C商城 3：C2C商城
		sParaRefundTemp.put("ShopCode",payment.getMerchantid());
		sParaRefundTemp.put("ShopAcct", payment.getQuerykey());
		sParaRefundTemp.put("QrySerialNo", "");
		sParaRefundTemp.put("QryOrderNum", "");
		sParaRefundTemp.put("BeginDate", dateFile);
		sParaRefundTemp.put("EndDate", dateFile);
		sParaRefundTemp.put("BeginTime", "");
		sParaRefundTemp.put("EndTime", "");
		sParaRefundTemp.put("ResultType", "010");
		sParaRefundTemp.put("PTOrderNo", "");
		sParaRefundTemp.put("NextTag", "");
		sParaRefundTemp.put("IfSeqno", "");
		sParaRefundTemp.put("AcctSeq", "");
		//退货查询
		List<Check> backCheck = ICBCPaySubmit.checkSocketEBSS(sParaRefundTemp, payment,"2");
		
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

	@Override
	public OrderFormUnPay getOrderFormUnPay(HttpServletRequest request,
			HttpServletResponse response) {
		logger.debug("工行通知", "进入延时支付页面开始。。。");
		try {
			request.setCharacterEncoding("GBK");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		Map reqParam = ICBCPayUtil.getAllRequestParam(request);// TODO 处理银行通知返回结果
		// 解析xml 文件，
		String notifyData = (String)reqParam.get("notifyData");
		byte[] nofifyDataMW = ReturnValue.base64dec(notifyData.getBytes());//讲银行返回的通知解码 TODO scf
		
		logger.debug("工行通知", "进入延时支付页面:交易明文数据..."+new String(nofifyDataMW).toString());
		
		Map notifyDataMap = ICBCPayUtil.readStringXmlOut(new String(nofifyDataMW).toString());
		
		String orderNum = (String) notifyDataMap.get("orderid");
		logger.debug("工行通知", "getOrderFormUnPay:orderNum="+orderNum);
		
		ComplexOrderBean complexOrderBean = payService.queryComplexOrderBeanByOrderNumber(orderNum);
		OrderFormUnPay orderUnpay = complexOrderBean.getOrderformunpay();
		OrderFormPay orderpay = complexOrderBean.getOrderform();
		return orderUnpay;
	}

}
