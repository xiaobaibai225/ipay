package com.pay.framework.payment.abcpay;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.abc.trustpay.client.Constants;
import com.abc.trustpay.client.JSON;
import com.abc.trustpay.client.TrxException;
import com.abc.trustpay.client.ebus.PaymentRequest;
import com.abc.trustpay.client.ebus.PaymentResult;
import com.abc.trustpay.client.ebus.RefundRequest;
import com.abc.trustpay.client.ebus.SettleRequest;
import com.pay.framework.log.LogManager;
import com.pay.framework.payment.BasePay;
import com.pay.framework.payment.ICodePay;
import com.pay.framework.payment.IMobilePay;
import com.pay.framework.payment.IPay;
import com.pay.framework.payment.unionpay.util.DateStyle;
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

@Component("abcpay")
public class Abcpay extends BasePay implements IPay, IMobilePay, ICodePay {
	LogManager logManager = LogManager.getLogger(Abcpay.class);
	
	@Autowired
	private OrderFormService orderFormService;
	
	@Autowired
	private PayService payService;
	
	@Override
	public String getCodeForwardParameter(HttpServletRequest request,
			HttpServletResponse response, int payTypeId) {
		
		return null;
	}

	@Override
	public int codeServerNotify(HttpServletRequest request,
			HttpServletResponse response) {
		
		return 0;
	}

	@Override
	public String getMobileForwardParameter(HttpServletRequest request,
			HttpServletResponse response, int payTypeId) {
		
		return null;
	}
	@Override
	public int mobileServerNotify(int payTypeId, HttpServletRequest request,
			HttpServletResponse response) {
		
		return 0;
	}
	@Override
	public String getWapForwardParameter(HttpServletRequest request,
			HttpServletResponse response, int payTypeId) {
		
		return null;
	}

	@Override
	public String getPhoneMessageForwardParameter(HttpServletRequest request,
			HttpServletResponse response, int payTypeId) {
		
		return null;
	}

	@Override
	public int wapServerNotify(HttpServletRequest request,
			HttpServletResponse response) {
		
		return 0;
	}

	@Override
	public String getIapResult(HttpServletRequest request,
			HttpServletResponse response, int payTypeId)
			throws MalformedURLException, IOException {
		
		return null;
	}

