package com.pay.framework.payment.cmbpay;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cmb.netpayment.Security;
import cmb.netpayment.Settle;

import com.pay.framework.log.LogManager;
import com.pay.framework.payment.BasePay;
import com.pay.framework.payment.IPay;
import com.pay.framework.payment.alipay.Alipay;
import com.pay.framework.service.OrderFormService;
import com.pay.framework.service.PayService;
import com.pay.framework.util.DateUtil;
import com.pay.model.Check;
import com.pay.model.ComplexOrderBean;
import com.pay.model.OrderFormPay;
import com.pay.model.OrderFormUnPay;
import com.pay.model.PayStatus;
import com.pay.model.PaymentBean;
import com.pay.model.Refund;

@Component("cmbpay")

public class CmbPay extends BasePay implements IPay {
	LogManager logManager = LogManager.getLogger(Alipay.class);

	@Autowired
	private OrderFormService orderFormService;

	@Autowired
	private PayService payService;
	
	
	@Override
	public String getForwardUrl(HttpServletRequest request,
			HttpServletResponse response, int payTypeId) {
		//处理支付通用逻辑
		OrderFormUnPay orderform = CmbBeforePay(request, payTypeId);
		if (null == orderform) {
			return null;
		}
		logManager.debug("info","----招行支付----开始......");
		PaymentBean payment = payTypeService.getPayType(orderform.getPaytype());
		
//		Map<String, String> sParaTemp = new HashMap<String, String>();
//		sParaTemp.put("BranchID", payment.getExt()); //商户开户分行号，请咨询开户的招商银行分支机构；
//		sParaTemp.put("CoNo", payment.getMerchantid()); //商户号，6位数字，由银行在商户开户时确定；
//		sParaTemp.put("BillNo", orderform.getOrdernumber()); //定单号，6位或10位数字，由商户系统生成，一天内不能重复；
//		sParaTemp.put("Amount", orderform.getMoney()); //定单总金额，格式为：xxxx.xx元；
//		sParaTemp.put("Date",  orderform.getSubmitdate()); //交易日期，格式：YYYYMMDD。
//		sParaTemp.put("MerchantUrl", payment.getBgurl()); //后台通知url
//		sParaTemp.put("MerchantReturnUrl", payment.getPageurl());//前端通知
		String date = orderform.getSubmitdate().replaceAll("-", "").substring(0,8);
		
		String MerchantCode = CmbPaySubmit.buildRequestString(payment.getSeckey(), date, payment.getExt(),  payment.getMerchantid(), 
				orderform.getOrdernumber(),  orderform.getMoney(), "", payment.getBgurl(), orderform.getUserid(), payment.getMerchantid(), "", "54011600", "");
		logManager.debug("info","----招行支付----开始...MerchantCode.."+MerchantCode);
		
//		sParaTemp.put("MerchantCode", MerchantCode);

		String sHtmlText = payment.getPosturl()+"BranchID="+payment.getExt()+"&CoNo="+ payment.getMerchantid()+"&BillNo="+orderform.getOrdernumber()+"&Amount="+orderform.getMoney()+"&Date="+date+"&MerchantUrl="+payment.getBgurl()+"&MerchantRetUrl="+payment.getPageurl()+"&MerchantCode="+MerchantCode;
//		String sHtmlText = payment.getPosturl()+"BranchID="+payment.getExt()+"&CoNo="+ payment.getMerchantid()+"&BillNo="+orderform.getOrdernumber()+"&Amount="+orderform.getMoney()+"&Date="+orderform.getSubmitdate()+"&MerchantUrl="+payment.getPageurl()+"&MerchantCode="+MerchantCode;
		logManager.debug("info","----招行支付----开始...payurl.."+sHtmlText);
		return sHtmlText;

	}




