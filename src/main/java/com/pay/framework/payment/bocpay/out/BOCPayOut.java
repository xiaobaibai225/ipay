package com.pay.framework.payment.bocpay.out;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.cxf.service.invoker.SpringBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pay.framework.http.HttpRequester;
import com.pay.framework.http.HttpResponse;
import com.pay.framework.log.LogManager;
import com.pay.framework.payment.BasePay;
import com.pay.framework.payment.IPay;
import com.pay.framework.payment.bocpay.out.util.SHA256Util;
import com.pay.framework.service.OrderFormService;
import com.pay.framework.service.PayService;
import com.pay.model.Check;
import com.pay.model.ComplexOrderBean;
import com.pay.model.OrderFormPay;
import com.pay.model.OrderFormUnPay;
import com.pay.model.PayStatus;
import com.pay.model.PaymentBean;
import com.pay.model.Refund;
import com.sun.org.apache.bcel.internal.generic.RETURN;

@Component("bocpayout")
public class BOCPayOut extends BasePay implements IPay {

	static LogManager logManager = LogManager.getLogger(BOCPayOut.class);
	
	@Autowired
	private OrderFormService orderFormService;
	
	@Autowired
	private PayService payService;
	
	@Override
	public String getForwardUrl(HttpServletRequest request,
			HttpServletResponse response, int payTypeId) {
		logManager.debug("进入中行外卡支付", "进入中行外卡支付。。。。payTypeId="+payTypeId);
		OrderFormUnPay orderForm = BocOutBeforePay(request, payTypeId);
		if(orderForm == null){
			return null;
		}
		String createHtml = null;
		try{
			PaymentBean payment = payTypeService.getPayType(orderForm.getPaytype());
			//将请求参数打包
			Map<String,String> m = new HashMap<String, String>();
			m.put("Merchant_Id", payment.getMerchantid());
			m.put("Trans_Type", payment.getExt3());
			m.put("Author_Str", payment.getExt1());
			m.put("Card_Type", orderForm.getInternationalCardType());
			m.put("Order_No", orderForm.getOrdernumber());
			m.put("Currency_Code_T", "156");
			m.put("Amount_Loc", orderForm.getPrice()+"");
			m.put("Custom", "SOHO3Q");
			m.put("bocs_ReturnURL",	payment.getBgurl());
			m.put("end_ReturnURL", payment.getPageurl());
			logManager.debug("中行外卡支付", "HASH key："+payment.getExt2());
			logManager.debug("中行外卡支付", "中行外卡支付SHA256："+SHA256Util.hashAllFields(m, payment.getExt2()));
			m.put("HASH", SHA256Util.hashAllFields(m, payment.getExt2()));
			logManager.debug("中行外卡跳转到支付页面请求参数", "中行外卡跳转到支付页面请求参数："+m);
			logManager.debug("中行外卡支付请求页面URL", "中行外卡支付请求页面URL："+payment.getPosturl());
			createHtml = BOCPayOutSubmit.creatHtml(payment.getPosturl(), m);
			logManager.debug("中行外卡建立请求", "中行外卡建立请求："+createHtml);																	
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		return createHtml;
	}

	
	@Override
	public int doPaymentPageNotify(HttpServletRequest request,
			HttpServletResponse response) {
		logManager.debug("中行外卡页面通知", "中行外卡页面通知开始。。。");
		try{
			request.setCharacterEncoding("UTF-8");
		}catch(Exception e){
			e.printStackTrace();
		}
		Map<String, String> reqParam = BOCPayOutUtil.getRequestParameter(request);
		logManager.debug("中行外卡支付页面通知", "页面通知返回参数：" + reqParam);
		
		String Order_No = (String) reqParam.get("Order_No");
		logManager.debug("中行外卡页面通知", "返回订单编号：" + Order_No);
		ComplexOrderBean complexOrderBean = payService.queryComplexOrderBeanByOrderNumber(Order_No);
		OrderFormPay orderForm = complexOrderBean.getOrderform();
		PaymentBean payment = payTypeService.getPayType(orderForm.getPaytype());
		if (null == orderForm) {
			logManager.debug("中行外卡页面通知","支付后台-----中行外卡支付接收页面通知结束......,状态：【"+PayStatus.PAY_NO_ORDER+"】");
			return PayStatus.PAY_NO_ORDER;
		}
		String HASH = (String) reqParam.get("HASH");
		logManager.debug("中行外卡页面通知", "返回SHA256："+HASH);
		reqParam.remove("HASH");
		if(HASH.equals(SHA256Util.hashAllFields(reqParam,payment.getExt2()))){
			String Resp_Code = (String) reqParam.get("Resp_Code");
			logManager.debug("中行外卡支付页面通知", "返回码：" + Resp_Code);
			if(Resp_Code.equals("0000")){
				logManager.debug("中行外卡支付","支付后台-----中行支外卡付接收页面通知......,中行返回状态：Resp_Code=0000");
				if (afterPageNotify(orderForm, response)) {
					logManager.debug("中行外卡支付","支付后台-----中行外卡支付接收页面通知结束......,状态：【"+PayStatus.PAY_SUCCESS+"】");
					return PayStatus.PAY_SUCCESS;
				}
			}
		}else{
			logManager.debug("中行外卡支付","中行外卡验签失败...,状态：【"+PayStatus.PAY_VALIDATE_FAIL+"】");
			return PayStatus.PAY_VALIDATE_FAIL;
		}
		
		return PayStatus.PAY_FAIL;
	}

	@Override
	public int doPaymentServerNotify(HttpServletRequest request,
			HttpServletResponse response) {
		logManager.debug("doPaymentServerNotify", "进入中行外卡主动通知。。");
		try{
			request.setCharacterEncoding("UTF-8");
		}catch(Exception e){
			e.printStackTrace();
		}
		try{
			Map<String, String> reqParam = BOCPayOutUtil.getRequestParameter(request);
			logManager.debug("中行外卡主动通知", "主动通知参数："+reqParam.toString());
			String orderNo = (String) reqParam.get("Order_No");
			logManager.debug("中行外卡主动通知", "订单号："+orderNo);
			
			String Merchant_Id = (String) reqParam.get("Merchant_Id");
			String Trans_Type = (String) reqParam.get("Trans_Type");
			String Author_Str = (String) reqParam.get("Author_Str");
			String Card_Type = (String) reqParam.get("Card_Type");
			String Order_No = (String) reqParam.get("Order_No");
			String Resp_Code = (String) reqParam.get("Resp_Code");
			String Currency_Code_T = (String) reqParam.get("Currency_Code_T");
			String Auth_Code = (String) reqParam.get("Auth_Code");
			String Amount_Loc = (String) reqParam.get("Amount_Loc");
			String Custom = (String) request.getParameter("Custom");
			String Ref_No = (String) reqParam.get("Ref_No");
			
			
			if (Resp_Code == null || "".equals(Resp_Code.trim())) {
				return PayStatus.PAY_FAIL;
			} else {
				Resp_Code = Resp_Code.trim();
			}
			if (!"0000".equals(Resp_Code)) {
				logManager.debug("中行外卡支付主动通知", "支付失败");
				return PayStatus.PAY_FAIL;
			}
			logManager.debug("中行外卡支付主动通知", "扣款成功");
			
			OrderFormUnPay orderFormUnpay = orderFormService.queryOrderFormUnPaymentByOrderNumber(orderNo);
			
			if(orderFormUnpay==null){
				logManager.debug("中行外卡支付主动通知","支付后台-----接收后台通知结束......,状态：【"+PayStatus.PAY_NO_ORDER+"】");
				return PayStatus.PAY_NO_ORDER;
			}
			PaymentBean paymentBean = payTypeService.getPayType(orderFormUnpay.getPaytype());
			String hash = (String) reqParam.get("HASH");
			logManager.debug("中行外卡支付主动通知", "返回的签名数据："+hash);
			reqParam.remove("HASH");
			if(!hash.equals(SHA256Util.hashAllFields(reqParam, paymentBean.getExt2()))){
				logManager.debug("中行外卡支付主动通知","支付后台-----接收后台通知结束......,状态：【验签失败!!】");
				return PayStatus.PAY_VALIDATE_FAIL;
			}
			
			if(Order_No==null || "".equals(Order_No.trim())){
				return PayStatus.PAY_NO_ORDER;
			}else{
				Order_No = Order_No.trim();
			}
			if (Amount_Loc == null || "".equals(Amount_Loc.trim())) {
				return PayStatus.PAY_FAIL;
			} else {
				Amount_Loc = Amount_Loc.trim();
			}
			BigDecimal transactionAmount = new BigDecimal(Amount_Loc);
			transactionAmount = transactionAmount.divide(new BigDecimal(100));
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
				logManager.debug("中行外卡支付", "更改支付表开始。。。");
				boolean result = super.afterServerNotify(orderFormUnpay, Ref_No);
				logManager.debug("中行外卡支付", "更改結果："+result);
			}
			return PayStatus.PAY_SUCCESS;
		}catch(Exception e){
			e.printStackTrace();
		}
		return PayStatus.PAY_FAIL;
	}

	
	@Override
	public String refund(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		logManager.debug("中行外卡退款", "中行外卡退款开始了。。。");
		Refund refund = super.bocOutBeforeRefund(request);
		JSONObject json = new JSONObject();
		int status = PayStatus.REFUND_FAIL;
		String errorMsg = "";
		if(refund==null){
			json.put("status", status);
			json.put("errorMsg", errorMsg);
			return json.toString();
		}
		
		logManager.debug("中行外卡退款", "PaytypeId："+refund.getPaytype());
		PaymentBean payment = payTypeService.getPayType(refund.getPaytype());
		Map<String,String> params = new HashMap<String, String>();
		params.put("Merchant_Id", payment.getMerchantid());
		OrderFormPay orderFormPay = orderFormService.getOrderByOrderNum(refund.getOrdernumber());
		String paymentdate = orderFormPay.getPaymentdate();
		Timestamp nowDate = new Timestamp(System.currentTimeMillis());
		int i= compare_date(paymentdate, nowDate.toString());
		if(i==1){
			params.put("Trans_Type", "void");
		}else{
			params.put("Trans_Type", "refund");
		}
		params.put("Author_Str", payment.getExt1());
		params.put("Currency_Code_T", "156");
		params.put("Order_No", refund.getOuterfundno());
		params.put("Order_No_Ori", refund.getOrdernumber());
		params.put("Amount_Ori", refund.getTotalmoney()+"");
		params.put("Amount_Loc", refund.getRefundmoney()+"");
		params.put("Custom", "SOHO3Q");
		logManager.debug("中行外卡退款", "外卡退款SHA256签名："+SHA256Util.hashAllFields(params, payment.getExt2()));
		params.put("HASH", SHA256Util.hashAllFields(params, payment.getExt2()));
		
		//String result = null;
		try{
			String urlString = payment.getPosturl();
			//HttpsRequest httpsReq=new HttpsRequest(payment);
			//result = httpsReq.sendPost(urlString, xmlObj, payment);
			logManager.debug("中行外卡退款", "中行外卡退款URL："+ urlString);
			logManager.debug("中行外卡退款", "中行外卡退款参数："+ params);
			HttpRequester httpRequester = new HttpRequester();
			HttpResponse httpResponse = httpRequester.sendPost(urlString, params);
			//BOCPayOutSubmit.creatRefundHtml(urlString, params);
			logManager.debug("中行外卡退款", "中行外卡退款发送退款请求。。。");
			if(httpResponse.getCode()==200){
				logManager.debug("中行外卡退款", "中行外卡退款请求成功。。。");
				String flag = dealRefundNotify(request, response, httpResponse, payment,refund);
				if("3".equals(flag)){
					errorMsg = "send req success";
					afterRefund(refund.getOrdernumber());
					orderFormService.updatePayStatus(refund.getOrdernumber(), PayStatus.PAY_SUCCESS_REFUND);
					status = PayStatus.PAY_SUCCESS_REFUND;
					logManager.debug("中行外卡退款", "退款成功，通知成功");
				}else if("2".equals(flag)){
					errorMsg = "send req success,not match";
					afterRefund(refund.getOrdernumber());
					orderFormService.updatePayStatus(refund.getOrdernumber(), PayStatus.PAY_SUCCESS_REFUND);
					status = PayStatus.PAY_SUCCESS_REFUND;
					logManager.debug("中行外卡退款", "退款成功，金额不同");
				}else if("1".equals(flag)){
					json.put("status", status);
					json.put("errormsg", "send signature fail");
					logManager.debug("中行外卡退款", "验签失败");
					return json.toString();
				}else{
					json.put("status", status);
					json.put("errormsg", "send req fail");
					logManager.debug("中行外卡退款", "退款失败");
				}
			}else{
				json.put("status", status);
				json.put("errormsg", "send req fail");
				logManager.debug("中行外卡退款", "请求失败");
				return json.toString();
			}
		}catch(Exception e){
			json.put("status", status);
			json.put("errormsg", "send req exception");
			logManager.debug("中行外卡退款", "退货异常");
			e.printStackTrace();
			return json.toString();
		}
		//Map<String,String> reqParam = BOCPayOutUtil.getRequestParameter(request);
		/*logManager.debug("中行外卡支付请求页面URL", "中行外卡退款请求页面URL："+payment.getExt());
		String createRufundHtml = BOCPayOutSubmit.creatRefundHtml(payment.getExt(), map);
		logManager.debug("中行外卡退款", "外卡退款请求："+createRufundHtml);*/
		json.put("status", status);
		json.put("errormsg", errorMsg);
		json.put("corderid", refund.getCorderid());
		json.put("money", refund.getRefundmoney());
		return json.toString();
	}