	@Override
	public String getForwardUrl(HttpServletRequest request,
			HttpServletResponse response, int payTypeId) {
		logManager.debug("*************农行B2C支付请求开始*****************",null);
		//处理支付通用逻辑
		OrderFormUnPay orderform = beforePay(request, payTypeId);
		if (null == orderform) {
			return null;
		}
		PaymentBean pay = payTypeService.getPayType(orderform.getPaytype());
		
		//1、生成订单对象
		PaymentRequest tPaymentRequest = new PaymentRequest();
		tPaymentRequest.dicOrder.put("PayTypeID","ImmediatePay");                   //设定交易类型 ImmediatePay：直接支付 PreAuthPay：预授权支付 DividedPay：分期支付
		String submitDate=orderform.getSubmitdate().substring(0,10);
		logManager.debug("submitDate====",submitDate);
		Date date=DateUtil.formatToDate(submitDate,"yyyy-MM-dd");
		logManager.debug("date====",date.toLocaleString());
		tPaymentRequest.dicOrder.put("OrderDate",DateUtil.format(date,"yyyy/MM/dd"));                  //设定订单日期 （必要信息 - YYYY/MM/DD）
		String submitTime=orderform.getSubmitdate().substring(11);
		logManager.debug("submitTime====",submitTime);
		tPaymentRequest.dicOrder.put("OrderTime", submitTime);                   //设定订单时间 （必要信息 - HH:MM:SS）
		//tPaymentRequest.dicOrder.put("orderTimeoutDate", request.getParameter("orderTimeoutDate"));     //设定订单有效期
		tPaymentRequest.dicOrder.put("OrderNo",orderform.getOrdernumber());                       //设定订单编号 （必要信息）
		tPaymentRequest.dicOrder.put("CurrencyCode", "156");             //设定交易币种
		logManager.debug("money===",orderform.getPrice()+"");
		BigDecimal price = new BigDecimal(orderform.getPrice() + "");
		price = price.divide(new BigDecimal(100));
		tPaymentRequest.dicOrder.put("OrderAmount", price);      //设定交易金额
		//tPaymentRequest.dicOrder.put("Fee", request.getParameter("Fee"));                               //设定手续费金额
		//tPaymentRequest.dicOrder.put("OrderDesc", request.getParameter("OrderDesc"));                   //设定订单说明
		//tPaymentRequest.dicOrder.put("OrderURL", request.getParameter("OrderURL"));                     //设定订单地址
		//tPaymentRequest.dicOrder.put("ReceiverAddress", request.getParameter("ReceiverAddress"));       //收货地址
		tPaymentRequest.dicOrder.put("InstallmentMark", "0");       //分期标识 1：分期；0：不分期
//		if (request.getParameter("InstallmentMark") == "1" && request.getParameter("PayTypeID") == "DividedPay")
//		{
//		    tPaymentRequest.dicOrder.put("InstallmentCode", request.getParameter("InstallmentCode"));   //设定分期代码
//		    tPaymentRequest.dicOrder.put("InstallmentNum", request.getParameter("InstallmentNum"));     //设定分期期数
//		}
		tPaymentRequest.dicOrder.put("CommodityType", pay.getExt1() == null ? "0202" : pay.getExt1());           //设置商品种类 消费类 0201:虚拟类,0202:传统类,0203:实名类
		tPaymentRequest.dicOrder.put("BuyIP", orderform.getIp());                           //IP
		//tPaymentRequest.dicOrder.put("ExpiredDate", request.getParameter("ExpiredDate"));               //设定订单保存时间

		//2、订单明细
		LinkedHashMap orderitem = new LinkedHashMap();
		//orderitem.put("SubMerName", "测试二级商户1");    //设定二级商户名称
		//orderitem.put("SubMerId", "12345");    //设定二级商户代码
		//orderitem.put("SubMerMCC", "0000");   //设定二级商户MCC码 
		//orderitem.put("SubMerchantRemarks", "测试");   //二级商户备注项
		//orderitem.put("ProductID", "IP000001");//商品代码，预留字段
		String orderDesc=orderform.getProductdesc();
		logManager.debug("getOrderDesc====",orderDesc.length()+"");
		if(orderDesc.getBytes().length>100){
			orderitem.put("ProductName", orderDesc.substring(orderDesc.indexOf(".")+1,orderDesc.length()));//商品名称
		}else{
			orderitem.put("ProductName",orderDesc);//商品名称
		}
		//orderitem.put("UnitPrice", "1.00");//商品总价
		//orderitem.put("Qty", "1");//商品数量
		//orderitem.put("ProductRemarks", "测试商品"); //商品备注项
		//orderitem.put("ProductType", "充值类");//商品类型
		//orderitem.put("ProductDiscount", "0.9");//商品折扣
		//orderitem.put("ProductExpiredDate", "10");//商品有效期
		tPaymentRequest.orderitems.put(1, orderitem);

		//3、生成支付请求对象
		String paymentType = pay.getMerchantpwd(); //1：农行卡支付 7：对公户 
		tPaymentRequest.dicRequest.put("PaymentType", paymentType);            //设定支付类型
		String paymentLinkType  ="1";//1：internet网络接入  2：手机网络接入 ;                                         
		tPaymentRequest.dicRequest.put("PaymentLinkType", paymentLinkType);    //设定支付接入方式
		if (paymentType.equals(Constants.PAY_TYPE_UCBP) && paymentLinkType.equals(Constants.PAY_LINK_TYPE_MOBILE))
		{
		    tPaymentRequest.dicRequest.put("UnionPayLinkType",request.getParameter("UnionPayLinkType"));  //当支付类型为6，支付接入方式为2的条件满足时，需要设置银联跨行移动支付接入方式
		}
		//tPaymentRequest.dicRequest.put("ReceiveAccount", request.getParameter("ReceiveAccount"));      //设定收款方账号
		//tPaymentRequest.dicRequest.put("ReceiveAccName", request.getParameter("ReceiveAccName"));      //设定收款方户名
		tPaymentRequest.dicRequest.put("NotifyType", "1");              //设定通知方式  0：URL页面通知  1：服务器通知 
		tPaymentRequest.dicRequest.put("ResultNotifyURL", pay.getBgurl());    //设定通知URL地址
		//tPaymentRequest.dicRequest.put("MerchantRemarks", request.getParameter("MerchantRemarks"));    //设定附言
		tPaymentRequest.dicRequest.put("IsBreakAccount","0");      //设定交易是否分账 0:否；1:是
		//tPaymentRequest.dicRequest.put("SplitAccTemplate", request.getParameter("SplitAccTemplate"));  //分账模版编号        

		JSON json = tPaymentRequest.extendPostRequest(pay.getTerminalcode());

		String ReturnCode = json.GetKeyValue("ReturnCode");
		String ErrorMessage = json.GetKeyValue("ErrorMessage");
		
		if (ReturnCode.equals("0000"))
		{
			logManager.debug("PaymentURL-->",json.GetKeyValue("PaymentURL"));
			return json.GetKeyValue("PaymentURL");
		}else{
			logManager.debug("业务失败错误代码：",ReturnCode);
			logManager.debug("业务失败错误信息：",ErrorMessage);
			return null;
		}
	}