	/*  
	 *  页面通知处理方法
	 * (non-Javadoc)
	 * @see com.pay.framework.payment.IPay#doPaymentPageNotify(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public int doPaymentPageNotify(HttpServletRequest request,
			HttpServletResponse response) {
	//	int flag = doPaymentServerNotify(request, response);
	//	logManager.debug("info","----招行支付----接收服务端通知结束....serviceflag="+flag);
		logManager.debug("info","----招行支付----接收页面通知开始......");
		try {
			request.setCharacterEncoding("ISO-8859-1");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String BillNo = request.getParameter("BillNo");//定单号(由支付命令送来)
		logManager.debug("info","----招行支付----接收页面通知..BillNo=="+BillNo);
		ComplexOrderBean complexOrderBean =  BillNo==null?null:payService.queryComplexOrderBeanByOrderNumber(BillNo);
		
		OrderFormPay orderForm = complexOrderBean.getOrderform();
		if (null == orderForm) {
			logManager.debug("info","----招行支付-----接收页面通知结束......,状态：【"+PayStatus.PAY_NO_ORDER+"】");
			return PayStatus.PAY_NO_ORDER;
		}
		PaymentBean payment = payTypeService.getPayType(orderForm.getPaytype());
		
		String Succeed = request.getParameter("Succeed");
		String Amount = request.getParameter("Amount");//实际支付金额(由支付命令送来)
		String Date = request.getParameter("Date");//(商户送过来的定单中的日期)
//		Msg:银行通知用户的支付结果消息。信息的前38个字符格式为：4位分行号＋6位商户号＋8位银行接受交易的日期＋20位银行流水号；可以利用交易日期＋银行流水号对该定单进行结帐处理；
		String Msg = request.getParameter("Msg");
		String Signature = request.getParameter("Signature");
		
//		验证签名
		boolean bRet = false;
		try{
			cmb.netpayment.Security pay = new cmb.netpayment.Security(payment.getExtCol1());
	        String params = "Succeed="+Succeed+"&BillNo="+BillNo+"&Amount="+Amount+"&Date="+Date+"&Msg="+Msg+"&Signature="+Signature;
	        byte[] baSig =params.getBytes("GB2312");
	        logManager.debug("info","---招行B2C支付----接收页面通知...验签参数串.."+params);
	        bRet = pay.checkInfoFromBank(baSig);
		}catch(Exception e){
			e.printStackTrace();
		}
		if (bRet) {
			if (Succeed.equals("Y")) {
				logManager.debug("info","----招行支付----接收页面通知......,返回状态：SUCCESS=Y");
				if (afterPageNotify(orderForm, response)) {
					logManager.debug("info","----招行支付-----接收页面通知结束......,状态：【"+PayStatus.PAY_SUCCESS+"】");
					return PayStatus.PAY_SUCCESS;
				}
			}
		}else{
			logManager.debug("info","----招行支付-----接收页面通知结束...验签失败...,状态：【"+PayStatus.PAY_VALIDATE_FAIL+"】");
			return PayStatus.PAY_VALIDATE_FAIL;
		}
		logManager.debug("info","----招行支付-----接收页面通知结束......,状态：【"+PayStatus.PAY_FAIL+"】");

		return PayStatus.PAY_FAIL; // 请不要修改或删除
	 
	}

	/*
	 * 后台通知执行方法
	 * (non-Javadoc)
	 * @see com.pay.framework.payment.IPay#doPaymentServerNotify(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public synchronized int doPaymentServerNotify(HttpServletRequest request,
			HttpServletResponse response) {
		
		
		logManager.debug("info","---招行B2C支付----接收后台通知开始......");
		try {
			request.setCharacterEncoding("ISO-8859-1");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}


		String BillNo = request.getParameter("BillNo");//定单号(由支付命令送来)
		logManager.debug("info","---招行B2C支付----接收后台通知开始...BillNo="+BillNo);
		OrderFormUnPay orderFormUnpay = BillNo==null?null:orderFormService.queryOrderFormUnPaymentByOrderNumber(BillNo);
		if (orderFormUnpay == null) {
			try {
				PrintWriter out = response.getWriter();
				out.println("<meta name=\"CMBNETPAYMENT\" content=\"China Merchants Bank\">");
				out.println("success");
				out.flush();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			logManager.debug("info","---招行B2C支付----接收后台通知结束......,状态：【"+PayStatus.PAY_NO_ORDER+"】");
			return PayStatus.PAY_NO_ORDER;
		}
		
		String Succeed = request.getParameter("Succeed");
		String Amount = request.getParameter("Amount");//实际支付金额(由支付命令送来)
		String Date = request.getParameter("Date");//(商户送过来的定单中的日期)
//		Msg:银行通知用户的支付结果消息。信息的前38个字符格式为：4位分行号＋6位商户号＋8位银行接受交易的日期＋20位银行流水号；可以利用交易日期＋银行流水号对该定单进行结帐处理；
		String Msg = request.getParameter("Msg");
		String Signature = request.getParameter("Signature");

		
		PaymentBean payment = payTypeService.getPayType(orderFormUnpay.getPaytype());
		
		
//		验证签名
		boolean bRet = false;
		try{
			cmb.netpayment.Security pay = new cmb.netpayment.Security(payment.getExtCol1());
	        String params = "Succeed="+Succeed+"&BillNo="+BillNo+"&Amount="+Amount+"&Date="+Date+"&Msg="+Msg+"&Signature="+Signature;
	        byte[] baSig =params.getBytes("GB2312");
	        logManager.debug("info","---招行B2C支付----接收后台通知...验签参数串.."+params);
	        bRet = pay.checkInfoFromBank(baSig);
		}catch(Exception e){
			e.printStackTrace();
		}
		
//		try {
//			if (StringUtils.isNotEmpty(Msg)) {
//				String body = URLDecoder.decode(Msg, "utf-8");
//				request.setAttribute("Msg", body);
//				order_transeq = body.substring(17); //交易流水
//			} else {
//				request.setAttribute("Msg", "");
//			}
//
//		} catch (UnsupportedEncodingException e1) {
//			e1.printStackTrace();
//		}


		if (bRet) {// 验证成功

			logManager.debug("info","---招行B2C支付----接收后台通知，签名验证成功......");
			if (Succeed.equals("Y")) {
				logManager.debug("info","---招行B2C支付----接收后台通知，返回状态：SUCCESS=Y......");
	
				// 判断金额
				if ((new java.math.BigDecimal(Amount)
							.multiply(new java.math.BigDecimal(100)).intValue()) < (new java.math.BigDecimal(
							orderFormUnpay.getPrice()).intValue())) {
						try {
							PrintWriter out = response.getWriter();
							out.println("<meta name=\"CMBNETPAYMENT\" content=\"China Merchants Bank\">");
							out.println("fail");
							out.flush();
							out.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						logManager.debug("info","---招行B2C支付----接收后台通知结束......,状态：【"+PayStatus.PAY_NOT_MATCH+"】");
						return PayStatus.PAY_NOT_MATCH;
				}
				super.afterServerNotify(orderFormUnpay,BillNo);
			}
			try {
				PrintWriter out = response.getWriter();
				out.println("<meta name=\"CMBNETPAYMENT\" content=\"China Merchants Bank\">");
				out.println("success");
				out.flush();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			logManager.debug("info","---招行B2C支付----接收后台通知结束......,状态：【"+PayStatus.PAY_SUCCESS+"】");
			return PayStatus.PAY_SUCCESS;
		} else {// 验证失败
			logManager.debug("info","---招行B2C支付----接收后台通知，签名验证失败......");
			try {
				PrintWriter out = response.getWriter();
				out.println("<meta name=\"CMBNETPAYMENT\" content=\"China Merchants Bank\">");
				out.println("fail");
				out.flush();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			logManager.debug("info","---招行B2C支付----接收后台通知结束......,状态：【"+PayStatus.PAY_VALIDATE_FAIL+"】");
			return PayStatus.PAY_FAIL;
		}
	}

	
	@Override
	public OrderFormUnPay getOrderFormUnPay(HttpServletRequest request,
			HttpServletResponse response) {
		String out_trade_no = "";
		try {
			out_trade_no = new String(request.getParameter("BillNo").getBytes(
					"ISO-8859-1"), "UTF-8");
			logManager.debug("info", "getOrderFormUnPay()--->out_trade_no=="+out_trade_no);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ComplexOrderBean complexOrderBean = payService.queryComplexOrderBeanByOrderNumber(out_trade_no);
		OrderFormUnPay orderUnpay = complexOrderBean.getOrderformunpay();
		OrderFormPay orderpay = complexOrderBean.getOrderform();
		return orderUnpay;
	}
	/*
	 * 对账
	 * (non-Javadoc)
	 * @see com.pay.framework.payment.IPay#check(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, int)
	 */

	
	@Override
	public List<Check> check(HttpServletRequest request,
			HttpServletResponse response, int payTypeId) throws Exception {
		
		if (!beforeCheck(request)) {
			return null;
		}
		logManager.debug("info","--招行--查询对账信息开始......");
		String orderDate = (String)request.getAttribute("starttime");
		orderDate = orderDate.replace("-", "");
		PaymentBean payment = payTypeService.getPayType(payTypeId);
		
		List<Check> resultcheck = new ArrayList<Check>();
		
		Settle settle=new Settle();
		int iRet;
		StringBuffer strbuf = new StringBuffer();
    	iRet = settle.SetOptions("payment.ebank.cmbchina.com");
   		if (iRet != 0){
   			logManager.debug("info","--招行--settle.SetOptions出错："+settle.GetLastErr(iRet));
			return null;
		}
    	iRet = settle.LoginC(payment.getExt(),payment.getMerchantid(),payment.getMerchantpwd());
    	if (iRet != 0){
			logManager.debug("info","--招行--settle.LoginC出错："+settle.GetLastErr(iRet));
			return null;
		}
        settle.PageReset();
        logManager.debug("info","--招行--查询支付对账文件。。。。");
        do{
//        	分页方式按交易日查询已结帐定单（入账明细查询）。
    	    iRet = settle.QuerySettledOrderByPage(orderDate,orderDate,100,strbuf);
        } while (iRet==0 && !settle.m_bIsLastPage);
        if (iRet != 0){
			logManager.debug("info","--招行--查询支付对账文件出错："+settle.GetLastErr(iRet));
			return null;
		}
        
		String[] checkstrs ={""};
		if(strbuf!=null&&strbuf.length()>0)checkstrs = strbuf.toString().split("\n");
		 logManager.debug("info","--招行--支付对账文件数组==checkstrs::"+Arrays.toString(checkstrs)+"the length is:"+checkstrs.length);
    	if(checkstrs!=null&&checkstrs.length>=9){
//  i =  	0		1		2	  3		  4			5		6		7			8
//    	交易日期\n处理日期\n金额\n定单号\n订单状态\n卡类型\n手续费\n银行受理日期\n银行受理时间\n
//    	[20150325, 20150325, 0.01, 6508184863, 0, 02, 0.00, 20150325, 161350]
    		int rows = checkstrs.length/9;
    		for(int i =0;i<rows;i++){
    			Check check = new Check();
    			BigDecimal fee = "0.00".equals(checkstrs[i*9+6])?new BigDecimal("0.00"):new BigDecimal("-"+checkstrs[i*9+6]);
    			BigDecimal natvalue = new BigDecimal(checkstrs[i*9+2]).add(fee);
    			String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new SimpleDateFormat("yyyyMMddHHmmss").parse(checkstrs[i*9+7]+checkstrs[i*9+8]));
    			check.setAccountname("");
    			check.setAccountno("");
    			check.setBank("CMB");
    			check.setFee(fee.toString());
    			check.setNetvalue(natvalue.toString());
    			check.setOrdernumber(checkstrs[i*9+3]);
    			check.setPaytype(payTypeId+"");
    			check.setPrice(checkstrs[i*9+2]);
    			check.setTransdate(date);
    			check.setTranseq("");
    			OrderFormPay orderFormPay = payService.getOrderFormByOrderNumPay(checkstrs[i*9+3]);
	    		if (orderFormPay != null) {
	    			check.setCorderid(orderFormPay.getCorderid());
				}
	    		resultcheck.add(check);
    		}
    	}
    	