	public String dealRefundNotify(HttpServletRequest request, HttpServletResponse response, 
			HttpResponse httpResponse,PaymentBean payment, Refund refund){
		String content = httpResponse.getContent();
		String flag = "0";
		logManager.debug("中行外卡退款", "content :"+ content);
		Map map = BOCPayOutUtil.readStringOut(content);
		logManager.debug("中行外卡退款", "中行外卡退款返回信息："+map.toString());
		String Resp_Code = (String) map.get("Resp_Code");
		if(Resp_Code.equals("0000")){
			flag="3";
			logManager.debug("中行外卡退款", "中行外卡退款成功");
			String hash = (String) map.get("HASH");
			logManager.debug("中行外卡退款", "返回参数hash："+hash);
			map.remove("HASH");
			logManager.debug("中行外卡退款", "验签HASH："+SHA256Util.hashAllFields(map, payment.getExt2()));
			if(SHA256Util.hashAllFields(map, payment.getExt2()).equals(hash)){
				logManager.debug("中行外卡退款", "中行外卡退款验签成功");
				if(map.get("Amount_Loc").equals(refund.getRefundmoney()+"")){
					logManager.debug("中行外卡退款", "中行外卡退款成功，退款金额相同！");
					flag="3";
				}else{
					logManager.debug("中行外卡退款", "中行外卡退款成功，退款金额不相同");
					flag="2";
				}
			}else{
				//logManager.debug("中行外卡退款", "中行外卡退款验签失败");
				//flag = "1";
			}
		}else{
			return flag;
		}
		return flag;
	}
	
	@Override
	public int refundNotify(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		return PayStatus.REFUND_SUCCESS;
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
	
	
	public static int compare_date(String DATE1, String DATE2) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date dt1 = df.parse(DATE1);
            Date dt2 = df.parse(DATE2);
            if (dt1.getTime() == dt2.getTime()) {
                return 1;
            } else if (dt1.getTime() < dt2.getTime()) {
                
                return 0;
            } else {
                return -1;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return -1;
    }
}