	@Override
	public int doPaymentServerNotify(HttpServletRequest request,
			HttpServletResponse response) {
		logManager.debug("*************农行支付通知请求开始*****************",null);
		String MSG=request.getParameter("MSG");
		logManager.debug("***MSG***",MSG);
		try {
			PaymentResult result=new PaymentResult(MSG);
			if(result!=null && result.isSuccess()){
				String order_no = result.getValue("OrderNo");
				logManager.debug("OrderNo*****",order_no);
				OrderFormUnPay orderFormUnpay = orderFormService.queryOrderFormUnPaymentByOrderNumber(order_no);
				if (orderFormUnpay == null) {
					try {
						OrderFormPay orderForm=orderFormService.getOrderByOrderNum(order_no);
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
						logManager.debug("农行在没有unpay的情况下跳转前台地址失败",null);
					}
					return PayStatus.PAY_NO_ORDER;
				}
				String amount = (String) result.getValue("Amount");
				logManager.debug("Amount=",amount);
				BigDecimal transactionAmount= new BigDecimal(amount);
				// 判断金额
				if(transactionAmount.multiply(new BigDecimal(100)).compareTo(new BigDecimal(orderFormUnpay.getPrice()))<0) {
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
					String iRspRef=(String)result.getValue("iRspRef");
					logManager.debug("银行返回交易流水号:iRspRef=",iRspRef);
					super.afterServerNotify(orderFormUnpay,iRspRef);
				}
				logManager.debug("交易凭证号:VoucherNo=",result.getValue("VoucherNo"));
				logManager.debug("交易批次号:iRspRef=",result.getValue("BatchNo"));
				logManager.debug("银行交易日期:HostDate=",result.getValue("HostDate"));
				logManager.debug("银行交易时间:HostTime=",result.getValue("HostTime"));
				logManager.debug("消费者支付方式:PayType=",result.getValue("PayType"));
				logManager.debug("由于农行后台通知，需要跳转到页面通知的界面，地址为", orderFormUnpay.getFronturl());
				try {
					String url=orderFormUnpay.getFronturl();
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
					logManager.debug("农行跳转前台地址失败",null);
				}
			}else{
				logManager.debug("业务失败错误代码：",result.getReturnCode());
				logManager.debug("业务失败错误信息：",result.getErrorMessage());
				return PayStatus.PAY_FAIL;
			}
		} catch (TrxException e) {
			e.printStackTrace();
		}
		return PayStatus.PAY_SUCCESS;
	}