    	settle.PageReset();
		StringBuffer strbuf2 = new StringBuffer();
		logManager.debug("info","--招行--查询退款对账文件....");
		do{
//			查询退款账单明细
			iRet = settle.QueryRefundByDatePage(orderDate, orderDate, 100, strbuf2);
		} while (iRet==0 && !settle.m_bIsLastPage);
		if (iRet != 0){
			logManager.debug("info","--招行--查询退款对账文件出错："+settle.GetLastErr(iRet));
			return null;
		}
		logManager.debug("info","--招行--招行退款文件==."+strbuf2);
    	String[] checkrefunds ={""};
		if(strbuf2!=null&&strbuf2.length()>0)checkrefunds = strbuf2.toString().split("\n");
		 logManager.debug("info","--招行--退款对账文件数组==checkrefunds::"+Arrays.toString(checkrefunds)+"the length is:"+checkrefunds.length);
    	if(checkrefunds!=null&&checkrefunds.length>=12){
//   	0		1			2			3			4			5			6		7			8			9			10			11		12
//退款单流水号\n商户定单号\n定单参考号\n商户定单日期\n退款币种\n退款金额\n费用金额\n银行受理日期\n银行受理时间\n经办操作员号\n退款日期\n退款时间\n退款说明\n
//    	0		1			2			3			4			5			6		7			8			9		10			11					
//退款单流水号\n商户定单号\n定单参考号\n商户定单日期\n退款币种\n退款金额\n费用金额\n银行受理时间\n经办操作员号\n退款日期\n退款时间\n退款说明\n
    		int rows = (checkrefunds.length+1)/13;
    		for(int i =0;i<rows;i++){
    			Check check = new Check();
    			BigDecimal natvalue = new BigDecimal("-"+checkrefunds[i*13+5]).add(new BigDecimal(checkrefunds[i*13+6]));
    			String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new SimpleDateFormat("yyyyMMddHHmmss").parse(checkrefunds[i*13+7]+checkrefunds[i*13+8]));
    			check.setAccountname("");
    			check.setAccountno("");
    			check.setBank("CMB");
    			check.setFee(checkrefunds[i*13+6]);
    			check.setNetvalue(natvalue.toString());
    			check.setOrdernumber(checkrefunds[i*13+1]);
    			check.setPaytype(payTypeId+"");
    			check.setPrice("-"+checkrefunds[i*13+5]);
    			check.setTransdate(date);
    			check.setTranseq(checkrefunds[i*13+0]);
    			OrderFormPay orderFormPay = payService.getOrderFormByOrderNumPay(checkrefunds[i*13+1]);
	    		if (orderFormPay != null) {
	    			check.setCorderid(orderFormPay.getMemo());//如果是退款金额，则取退款的应收单
				}
	    		resultcheck.add(check);
    		}	
    	}
    	settle.Logout();
    	logManager.debug("info","--招行--查询对账信息结束......银行交易个数="+resultcheck.size());
		return resultcheck;
	}
	/*
	 * 退款实现方法
	 * (non-Javadoc)
	 * @see com.pay.framework.payment.IPay#refund(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public String refund(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
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
		
		Settle settle=new Settle();
		int iRet;
    	iRet = settle.SetOptions("payment.ebank.cmbchina.com");
   		if (iRet != 0){
   			logManager.debug("info","--招行退款--settle.SetOptions出错："+settle.GetLastErr(iRet)+".....");
   			json.put("status", statu);
			json.put("errormsg", settle.GetLastErr(iRet));
			return json.toString();
		}
    	iRet = settle.LoginC(payment.getExt(),payment.getMerchantid(),payment.getMerchantpwd());
    	if (iRet != 0){
			logManager.debug("info","--招行退款--settle.LoginC出错："+settle.GetLastErr(iRet)+".....");
			json.put("status", statu);
			json.put("errormsg", settle.GetLastErr(iRet));
			return json.toString();
		}
    	BigDecimal price = new BigDecimal(refund.getRefundmoney()).divide(new BigDecimal(100));
    	OrderFormPay orderFormPay = payService.getOrderFormByOrderNumPay(refund.getOrdernumber());
    	String transdate=orderFormPay.getPaymentdate();
    	Date txd=DateUtil.parseDate(transdate);
    	String txDate=DateUtil.format(txd, DateUtil.C_DATA_PATTON_YYYYMMDD);
    	iRet = settle.RefundOrder(txDate,refund.getOrdernumber(),price+"","",payment.getSeckey());
    	logManager.debug("招行退款订单号及金额为"+refund.getOrdernumber(), price+""+"交易日期"+txDate);
    	if (iRet != 0){
			logManager.debug("info","--招行退款--退款失败："+settle.GetLastErr(iRet)+".....");
			errorMsg = settle.GetLastErr(iRet);
		}else{
			afterRefund(refund.getOrdernumber());
			orderFormService.updatePayStatus(refund.getOrdernumber(), PayStatus.PAY_SUCCESS_REFUND);
			statu = PayStatus.PAY_SUCCESS;
			settle.Logout();
		}
		
		json.put("status", statu);
		json.put("errormsg", errorMsg);
		json.put("corderid", refund.getCorderid());
		json.put("money", refund.getRefundmoney());
	    return json.toString();
	}
    
	/*
	 *  退款通知
	 * (non-Javadoc)
	 * @see com.pay.framework.payment.IPay#refundNotify(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public int refundNotify(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}
	/*
	 * 验证通知有效性
	 */
	public static boolean verify(String ckString){
		boolean temp = true;
		try {
			Security secutity = new Security("");
			temp=secutity.checkInfoFromBank(ckString.getBytes("GB2312"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return temp;
	}
	

	

}
