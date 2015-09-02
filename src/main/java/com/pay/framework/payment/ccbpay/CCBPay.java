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
 * 建行支付
 * 
 * @author zidanezhang
 * 
 */
@Component("ccbpay")
public class CCBPay  extends BasePay implements IPay {

	LogManager logManager = LogManager.getLogger(CCBPay.class);

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
		OrderFormUnPay orderform = beforePay(request, payTypeId);
		if (null == orderform) {
			return null;
		}
		PaymentBean payment = payTypeService.getPayType(orderform.getPaytype());
		BigDecimal price = new BigDecimal(orderform.getPrice() + "");
		price = price.divide(new BigDecimal(100));
		// 把请求参数打包成数组
//		Map<String, String> sParaTemp = new HashMap<String, String>();
//		sParaTemp.put("MERCHANTID", payment.getMerchantid());//必填商户ID
//		sParaTemp.put("POSID", payment.getExt());//必填商户柜台代码,扩展字段
//		sParaTemp.put("BRANCHID", payment.getExtCol1());// 必填 分行代码,扩展字段
//		sParaTemp.put("ORDERID", orderform.getOrdernumber());//必填 定单号 由商户提供，最长30位
//		sParaTemp.put("PAYMENT", price+"");//必填 付款金额 由商户提供，按实际金额给出
//		sParaTemp.put("CURCODE", "01");//必填 币种 缺省为01－人民币 （只支持人民币支付）
//		sParaTemp.put("REMARK1", "");//网银不处理，直接传到城综网
//		sParaTemp.put("REMARK2", "");//网银不处理，直接传到城综网
//		sParaTemp.put("TXCODE", "520100");//必填 由建行统一分配为520100
//		sParaTemp.put("TYPE", "1");//必填 0- 非钓鱼接口;1- 防钓鱼接口;目前该字段以银行开关为准，如果有该字段则需要传送以下字段
////		sParaTemp.put("PUB", payment.getSeckey());//必填 仅作为源串参加MD5摘要，不作为参数传递
//		sParaTemp.put("GATEWAY","");//非必填 网关类型 W0Z1或W0Z2：仅显示帐号支付标签W1Z0或W2Z0：仅显示网银客户支付标签W1Z1或W2Z1：两个均显示，选中网银客户支付标签W1Z2：两个均显示，选中帐号支付标签W0Z0或W2Z2或其他：两个均显示，选中网银客户支付标签
//		sParaTemp.put("CLIENTIP",  "");//非必填 客户端IP 
//		//使用js的escape()方法对“客户注册信息”和“商品信息”进行转码，数字字母信息不需转码。例：escape(小飞侠)= %u5C0F%u98DE%u4FA0
//		sParaTemp.put("REGINFO", CCBPayUtil.escape(payment.getQuerykey()));//客户注册信息 客户在商户系统中注册的信息，中文需使用转码编码
//		sParaTemp.put("PROINFO",CCBPayUtil.escape(orderform.getProductname()));//商品信息，中文需使用转码编码
//		sParaTemp.put("REFERER", "");//商户URL 商户送空值即可，银行从后台读取商户设置的一级域名，如www.ccb.com则设为： “ccb.com”，最多允许设置三个不同的域名，格式为：****.com| ****.com.cn|****.net）
		
		String  fourparams = payment.getMerchantid();// 商户名，商户id，商户柜台代码，分行代码
		String[] bankparams = fourparams.split(",");
		
		String paramsStr =
				"MERCHANTID="+bankparams[1]+"&POSID="+bankparams[2]+"&BRANCHID="+bankparams[3]+"&ORDERID="+orderform.getOrdernumber()+"&PAYMENT="+price
				+"&CURCODE=01&TXCODE=520100&REMARK1=&REMARK2=&TYPE=1&PUB="+payment.getSeckey()+"&GATEWAY=&CLIENTIP="
				+"&REGINFO="+CCBPayUtil.escape(bankparams[0])+"&PROINFO="+CCBPayUtil.escape(orderform.getProductname().replaceAll("\\.|-|#|\\s", ""))+"&REFERER=";
		
		String mac =  Md5Encrypt.md5(paramsStr);
//		sParaTemp.put("MAC", mac);//必填 采用标准MD5算法，由商户实现
		logManager.debug("info","建行---MAC="+mac+"---参与md5算法的请求参数串......"+paramsStr);
		
//		sParaTemp.put("URL", payment.getPosturl());//https://ibsbjstar.ccb.com.cn/app/ccbMain 银行网址
//		Map req = new HashMap();
//		req.putAll(sParaTemp);
//		Set set = sParaTemp.keySet();
//		Iterator iterator = set.iterator();
//		while (iterator.hasNext()) {
//			String key = (String) iterator.next();
//			if ((sParaTemp.get(key) == null) || (sParaTemp.get(key) == "")) {
//				req.remove(key);
//			}
//
//		}
		
//		String sHtmlText = CCBPaySubmit.buildForwardUrl(sParaTemp);
		
//		请求路径，不包含pub公钥参数
		String urlStr =
			"MERCHANTID="+bankparams[1]+"&POSID="+bankparams[2]+"&BRANCHID="+bankparams[3]+"&ORDERID="+orderform.getOrdernumber()+"&PAYMENT="+price
			+"&CURCODE=01&TXCODE=520100&REMARK1=&REMARK2=&TYPE=1&GATEWAY=&CLIENTIP="
			+"&REGINFO="+CCBPayUtil.escape(bankparams[0])+"&PROINFO="+CCBPayUtil.escape(orderform.getProductname().replaceAll("\\.|-|#|\\s", ""))+"&REFERER=";
	