	@Override
	public int doPaymentPageNotify(HttpServletRequest request,
			HttpServletResponse response) {
		
		return 0;
	}

	@Override
	public String refund(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		logManager.debug("*************农行退款请求开始*****************",null);
		Refund refund = super.beforeRefund(request);
		JSONObject json = new JSONObject();
		if (refund == null) {
			json.put("status", "0");
			json.put("errormsg", "already refund all");
			return json.toString();
		}
		PaymentBean payment = payTypeService.getPayType(refund.getPaytype());
		
        //1、生成退款请求对象
        RefundRequest tRequest = new RefundRequest();
		String refundDate=refund.getRefundDate().substring(0,10);
		logManager.debug("refundDate====",refundDate);
		Date date=DateUtil.formatToDate(refundDate,"yyyy-MM-dd");
		logManager.debug("date====",date.toLocaleString());
		String refundTime=refund.getRefundDate().substring(11);
		logManager.debug("refundTime====",refundTime);
        tRequest.dicRequest.put("OrderDate", DateUtil.format(date,"yyyy/MM/dd"));  //订单日期（必要信息）
        tRequest.dicRequest.put("OrderTime",refundTime); //订单时间（必要信息）
        //tRequest.dicRequest.put("MerRefundAccountNo", request.getParameter("txtMerRefundAccountNo"));  //商户退款账号
        //tRequest.dicRequest.put("MerRefundAccountName", request.getParameter("txtMerRefundAccountName")); //商户退款名
        tRequest.dicRequest.put("OrderNo", refund.getOrdernumber()); //原交易编号（必要信息）
        tRequest.dicRequest.put("NewOrderNo", refund.getOuterfundno()); //交易编号（必要信息）
        tRequest.dicRequest.put("CurrencyCode", "156"); //交易币种（必要信息）
        tRequest.dicRequest.put("TrxAmount",String.valueOf(refund.getRefundmoney()*1.00/100)); //退货金额 （必要信息）
        //tRequest.dicRequest.put("MerchantRemarks", request.getParameter("txtMerchantRemarks"));  //附言

        //2、传送退款请求并取得退货结果
        JSON json1 = tRequest.extendPostRequest(payment.getTerminalcode());

        //3、判断退款结果状态，进行后续操作
        String ReturnCode = json1.GetKeyValue("ReturnCode");
        String ErrorMessage = json1.GetKeyValue("ErrorMessage");
		if(ReturnCode.equals("0000")){
			logManager.debug("交易凭证号:VoucherNo=", json1.GetKeyValue("VoucherNo"));
			logManager.debug("银行返回交易流水号:iRspRef=", json1.GetKeyValue("iRspRef"));
			logManager.debug("银行交易日期:HostDate=", json1.GetKeyValue("HostDate"));
			logManager.debug("银行交易时间:HostTime=", json1.GetKeyValue("HostTime"));
			afterRefund(refund.getOrdernumber());
			orderFormService.updatePayStatus(refund.getOrdernumber(), PayStatus.PAY_SUCCESS_REFUND);
			json.put("status", PayStatus.PAY_SUCCESS);
			json.put("corderid", refund.getCorderid());
			json.put("money", refund.getRefundmoney());
			json.put("success", true);
		}else{
			logManager.debug("业务失败错误代码：",ReturnCode);
			logManager.debug("业务失败错误信息：",ErrorMessage);
			json.put("status", PayStatus.PAY_FAIL);
			json.put("corderid", refund.getCorderid());
			json.put("money", refund.getRefundmoney());
			json.put("errormsg",ErrorMessage);
		}
		return json.toString();
	}

	@Override
	public int refundNotify(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		return 0;
	}

