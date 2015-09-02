package com.pay.framework.payment.ccbpay;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pay.framework.log.LogFlags;
import com.pay.framework.log.LogManager;
import com.pay.framework.payment.BasePay;
import com.pay.framework.payment.IPay;
import com.pay.framework.payment.alipay.Md5Encrypt;
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
 * 建行支付 BTOB
 * 
 * @author sunqi
 * 
 */
@Component("ccbpaybb")
public class CCBPayBtoB  extends BasePay implements IPay {

	LogManager logManager = LogManager.getLogger(CCBPayBtoB.class);

	@Autowired
	private OrderFormService orderFormService;

	@Autowired
	private PayService payService;

	public static final String C_TIME_PATTON_DEFAULT = "yyyyMMdd";

	public static final DateFormat df = new SimpleDateFormat(
			C_TIME_PATTON_DEFAULT);

	@Override
	public String getForwardUrl(HttpServletRequest request,
			HttpServletResponse response, int payTypeId) {
		logManager.debug("info","-----建行支付BtoB--支付开始......");
		OrderFormUnPay orderform = beforePay(request, payTypeId);
		if (null == orderform) {
			return null;
		}
		
		PaymentBean payment = payTypeService.getPayType(orderform.getPaytype());
		String  fourparams = payment.getMerchantid();// 商户名，商户id，商户柜台代码，分行代码
		String[] bankparams = fourparams.split(",");
		
		BigDecimal price = new BigDecimal(orderform.getPrice() + "");
		price = price.divide(new BigDecimal(100));
		
		Map<String,String> sParaTemp = new HashMap<String,String>();
		sParaTemp.put("MERCHANTID", bankparams[1]);
		sParaTemp.put("POSID", bankparams[2]);
		sParaTemp.put("BRANCHID", bankparams[3]);
		sParaTemp.put("ORDERID", orderform.getOrdernumber());
		sParaTemp.put("PAYMENT", price+"");
		sParaTemp.put("CURCODE", "01");
		sParaTemp.put("TXCODE", "690401");
		sParaTemp.put("REMARK1", "");
		sParaTemp.put("REMARK2", "");
		
		String paramsStr =
				"MERCHANTID="+bankparams[1]+"&POSID="+bankparams[2]+"&BRANCHID="+bankparams[3]+
				"&ORDERID="+orderform.getOrdernumber()+"&PAYMENT="+price
				+"&CURCODE=01&TXCODE=690401&REMARK1=&REMARK2=";
		String mac =  Md5Encrypt.md5(paramsStr);
		sParaTemp.put("MAC", mac);//必填 采用标准MD5算法，由商户实现
		logManager.debug("info","建行---MAC="+mac+"---参与md5算法的请求参数串......"+paramsStr);
		try{
			String sHtmlxml = CCBPayBtoBUtil.createHtml(payment.getPosturl(), sParaTemp);
			logManager.debug("info","建行-----建行支付请求.....sHtmlxml表单xml串.."+sHtmlxml);
			response.getWriter().write(sHtmlxml);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return PayStatus.PAY_SUCCESS_TOPAY;
	}

	/* (non-Javadoc) 建行支付后台通知
	 * @see com.pay.framework.payment.IPay#doPaymentServerNotify(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public int doPaymentServerNotify(HttpServletRequest req,
			HttpServletResponse response) {
		String requestMethod=req.getMethod().toUpperCase();
		logManager.debug("info","支付后台-----建行BtoB支付接收后台通知开始......是以【"+requestMethod+"】方式请求的");
		try {
			if(requestMethod.equals("POST"))
			{
				req.setCharacterEncoding("GBK");
			}
		} catch (UnsupportedEncodingException e1) {
			logManager.debug("info","支付后台-----建行BtoB支付接收后台通知....设置编码GBK失败");
			e1.printStackTrace();
		}
		String order_no = (String)req.getParameter("ORDER_NUMBER");
		OrderFormUnPay orderFormUnpay = orderFormService.queryOrderFormUnPaymentByOrderNumber(order_no);
		PaymentBean payment = payTypeService.getPayType(orderFormUnpay.getPaytype());
		
		if (orderFormUnpay == null) {
			try {
				PrintWriter out = response.getWriter();
				out.println("success");
				out.flush();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			logManager.debug("info","支付后台-----建行支付接收后台通知结束......,状态：【"+PayStatus.PAY_NO_ORDER+"】");
			return PayStatus.PAY_NO_ORDER;
		}
		String MPOSID = req.getParameter("MPOSID")==null?"":(String)req.getParameter("MPOSID");
		String CUST_ID = req.getParameter("CUST_ID")==null?"":(String)req.getParameter("CUST_ID");
		String ACC_NO = req.getParameter("ACC_NO")==null?"":(String)req.getParameter("ACC_NO");
		String ACC_NAME = req.getParameter("ACC_NAME")==null?"":(String)req.getParameter("ACC_NAME");
		String AMOUNT = req.getParameter("AMOUNT")==null?"":(String)req.getParameter("AMOUNT");
		String STATUS = req.getParameter("STATUS")==null?"":(String)req.getParameter("STATUS");
		String REMARK1 = req.getParameter("REMARK1")==null?"":(String)req.getParameter("REMARK1");
		String REMARK2 = req.getParameter("REMARK2")==null?"":(String)req.getParameter("REMARK2");
		String TRAN_FLAG = req.getParameter("TRAN_FLAG")==null?"":(String)req.getParameter("TRAN_FLAG");
		String TRAN_TIME = req.getParameter("TRAN_TIME")==null?"":(String)req.getParameter("TRAN_TIME");
		String BRANCH_NAME = req.getParameter("BRANCH_NAME")==null?"":(String)req.getParameter("BRANCH_NAME");
		String SIGNSTRING = req.getParameter("SIGNSTRING")==null?"":(String)req.getParameter("SIGNSTRING");
		
		if(requestMethod.equals("GET"))
		{
			try {
				ACC_NAME=new String(ACC_NAME.getBytes("ISO8859_1"), "GBK");
				BRANCH_NAME=new String(BRANCH_NAME.getBytes("ISO8859_1"), "GBK"); 
			} catch (UnsupportedEncodingException e) {
				logManager.debug("info","支付后台-----建行BtoB支付接收后台通知....从ISO8859_1转码为GBK失败");
				e.printStackTrace();
			} 
			
		}
		
		String strSrc = MPOSID+order_no+CUST_ID+ACC_NO+ACC_NAME+AMOUNT+STATUS+REMARK1+REMARK2+TRAN_FLAG+TRAN_TIME+BRANCH_NAME;
		
		RSASig rsa = new RSASig();
		String strPubkey = payment.getPubkey();//公钥
		logManager.debug("info","支付后台-----建行BtoB支付接收后台通知....strSrc= "+strSrc);
		logManager.debug("info","支付后台-----建行BtoB支付接收后台通知....SIGNSTRING= "+SIGNSTRING);
		if (strSrc == null) {
			strSrc = "";
		}
		if (SIGNSTRING == null) {
			SIGNSTRING = "";
		}
		if (strPubkey == null) {
			strPubkey = "";
		}
		rsa.setPublicKey(strPubkey);
		boolean bRet = rsa.verifySigature(SIGNSTRING, strSrc);

		if (bRet) {
			logManager.debug("info","支付后台-----建行BtoB支付接收后台通知，签名验证成功......");
			if (STATUS.equals("2")||STATUS.equals("3")||STATUS.equals("4")) {
				logManager.debug("info","支付后台-----建行BtoB支付接收后台通知，建行返回状态：STATUS."+STATUS+".成功.....");
	
				// 判断金额
				if ((new java.math.BigDecimal(AMOUNT)
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
						logManager.debug("info","支付后台-----建行BtoB支付接收后台通知结束......,状态：【"+PayStatus.PAY_NOT_MATCH+"】");
						return PayStatus.PAY_NOT_MATCH;
				}
				super.afterServerNotify(orderFormUnpay,order_no);
			}
			try {
				PrintWriter out = response.getWriter();
				out.println("success");
				out.flush();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			logManager.debug("info","支付后台-----建行BtoB支付接收后台通知结束......,状态：【"+PayStatus.PAY_SUCCESS+"】");
			return PayStatus.PAY_SUCCESS;
		}else {
			logManager.debug("info","支付后台-----建行B2B支付接收后台通知，签名验证失败......");
			Map sParaTemp = new HashMap();
			String  fourparams = payment.getMerchantid();// 商户名，商户id，商户柜台代码，分行代码
			String[] bankparams = fourparams.split(",");
			String  extparams = payment.getExt();//ip,port,operatecode,pwd
			String[] ebssparams = extparams.split(",");
			sParaTemp.put("ip", ebssparams[0]);
			sParaTemp.put("port", ebssparams[1]);
			sParaTemp.put("REQUEST_SN", System.currentTimeMillis()+"");//请求序列号 varChar(16)F 只可以使用数字 ---同一客户的请求序列码不能重复
			sParaTemp.put("CUST_ID", bankparams[1]);//商户号 varChar(21) F 字符型char，网银商户号 
			sParaTemp.put("USER_ID", ebssparams[2]);//操作员号	F 20051210后必须使用 
			sParaTemp.put("PASSWORD",ebssparams[3]);//密码 varChar(32) F 操作员号交易密码 
			sParaTemp.put("LANGUAGE", "01");
			sParaTemp.put("KIND", "0");
			sParaTemp.put("ORDER", order_no);
			sParaTemp.put("DEXCEL", "1");
			sParaTemp.put("NORDERBY", "2");
			sParaTemp.put("PAGE", "1");
			sParaTemp.put("POS_CODE", MPOSID);
			sParaTemp.put("STATUS", "3");
			logManager.debug("info","支付后台-----建行B2B支付后台验签失败后，去银行查询该订单支付情况.开始.....");
			Map resultMap = CCBPaySubmit.checkOrderPay(sParaTemp);
			String success = resultMap.get("success")+"";
			if("true".equals(success)){
				String orderStatu = resultMap.get("orderStatu")+"";
				String money = resultMap.get("money")+"";
				logManager.debug("info","支付后台-----建行B2B支付后台验签失败后，去银行查询该订单.查询成功....orderStatu="+orderStatu+",,money="+money);
				if (orderStatu.equals("1")) {
					// 判断金额
					if ((new java.math.BigDecimal(money)
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
							logManager.debug("info","支付后台-----建行B2B支付接收后台通知结束...查询付款金额比对...,状态：【"+PayStatus.PAY_NOT_MATCH+"】");
							return PayStatus.PAY_NOT_MATCH;
					}
					logManager.debug("info","支付后台-----建行B2B支付接收后台通知结束...查询订单...处理支付成功逻辑");
					super.afterServerNotify(orderFormUnpay,order_no);
				}
				try {
					PrintWriter out = response.getWriter();
					out.println("success");
					out.flush();
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				logManager.debug("info","支付后台-----建行B2B支付接收后台通知结束......,状态：【"+PayStatus.PAY_SUCCESS+"】");
				return PayStatus.PAY_SUCCESS;
			}else{
				logManager.debug("info","支付后台-----建行B2B支付后台验签失败后，去银行查询该订单.查询失败.....");
			}
			
			try {
				PrintWriter out = response.getWriter();
				out.println("error");
				out.flush();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			logManager.debug("info","支付后台-----建行BtoB支付接收后台通知结束......,状态：【"+PayStatus.PAY_VALIDATE_FAIL+"】");
			return PayStatus.PAY_VALIDATE_FAIL;
			  
		}
	}

	@Override
	public int doPaymentPageNotify(HttpServletRequest req,
			HttpServletResponse response) {
		
		String requestMethod=req.getMethod().toUpperCase();

		logManager.debug("info","支付页面返回-----建行BtoB支付接收页面通知开始......是以【"+requestMethod+"】方式请求的");
		try {
			if(requestMethod.equals("POST"))
			{
				req.setCharacterEncoding("GBK");
			}
		} catch (UnsupportedEncodingException e1) {
			logManager.debug("info","支付后台-----建行BtoB支付接收页面通知....设置编码GBK失败");
			e1.printStackTrace();
		}
		
		String order_no = (String)req.getParameter("ORDER_NUMBER");
		ComplexOrderBean complexOrderBean = payService.queryComplexOrderBeanByOrderNumber(order_no);
		OrderFormPay orderForm = complexOrderBean.getOrderform();
		if (null == orderForm) {
			logManager.debug("info","支付后台-----建行支付接收页面通知结束......,状态：【"+PayStatus.PAY_NO_ORDER+"】");
			return PayStatus.PAY_NO_ORDER;
		}
		PaymentBean payment = payTypeService.getPayType(orderForm.getPaytype());
		
		String MPOSID = req.getParameter("MPOSID")==null?"":(String)req.getParameter("MPOSID");
		String CUST_ID = req.getParameter("CUST_ID")==null?"":(String)req.getParameter("CUST_ID");
		String ACC_NO = req.getParameter("ACC_NO")==null?"":(String)req.getParameter("ACC_NO");
		String ACC_NAME = req.getParameter("ACC_NAME")==null?"":(String)req.getParameter("ACC_NAME");
		String AMOUNT = req.getParameter("AMOUNT")==null?"":(String)req.getParameter("AMOUNT");
		String STATUS = req.getParameter("STATUS")==null?"":(String)req.getParameter("STATUS");
		String REMARK1 = req.getParameter("REMARK1")==null?"":(String)req.getParameter("REMARK1");
		String REMARK2 = req.getParameter("REMARK2")==null?"":(String)req.getParameter("REMARK2");
		String TRAN_FLAG = req.getParameter("TRAN_FLAG")==null?"":(String)req.getParameter("TRAN_FLAG");
		String TRAN_TIME = req.getParameter("TRAN_TIME")==null?"":(String)req.getParameter("TRAN_TIME");
		String BRANCH_NAME = req.getParameter("BRANCH_NAME")==null?"":(String)req.getParameter("BRANCH_NAME");
		String SIGNSTRING = req.getParameter("SIGNSTRING")==null?"":(String)req.getParameter("SIGNSTRING");
		
		if(requestMethod.equals("GET"))
		{
			try {
				ACC_NAME=new String(ACC_NAME.getBytes("ISO8859_1"), "GBK");
				BRANCH_NAME=new String(BRANCH_NAME.getBytes("ISO8859_1"), "GBK"); 
			} catch (UnsupportedEncodingException e) {
				logManager.debug("info","支付后台-----建行BtoB支付接收页面通知....从ISO8859_1转码为GBK失败");
				e.printStackTrace();
			} 
			
		}
		
		String strSrc = MPOSID+order_no+CUST_ID+ACC_NO+ACC_NAME+AMOUNT+STATUS+REMARK1+REMARK2+TRAN_FLAG+TRAN_TIME+BRANCH_NAME;
		
		RSASig rsa = new RSASig();
		String strPubkey = payment.getPubkey();//公钥
		logManager.debug("info","支付后台-----建行BtoB支付接收页面通知....strSrc= "+strSrc);
		logManager.debug("info","支付后台-----建行BtoB支付接收页面通知....SIGNSTRING= "+SIGNSTRING);
		if (strSrc == null) {
			strSrc = "";
		}
		if (SIGNSTRING == null) {
			SIGNSTRING = "";
		}
		if (strPubkey == null) {
			strPubkey = "";
		}
		rsa.setPublicKey(strPubkey);
		boolean bRet = rsa.verifySigature(SIGNSTRING, strSrc);

		if (bRet) {
			if (STATUS.equals("2")||STATUS.equals("3")||STATUS.equals("4")) {
				logManager.debug("info","支付后台-----建行BtoB支付接收页面通知......,建行返回状态：成功 。STATUS="+STATUS);
				if (afterPageNotify(orderForm, response)) {
					logManager.debug("info","支付后台-----建行BtoB支付接收页面通知结束......,状态：【"+PayStatus.PAY_SUCCESS+"】");
					return PayStatus.PAY_SUCCESS;
				}
			}
		}else{
			logManager.debug("info","支付后台-----建行BtoB支付接收页面通知结束...验签失败...,状态：【"+PayStatus.PAY_VALIDATE_FAIL+"】");
			return PayStatus.PAY_VALIDATE_FAIL;
		}
		logManager.debug("info","支付后台-----建行BtoB支付接收页面通知结束......,状态：【"+PayStatus.PAY_FAIL+"】");
		return PayStatus.PAY_FAIL; // 请不要修改或删除
	}

	@Override
	public String refund(HttpServletRequest req,
			HttpServletResponse response) throws Exception {
		logManager.debug("info","--建行BtoB--退款开始......");
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
		String  fourparams = payment.getMerchantid();// 商户名，商户id，商户柜台代码，分行代码
		String[] bankparams = fourparams.split(",");
		String  extparams = payment.getExt();//ip,port,operatecode,pwd
		String[] ebssparams = extparams.split(",");
		
		BigDecimal price = new BigDecimal(refund.getRefundmoney()).divide(new BigDecimal(100));
		// 把请求参数打包成数组
		Map<String, String> sParaTemp = new HashMap<String, String>();
		
		sParaTemp.put("REQUEST_SN", System.currentTimeMillis()+"");//请求序列号 varChar(16)F 只可以使用数字 ---同一客户的请求序列码不能重复
		sParaTemp.put("CUST_ID", bankparams[1]);//商户号 varChar(21) F 字符型char，网银商户号 
		sParaTemp.put("USER_ID", ebssparams[2]);//操作员号	F 20051210后必须使用 
		sParaTemp.put("PASSWORD",ebssparams[3]);//密码 varChar(32) F 操作员号交易密码 
		sParaTemp.put("TX_CODE", "5W1004");//交易码 varChar(6) F 交易请求码 --支付流水
		sParaTemp.put("LANGUAGE", "01");//语言 varChar(2) F CN 
		sParaTemp.put("MONEY", price+"");//退款金额     varChar(100) T  
		sParaTemp.put("ORDER", refund.getOrdernumber());//订单号   varChar(30) F 
		sParaTemp.put("SIGN_INFO", "");//签名信息 T
		sParaTemp.put("SIGNCERT","" );//签名CA信息 T 客户采用socket连接时，建行客户端自动添加 
		logManager.debug("info","--建行BtoB--退款.....订单号=."+refund.getOrdernumber());
		errorMsg = CCBPaySubmit.refundSocketEBSS(sParaTemp, payment);
		if("".equals(errorMsg)){
			afterRefund(refund.getOrdernumber());
			orderFormService.updatePayStatus(refund.getOrdernumber(), PayStatus.PAY_SUCCESS_REFUND);
			statu = PayStatus.PAY_SUCCESS_REFUND;
		}
		json.put("status", statu);
		json.put("errormsg", errorMsg);
		json.put("corderid", refund.getCorderid());
		json.put("money", refund.getRefundmoney());
		logManager.debug("info","--建行BtoB--退款结束......");
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
		logManager.debug("info","--建行BtoB--查询对账信息开始......");
		PaymentBean payment = payTypeService.getPayType(payTypeId);
		String orderDate = (String)request.getAttribute("starttime");
		orderDate = orderDate.replace("-", "");
		String  fourparams = payment.getMerchantid();// 商户名，商户id，商户柜台代码，分行代码
		String[] bankparams = fourparams.split(",");
		String  extparams = payment.getExt();//ip,port,operatecode,pwd
		String[] ebssparams = extparams.split(",");
		// 把请求参数打包成数组
		Map<String, String> sParaTemp = new HashMap<String, String>();

		sParaTemp.put("REQUEST_SN", System.currentTimeMillis()+"");//请求序列号 varChar(16)F 只可以使用数字 ---同一客户的请求序列码不能重复
		sParaTemp.put("CUST_ID", bankparams[1]);//商户号 varChar(21) F 字符型char，网银商户号 
		sParaTemp.put("USER_ID", ebssparams[2]);//操作员号	F 20051210后必须使用 
		sParaTemp.put("PASSWORD",ebssparams[3]);//密码 varChar(32) F 操作员号交易密码 
		sParaTemp.put("TX_CODE", "5W1005");//交易码 varChar(6) F 交易请求码 --支付流水
		sParaTemp.put("LANGUAGE", "01");//语言 varChar(2) F CN 
		sParaTemp.put("DATE", orderDate);//日期 varChar(8) F 　yyyyMMdd 
		sParaTemp.put("KIND","1");//流水类型 Char(1) F 1：已结流水（默认），0：未结流水
		sParaTemp.put("FILETYPE", "1");//文件类型 1：txt（默认），2：excel（一点接商户不支持excel文件格式下载）
		sParaTemp.put("TYPE", "0");//流水类型 F 0：支付流水；1：退款流水
		sParaTemp.put("NORDERBY", "1");//排序     Char(1) T 1:交易日期,2:订单号 
		sParaTemp.put("POS_CODE", bankparams[2]);//柜台号   varChar(9) T 　 
		sParaTemp.put("ORDER", "");//订单号 T
		sParaTemp.put("STATUS","1" );//流水状态 Char(1) F 0：交易失败,1：交易成功,2：待银行确认(未结流水);3：全部(未结流水) 

		try {
			logManager.debug("info","--建行BtoB--查询对账信息...获取支付流水信息开始...");
			List<Check> payCheck = CCBPaySubmit.checkSocketEBSS(sParaTemp, payment, "0");
			logManager.debug("info","--建行BtoB--查询对账信息...获取支付流水信息结束...");
			logManager.debug("info","--建行BtoB--查询对账信息...获取退款流水信息开始...");
			List<Check> backCheck = CCBPaySubmit.checkSocketEBSS(sParaTemp, payment, "1");
			logManager.debug("info","--建行BtoB--查询对账信息...获取退款流水信息结束...");
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
		logManager.debug("info","--建行BtoB--查询对账信息结束......");
		return resultcheck;
	}

	@Override
	public OrderFormUnPay getOrderFormUnPay(HttpServletRequest request,
			HttpServletResponse response) {
		String out_trade_no = "";
		try {
			out_trade_no = new String(request.getParameter("ORDER_NUMBER").getBytes(
					"ISO-8859-1"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ComplexOrderBean complexOrderBean = payService.queryComplexOrderBeanByOrderNumber(out_trade_no);
		OrderFormUnPay orderUnpay = complexOrderBean.getOrderformunpay();
		return orderUnpay;
	}

}