		String sHtmlText = payment.getPosturl()+"?"+urlStr+"&MAC="+mac;
		
		System.out.println("建行-----建行支付请求路径......"+sHtmlText);
		logManager.debug("info","建行-----建行支付请求路径......"+sHtmlText);
		return sHtmlText;
	}

	/* (non-Javadoc) 建行支付后台通知
	 * @see com.pay.framework.payment.IPay#doPaymentServerNotify(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public int doPaymentServerNotify(HttpServletRequest req,
			HttpServletResponse response) {
		//LogUtil.writeLog("BackRcvResponse接收后台通知开始");
		logManager.debug("info","支付后台-----建行支付接收后台通知开始......");
		try {
			req.setCharacterEncoding("ISO-8859-1");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
//		String encoding = req.getParameter("encoding");

		Map reqParam = CCBPayUtil.getAllRequestParam(req);

		String order_no = (String) reqParam.get("ORDERID");
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
			logManager.debug("info","支付后台-----建行支付接收后台通知结束......,状态：【"+PayStatus.PAY_NO_ORDER+"】");
			return PayStatus.PAY_NO_ORDER;
		}

		//LogUtil.printRequestLog(reqParam);

//		Map valideData = null;
//		if ((null != reqParam) && (!reqParam.isEmpty())) {
//			Iterator it = reqParam.entrySet().iterator();
//			valideData = new HashMap(reqParam.size());
//			while (it.hasNext()) {
//				Map.Entry e = (Map.Entry) it.next();
//				String key = (String) e.getKey();
//				String value = (String) e.getValue();
//				try {
//					value = new String(value.getBytes("ISO-8859-1"), encoding);
//				} catch (UnsupportedEncodingException e1) {
//					e1.printStackTrace();
//				}
//				valideData.put(key, value);
//			}
//
//		}
		
		PaymentBean payment = payTypeService.getPayType(orderFormUnpay.getPaytype());
		
		RSASig rsa = new RSASig();
//		String strSrc = req.getParameter("src");//返回连接
		String strSrc = "POSID="+reqParam.get("POSID")+"&BRANCHID="+reqParam.get("BRANCHID")+"&ORDERID="+reqParam.get("ORDERID")+"&PAYMENT="+reqParam.get("PAYMENT")+
					"&CURCODE="+reqParam.get("CURCODE")+"&REMARK1=&REMARK2=&ACC_TYPE="+reqParam.get("ACC_TYPE")+
					"&SUCCESS="+reqParam.get("SUCCESS")+"&TYPE="+reqParam.get("TYPE")+"&REFERER="+(reqParam.get("REFERER")==null?"":reqParam.get("REFERER"))+"&CLIENTIP="+reqParam.get("CLIENTIP")+"&ACCDATE="+reqParam.get("ACCDATE");
		
		if(reqParam.get("USRMSG")!=null)strSrc = strSrc+"&USRMSG="+reqParam.get("USRMSG");
		logManager.debug("info","支付后台-----建行支付接收后台通知......,strSrc【"+strSrc+"】");
		
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
			logManager.debug("info","支付后台-----建行支付接收后台通知，签名验证成功......");
			strRet = "Y";
			String trade_status = (String) reqParam.get("SUCCESS");//Y：成功，N：失败
			if (trade_status.equals("Y")) {
				logManager.debug("info","支付后台-----建行支付接收后台通知，建行返回状态：SUCCESS=Y......");
				String total_fee = (String) reqParam.get("PAYMENT");
	
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
						logManager.debug("info","支付后台-----建行支付接收后台通知结束......,状态：【"+PayStatus.PAY_NOT_MATCH+"】");
						return PayStatus.PAY_NOT_MATCH;
				}
				super.afterServerNotify(orderFormUnpay,(String)reqParam.get("ORDERID"));
			}
			try {
				PrintWriter out = response.getWriter();
				out.println("success");
				out.flush();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			logManager.debug("info","支付后台-----建行支付接收后台通知结束......,状态：【"+PayStatus.PAY_SUCCESS+"】");
			return PayStatus.PAY_SUCCESS;
		}else {
			logManager.debug("info","支付后台-----建行支付接收后台通知，签名验证失败......");
			try {
				PrintWriter out = response.getWriter();
				out.println("error");
				out.flush();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			strRet = "N";
			logManager.debug("info","支付后台-----建行支付接收后台通知结束......,状态：【"+PayStatus.PAY_VALIDATE_FAIL+"】");
			return PayStatus.PAY_VALIDATE_FAIL;
			  
		}


		//LogUtil.writeLog("BackRcvResponse接收后台通知结束");
	
	}

	@Override
	public int doPaymentPageNotify(HttpServletRequest req,
			HttpServletResponse response) {
		//LogUtil.writeLog("FrontRcvResponse前台接收报文返回开始");
		logManager.debug("info","支付页面返回-----建行支付接收页面通知开始......");
		try {
			req.setCharacterEncoding("ISO-8859-1");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		String encoding = req.getParameter("encoding");
		
		logManager.debug("info","支付页面返回-----建行支付接收页面通知开始......返回报文中encoding=[" + encoding + "]");
		Map reqParam = UnionpayUtil.getAllRequestParam(req);

		String orderNum = (String) reqParam.get("ORDERID");
		ComplexOrderBean complexOrderBean = payService.queryComplexOrderBeanByOrderNumber(orderNum);
		
		OrderFormPay orderForm = complexOrderBean.getOrderform();
		if (null == orderForm) {
			logManager.debug("info","支付后台-----建行支付接收页面通知结束......,状态：【"+PayStatus.PAY_NO_ORDER+"】");
			return PayStatus.PAY_NO_ORDER;
		}

//		Map valideData = null;
//		if ((null != respParam) && (!respParam.isEmpty())) {
//			Iterator it = respParam.entrySet().iterator();
//			valideData = new HashMap(respParam.size());
//			while (it.hasNext()) {
//				Map.Entry e = (Map.Entry) it.next();
//				String key = (String) e.getKey();
//				String value = (String) e.getValue();
//				valideData.put(key, value);
//			}
//		}
		
		PaymentBean payment = payTypeService.getPayType(orderForm.getPaytype());
		
		RSASig rsa = new RSASig();
//		String strSrc = req.getParameter("src");//返回连接
		String strSrc ="POSID="+reqParam.get("POSID")+"&BRANCHID="+reqParam.get("BRANCHID")+"&ORDERID="+reqParam.get("ORDERID")+"&PAYMENT="+reqParam.get("PAYMENT")+
		"&CURCODE="+reqParam.get("CURCODE")+"&REMARK1=&REMARK2="+
		"&SUCCESS="+reqParam.get("SUCCESS")+"&TYPE="+reqParam.get("TYPE")+"&REFERER="+(reqParam.get("REFERER")==null?"":reqParam.get("REFERER"))+"&CLIENTIP="+reqParam.get("CLIENTIP")+"&ACCDATE="+reqParam.get("ACCDATE");
		
		if(reqParam.get("USRMSG")!=null)strSrc = strSrc+"&USRMSG="+reqParam.get("USRMSG");
		logManager.debug("info","支付后台-----建行支付接收页面通知......,strSrc【"+strSrc+"】");
		
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
			 String trade_status = (String) reqParam.get("SUCCESS");//Y：成功，N：失败
			if (trade_status.equals("Y")) {
				logManager.debug("info","支付后台-----建行支付接收页面通知......,建行返回状态：SUCCESS=Y");
				if (afterPageNotify(orderForm, response)) {
					logManager.debug("info","支付后台-----建行支付接收页面通知结束......,状态：【"+PayStatus.PAY_SUCCESS+"】");
					return PayStatus.PAY_SUCCESS;
				}
			}
		}else{
			logManager.debug("info","支付后台-----建行支付接收页面通知结束...验签失败...,状态：【"+PayStatus.PAY_VALIDATE_FAIL+"】");
			return PayStatus.PAY_VALIDATE_FAIL;
		}
		logManager.debug("info","支付后台-----建行支付接收页面通知结束......,状态：【"+PayStatus.PAY_FAIL+"】");
		return PayStatus.PAY_FAIL; // 请不要修改或删除
	}

	@Override
	public String refund(HttpServletRequest req,
			HttpServletResponse response) throws Exception {
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
		logManager.debug("info","查询对账信息开始......");
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
			logManager.debug("info","查询对账信息...获取支付流水信息开始...");
			List<Check> payCheck = CCBPaySubmit.checkSocketEBSS(sParaTemp, payment, "0");
			logManager.debug("info","查询对账信息...获取支付流水信息结束...");
			logManager.debug("info","查询对账信息...获取退款流水信息开始...");
			List<Check> backCheck = CCBPaySubmit.checkSocketEBSS(sParaTemp, payment, "1");
			logManager.debug("info","查询对账信息...获取退款流水信息结束...");
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
		String out_trade_no = "";
		try {
			out_trade_no = new String(request.getParameter("ORDERID").getBytes(
					"ISO-8859-1"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ComplexOrderBean complexOrderBean = payService.queryComplexOrderBeanByOrderNumber(out_trade_no);
		OrderFormUnPay orderUnpay = complexOrderBean.getOrderformunpay();
		OrderFormPay orderpay = complexOrderBean.getOrderform();
		return orderUnpay;
	}

}
