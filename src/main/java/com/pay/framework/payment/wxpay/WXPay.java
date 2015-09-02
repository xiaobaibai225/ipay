package com.pay.framework.payment.wxpay;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pay.framework.log.LogManager;
import com.pay.framework.payment.BasePay;
import com.pay.framework.payment.ICodePay;
import com.pay.framework.payment.IMobilePay;
import com.pay.framework.payment.IPay;
import com.pay.framework.payment.unionpay.util.DateStyle;
import com.pay.framework.service.OrderFormService;
import com.pay.framework.service.PayService;
import com.pay.model.Check;
import com.pay.model.OrderFormPay;
import com.pay.model.OrderFormUnPay;
import com.pay.model.PayStatus;
import com.pay.model.PaymentBean;
import com.pay.model.Refund;
import com.thoughtworks.xstream.io.StreamException;

@Component("wxpay")
public class WXPay extends BasePay implements IPay, IMobilePay, ICodePay{
	
	LogManager logManager = LogManager.getLogger(WXPay.class);
	
	@Autowired
	private OrderFormService orderFormService;
	
	@Autowired
	private PayService payService;

	@Override
	public String getForwardUrl(HttpServletRequest request,
			HttpServletResponse response, int payTypeId) {
		logManager.debug("*************微信网页扫码支付请求开始*****************",null);
		//处理支付通用逻辑
		OrderFormUnPay orderform = beforePay(request, payTypeId);
		if (null == orderform) {
			return null;
		}
		PaymentBean pay = payTypeService.getPayType(orderform.getPaytype());
		WXPostPO po=new WXPostPO(pay,orderform);
		String result=null;
		try {
			HttpRequest req=new HttpRequest();
			result=req.sendPost(pay.getPosturl(), po);
			logManager.debug("微信网页扫码请求返回的xml串味", result);
			WXReturnPO ro= (WXReturnPO)Util.getObjectFromXML(result, WXReturnPO.class);
	        if (ro == null || ro.getReturn_code() == null || ro.getResult_code()==null) {
	        	logManager.debug("【微信支付请求失败】支付请求逻辑错误，请仔细检测传过去的每一个参数是否合法，或是看API能否被正常访问",null);
	            return null;
	        }
	        if (ro.getReturn_code().equals("FAIL") || ro.getResult_code().equals("FAIL")) {
	            //注意：一般这里返回FAIL是出现系统级参数错误，请检测Post给API的数据是否规范合法
	        	logManager.debug("【微信支付请求失败】支付API系统返回失败，请检测Post给API的数据是否规范合法",null);
	            return null;
	        }
	        else {
	        	logManager.debug("【微信支付请求成功】支付请求API成功获得数据",null);
	        	result=ro.getCode_url();
	        }
		} catch (UnrecoverableKeyException e) {
			logManager.debug(e.getMessage(),null);
		} catch (KeyManagementException e) {
			logManager.debug(e.getMessage(),null);
		} catch (NoSuchAlgorithmException e) {
			logManager.debug(e.getMessage(),null);
		} catch (KeyStoreException e) {
			logManager.debug(e.getMessage(),null);
		} catch (IOException e) {
			logManager.debug(e.getMessage(),null);
		}
//		StringBuffer postXml=new StringBuffer();
//		postXml.append("<xml>");
//		postXml.append("<appid>"+po.getAppid()+"</appid>");
//		postXml.append("<mch_id>"+po.getMch_id()+"</mch_id>");
//		postXml.append("<nonce_str>"+po.getNonce_str()+"</nonce_str>");
//		postXml.append("<sign>"+po.getNonce_str()+"</sign>");
//		postXml.append("<body>"+po.getNonce_str()+"</body>");
//		postXml.append("<out_trade_no>"+po.getNonce_str()+"</out_trade_no>");
//		postXml.append("<total_fee>"+po.getNonce_str()+"</total_fee>");
//		postXml.append("<spbill_create_ip>"+po.getNonce_str()+"</spbill_create_ip>");
//		postXml.append("<notify_url>"+po.getNonce_str()+"</notify_url>");
//		postXml.append("<trade_type>"+po.getNonce_str()+"</trade_type>");
//		postXml.append("<product_id >"+po.getNonce_str()+"</product_id >");
//		postXml.append("</xml>");
		return result;
	}