	@Override
	public List<Check> check(HttpServletRequest request,
			HttpServletResponse response, int payTypeId) throws Exception {
		PaymentBean payment = payTypeService.getPayType(payTypeId);
		long start=System.currentTimeMillis()/3600;
		logManager.debug("*********农行对账交易接口开始请求**********************",start+"");
		String gmt_start_time = (String) request.getAttribute("starttime");
	    String  settleDate= gmt_start_time.replace("-", "/");//清算日期
	  //1、取得商户对账单下载所需要的信息 生成商户对账单下载请求对象
		SettleRequest sr=new SettleRequest();
		sr.dicRequest.put("SettleDate",settleDate);  //对账日期YYYY/MM/DD （必要信息）
		sr.dicRequest.put("ZIP","0");//1：压缩，0：不压缩 
		JSON json=sr.extendPostRequest(payment.getTerminalcode());
		//4、判断商户对账单下载结果状态，进行后续操作
		String ReturnCode = json.GetKeyValue("ReturnCode");
		String ErrorMessage = json.GetKeyValue("ErrorMessage");
		//String channel=payment.getChannel();
		if(ReturnCode.equals("0000")){
			logManager.debug("*****取得对账日期为"+json.GetKeyValue("SettleDate")+"的交易明细记录",null);
			//取得对账单明细
			String details=json.GetKeyValue("DetailRecords");
			String[] tRecords = details.split("\\^\\^");
			List<Check> list=new ArrayList<Check>();
			for(int i = 1; i < tRecords.length; i++) {
				logManager.debug("Record-" + i + " = [" + tRecords[i] + "]",null);
				String[] rd=tRecords[i].split("\\|");
				String cardType=rd[8];
				Check ch=new Check();
				logManager.debug("支付卡类型为",cardType);
				if("401".equals(cardType))
				{
					ch.setPaytype(payTypeId+"");
				}
				else if("601".equals(cardType))
				{
					ch.setPaytype((String)AbcCompany.companyType.get(payment.getCompanyid()));
				}
				else
				{
					continue;
				}
				String signer="Sale".equals(rd[1])?"":"-";
				
				ch.setOrdernumber(rd[2]);
				ch.setPrice(signer+rd[4]);
				ch.setTranseq(rd[13]);
				String td=com.pay.framework.payment.unionpay.util.DateUtil.DateToString(com.pay.framework.payment.unionpay.util.DateUtil.StringToDate(rd[3], DateStyle.YYYYMMDDHHMMSSCN),DateStyle.YYYY_MM_DD_HH_MM_SS);
				ch.setTransdate(td);		
				ch.setBank("Abc");
				ch.setAccountno(rd[7]);
				ch.setAccountname("");//农行对账单没有提供付款方名称
				ch.setFee("0.00");
				ch.setNetvalue(signer+rd[4]);
				OrderFormPay orderFormPay = payService.getOrderFormByOrderNumPay(ch.getOrdernumber());
				if (orderFormPay != null) {
					if("Sale".equals(rd[1])) ch.setCorderid(orderFormPay.getCorderid());//如果取得数据为付款金额，则取原账单
					else ch.setCorderid(orderFormPay.getMemo());//如果是退款金额，则取退款的应收单 
				}
				list.add(ch);
			}
			logManager.debug("*********农行对账交易接口请求结束****",System.currentTimeMillis()/3600+"");
			return list;
		}else{
			logManager.debug("业务失败错误代码：",ReturnCode);
			logManager.debug("业务失败错误信息：",ErrorMessage);
			List<Check> list=new ArrayList<Check>();
			return list;
		}
		//logManager.debug("*********农行对账交易接口请求结束**********************",System.currentTimeMillis()/3600+"");
		//return null;
	}

	@Override
	public OrderFormUnPay getOrderFormUnPay(HttpServletRequest request,
			HttpServletResponse response) {
		String out_trade_no = "";
		try {
			out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ComplexOrderBean complexOrderBean = payService.queryComplexOrderBeanByOrderNumber(out_trade_no);
		OrderFormUnPay orderUnpay = complexOrderBean.getOrderformunpay();
		return orderUnpay;
	}
	
}