	@Override
	public int doPaymentServerNotify(HttpServletRequest request,
			HttpServletResponse response) {
		logManager.debug("*************微信网页扫码支付通知开始*****************",null);
			String payResult="";
			try {
				payResult = Util.inputStreamToString(request.getInputStream());
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			logManager.debug("微信支付通知结果", payResult);
			WXPayResultPO po=(WXPayResultPO)Util.getObjectFromXML(payResult, WXPayResultPO.class);
	        if (po == null || po.getReturn_code() == null || po.getResult_code()==null) {
	        	logManager.debug("【微信支付通知失败】支付对象为空",null);
	            return PayStatus.PAY_FAIL;
	        }
	        if (po.getReturn_code().equals("FAIL") || po.getResult_code().equals("FAIL")) {
	        	logManager.debug("【微信支付通知失败】",null);
	            return PayStatus.PAY_FAIL;
	        }
	        else {
	        	logManager.debug("【微信支付通知成功】","开始处理验证签名");
	        	String order_no=po.getOut_trade_no();
	    		OrderFormUnPay orderFormUnpay = orderFormService
	    				.queryOrderFormUnPaymentByOrderNumber(order_no);
	    		if (orderFormUnpay == null) {
	    			try {
	    				PrintWriter out = response.getWriter();
	    				out.println("<xml>");
	    				out.println("<return_code><![CDATA[SUCCESS]]></return_code>");
	    				out.println("<return_msg><![CDATA[OK]]></return_msg>");
	    				out.println("</xml>");
	    				out.flush();
	    				out.close();
	    			} catch (IOException e) {
	    				e.printStackTrace();
	    			}
	    			return PayStatus.PAY_NO_ORDER;
	    		}
	    		PaymentBean payment = payTypeService.getPayType(orderFormUnpay.getPaytype());
				String key=po.getSign();
				po.setSign(null);
				String sign="";
				try {
					sign = Signature.getSign(po,payment.getSeckey());
				} catch (IllegalAccessException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if(!key.equals(sign))
				{
					logManager.debug("【微信支付通知验签失败",null);
					try {
						PrintWriter out = response.getWriter();
	    				out.println("<xml>");
	    				out.println("<return_code><![CDATA[FAIL]]></return_code>");
	    				out.println("<return_msg><![CDATA[签名失败]]></return_msg>");
	    				out.println("</xml>");
						out.flush();
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					return PayStatus.PAY_VALIDATE_FAIL;
				}
				else
				{
					logManager.debug("【微信支付通知验签成功",null);
					super.afterServerNotify(orderFormUnpay,po.getTransaction_id());
					try {
						PrintWriter out = response.getWriter();
	    				out.println("<xml>");
	    				out.println("<return_code><![CDATA[SUCCESS]]></return_code>");
	    				out.println("<return_msg><![CDATA[OK]]></return_msg>");
	    				out.println("</xml>");
						out.flush();
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					logManager.debug("微信支付业务处理完毕",null);
					return PayStatus.PAY_SUCCESS;
				}
	        }
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
		logManager.debug("*************微信native退款请求开始*****************",null);
		Refund refund = super.beforeRefund(request);
		JSONObject json = new JSONObject();
		if (refund == null) {
			json.put("status", "0");
			json.put("errormsg", "already refund all");
			return json.toString();
		}
		PaymentBean pay = payTypeService.getPayType(refund.getPaytype());
		
		WXPostRefundPO po=new WXPostRefundPO(pay,refund);
		String result=null;
		try {
			HttpsRequest req=new HttpsRequest(pay);
			result=req.sendPost(pay.getExtCol1(), po,pay);
			logManager.debug("微信native退款请求返回的xml串味", result);
			RefundOrderData ro= (RefundOrderData)Util.getObjectFromXML(result, RefundOrderData.class);
	        if (ro == null || ro.getReturn_code() == null || ro.getResult_code()==null) {
	        	logManager.debug("【微信退款请求失败】退款请求逻辑错误，请仔细检测传过去的每一个参数是否合法，或是看API能否被正常访问",null);
				json.put("status", "0");
				json.put("errormsg", "weixin refund fail request fail");
				return json.toString();
	        }
	        if (ro.getReturn_code().equals("FAIL") || ro.getResult_code().equals("FAIL")) {
	            //注意：一般这里返回FAIL是出现系统级参数错误，请检测Post给API的数据是否规范合法
	        	logManager.debug("【微信退款请求失败】退款API系统返回失败，请检测Post给API的数据是否规范合法",null);
				json.put("status", "0");
				json.put("errormsg", "weixin refund api fail");
				return json.toString();
	        }
	        else {
	        	logManager.debug("【微信退款请求成功】退款请求API成功获得数据,开始校验签名",null);
				String key=ro.getSign();
				ro.setSign(null);
				String sign="";
				try {
					sign = Signature.getSign(ro,pay.getSeckey());
				} catch (IllegalAccessException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if(!key.equals(sign))
				{
		        	logManager.debug("【微信退款验签失败】",null);
					json.put("status", "0");
					json.put("errormsg", "weixin refund verification fail");
					return json.toString();
				}
				else
				{
					afterRefund(refund.getOrdernumber());
					orderFormService.updatePayStatus(refund.getOrdernumber(), PayStatus.PAY_SUCCESS_REFUND);
					json.put("status", PayStatus.PAY_SUCCESS);
					json.put("corderid", refund.getCorderid());
					json.put("money", refund.getRefundmoney());
					json.put("success", true);
				}
	        }
		} catch (UnrecoverableKeyException e) {
			logManager.debug(e.getMessage(),null);
			json.put("status", "0");
			json.put("errormsg", "UnrecoverableKeyException");
		} catch (KeyManagementException e) {
			logManager.debug(e.getMessage(),null);
			json.put("status", "0");
			json.put("errormsg", "KeyManagementException");
		} catch (NoSuchAlgorithmException e) {
			logManager.debug(e.getMessage(),null);
			json.put("status", "0");
			json.put("errormsg", "NoSuchAlgorithmException");
		} catch (KeyStoreException e) {
			logManager.debug(e.getMessage(),null);
			json.put("status", "0");
			json.put("errormsg", "KeyStoreException");
		} catch (IOException e) {
			logManager.debug(e.getMessage(),null);
			json.put("status", "0");
			json.put("errormsg", "IOException");
		}
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
		List<Check> list=new ArrayList<Check>();
		logManager.debug("*********微信对账交易接口开始请求**********************",null);
		String gmt_start_time = (String) request.getAttribute("starttime");
	    String  billDate= gmt_start_time.replace("-", "");//对账单日期
		PaymentBean pay = payTypeService.getPayType(payTypeId);
		WXPostBillPO po=new WXPostBillPO(pay,billDate);
		String result="";
		HttpRequest req=new HttpRequest();
		result=req.sendPost(pay.getQuerykey(), po);
		logManager.debug("微信对账交易请求返回的xml为", result);
		try
		{
		  //注意，这里失败的时候是返回xml数据，成功的时候反而返回非xml数据
			WXBillPO bo=(WXBillPO)Util.getObjectFromXML(result, WXBillPO.class);
        if (bo == null || bo.getReturn_code() == null) {
        	logManager.debug("Case1:对账单API请求逻辑错误，请仔细检测传过去的每一个参数是否合法，或是看API能否被正常访问",null);
            return list;
        }
        if (bo.getReturn_code().equals("FAIL")) {
            ///注意：一般这里返回FAIL是出现系统级参数错误，请检测Post给API的数据是否规范合法
        	logManager.debug("Case2:对账单API系统返回失败，请检测Post给API的数据是否规范合法",null);
           return list;
        }
		}catch (StreamException e) {
			 //注意，这里成功的时候是直接返回纯文本的对账单文本数据，非XML格式
            if (null==result || result.equals("")) {
            	logManager.debug("Case3:对账单API系统返回数据为空",null);
            	 return list;
            } else {
            	logManager.debug("Case4:对账单API系统成功返回数据",null);
            	String[] resArrStr = result.split("\r\n");
            	for(int i=1;i<resArrStr.length;i++)
            	{
    				String[] rd = resArrStr[i].replace("`", "").split(",");
					if (rd.length < 10) {					
						break;
					}
					Check ch=new Check();
					if(rd[9].equals("SUCCESS"))
					{
						ch.setOrdernumber(rd[6]);	
						BigDecimal price=new BigDecimal(rd[12]);
						ch.setPrice(price.toPlainString());
						ch.setTranseq(rd[5]);
						BigDecimal fee=(new BigDecimal(rd[22])).negate();
						ch.setFee(fee.toPlainString());
						ch.setNetvalue(price.add(fee).toPlainString());
					}
					else
					{
						ch.setOrdernumber(rd[15]);	
						BigDecimal price=(new BigDecimal(rd[16])).negate();
						ch.setPrice(price.toPlainString());
						ch.setTranseq(rd[14]);
						BigDecimal fee=(new BigDecimal(rd[22]));
						ch.setFee(fee.toPlainString());
						ch.setNetvalue(price.add(fee).toPlainString());
					}
					ch.setTransdate(rd[0]);		
					ch.setBank("WX");
					ch.setAccountno("");
					ch.setAccountname("");
					ch.setPaytype(payTypeId+"");
					OrderFormPay orderFormPay = payService.getOrderFormByOrderNumPay(ch.getOrdernumber());
					if (orderFormPay != null) {
						if("SUCCESS".equals(rd[9])) ch.setCorderid(orderFormPay.getCorderid());//如果取得数据为付款金额，则取原账单
						else ch.setCorderid(orderFormPay.getMemo());//如果是退款金额，则取退款的应收单 
					}
					list.add(ch);
            	}
            }
		}
		return list;
	}

	@Override
	public OrderFormUnPay getOrderFormUnPay(HttpServletRequest request,
			HttpServletResponse response) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getCodeForwardParameter(HttpServletRequest request,
			HttpServletResponse response, int payTypeId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int codeServerNotify(HttpServletRequest request,
			HttpServletResponse response) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getMobileForwardParameter(HttpServletRequest request,
			HttpServletResponse response, int payTypeId) {
		// TODO Auto-generated method stub
		return null;
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
