package com.pay.service.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.pay.framework.dao.OrderFormPayDao;
import com.pay.framework.dao.PayTypeDao;
import com.pay.framework.http.RequestUtil;
import com.pay.framework.log.LogFlags;
import com.pay.framework.log.LogManager;
import com.pay.framework.payment.AutoPay;
import com.pay.framework.payment.ICodePay;
import com.pay.framework.payment.IMisPosPay;
import com.pay.framework.payment.IMobilePay;
import com.pay.framework.payment.IPay;
import com.pay.framework.payment.PayFactory;
import com.pay.framework.util.DateUtil;
import com.pay.framework.util.JsonBuilder;
import com.pay.model.Check;
import com.pay.model.OrderFormPay;
import com.pay.model.OrderFormUnPay;
import com.pay.model.PayStatus;
import com.pay.model.PaymentBean;
import com.pay.model.Refund;
import com.pay.service.PayService;
@Service("payServiceExt")
public class PayServiceImpl implements PayService {

	private static LogManager logger = LogManager.getLogger(PayService.class);

	@Autowired
	private OrderFormPayDao orderFormPayDao;
	@Autowired
	private com.pay.framework.service.PayService payService;
	@Autowired
	private PayTypeDao payTypeDao;

	@Override
	public boolean pay(int typeId, HttpServletRequest request, HttpServletResponse response) {
		// 如果传默认银行了，则使用支付宝
		String defaultbank = request.getParameter("defaultbank");
		if (StringUtils.isNotBlank(defaultbank)) {
			//typeId = PayTypes.PAYMENT_ALIPAY;
		}
		IPay pay = PayFactory.getPayInstance(typeId);
		// 获取发送请求的url
		String url = pay.getForwardUrl(request, response, typeId);
		if (null == url || url.equals("")) {
			return false;
		}
		// 在pay方法里面已发送了post请求 ,或其它处理方式（返回success）
		if (url.equalsIgnoreCase("post") || url.equalsIgnoreCase(PayStatus.PAY_SUCCESS_TOPAY)) {
			return true;
		}
		logger.debug(LogFlags.PAY_TO_PAY, " paytype: " + typeId + " , url: " + url);
		try {
			// 添加中行post 请求
			if(url.contains("pay_form_union") || url.contains("moto_code") || url.contains("pay_form_bocpay") || url.contains("pay_form_icbcpay")) response.getWriter().write(url);
			else if(url.contains("weixin"))
			{
		        String keycode = url;
		         
		        if (keycode != null && !"".equals(keycode)) {
		            ServletOutputStream stream = null;
		            try {
		                int size=400;
		                stream = response.getOutputStream();
		                QRCodeWriter writer = new QRCodeWriter();
		                BitMatrix m = writer.encode(keycode, BarcodeFormat.QR_CODE, size, size);
		                MatrixToImageWriter.writeToStream(m, "JPEG", stream);
		            } catch (WriterException e) {
		                e.printStackTrace();
		            } finally {
		                if (stream != null) {
		                    stream.flush();
		                    stream.close();
		                }
		            }
		        }
			}
			else response.sendRedirect(url);
		} catch (IOException e) {
			logger.debug(LogFlags.PAY_TO_PAY, "发送支付请求失败..........!!! typeId: " + typeId + " message: " + e.getMessage());
			return false;
		}
		return true;
	}

	@Override
	public boolean serverNotify(int typeId, HttpServletRequest request, HttpServletResponse response) {
		IPay pay = PayFactory.getPayInstance(typeId);
		int result = -1;
		try {
			result = pay.doPaymentServerNotify(request, response);
		} catch (Exception e) {
			e.printStackTrace();
			logger.debug(LogFlags.PAY_SERVER_NOTIFY, "出现异常， typeId: " + typeId + " message: " + e.getMessage());
			return false;
		}
		// 存入用户信息
		pay.saveUserInfo(request);
		logger.debug(LogFlags.PAY_SERVER_NOTIFY, "type id " + typeId + " result : " + result + " content : " + RequestUtil.getParamaterMap(request));
		return true;
	}

	@Override
	public boolean serverMobileNotify(int typeId, HttpServletRequest request, HttpServletResponse response) {
		IMobilePay pay = PayFactory.getMobilePayInstance(typeId);
		int result = -1;
		try {
			result = pay.mobileServerNotify(typeId,request, response);
		} catch (Exception e) {
			e.printStackTrace();
			logger.debug(LogFlags.PAY_MOBILE_SERVER_NOTIFY, "出现异常， typeId: " + typeId + " message: " + e.getMessage());
			return false;
		}
		logger.debug(LogFlags.PAY_MOBILE_SERVER_NOTIFY,
				"type id " + typeId + " result : " + result + " content : " + RequestUtil.getParamaterMap(request));
		return true;
	}

	@Override
	public boolean wapServerNotify(int typeId, HttpServletRequest request, HttpServletResponse response) {
		IMobilePay pay = PayFactory.getMobilePayInstance(typeId);
		int result = -1;
		try {
			result = pay.wapServerNotify(request, response);
		} catch (Exception e) {
			e.printStackTrace();
			logger.debug(LogFlags.PAY_MOBILE_SERVER_NOTIFY, "出现异常， typeId: " + typeId + " message: " + e.getMessage());
			return false;
		}
		logger.debug(LogFlags.PAY_MOBILE_SERVER_NOTIFY,
				"type id " + typeId + " result : " + result + " content : " + RequestUtil.getParamaterMap(request));
		return true;
	}

	@Override
	public boolean refundNotify(int typeId, HttpServletRequest request, HttpServletResponse response) {
		IPay pay = PayFactory.getPayInstance(typeId);
		int result = -1;
		try {
			result = pay.refundNotify(request, response);
		} catch (Exception e) {
			e.printStackTrace();
			logger.debug(LogFlags.PAY_SERVER_NOTIFY, "出现异常， typeId: " + typeId + " message: " + e.getMessage());
			return false;
		}

		if (result != PayStatus.PAY_SUCCESS) {
			return false;
		}
		return true;
	}

	@Override
	public boolean pageNotify(int typeId, HttpServletRequest request, HttpServletResponse response) {
		IPay pay = PayFactory.getPayInstance(typeId);
		int result = -1;
		try {
			result = pay.doPaymentPageNotify(request, response);
		} catch (Exception e) {
			logger.debug(LogFlags.PAY_PAGE_NOTIFY, "出现异常， typeId: " + typeId + " message: " + e.getMessage());
			return false;
		}

		logger.debug(LogFlags.PAY_PAGE_NOTIFY, "type id " + typeId + " result : " + result + " content : " + RequestUtil.getParamaterMap(request));
		return result == PayStatus.PAY_SUCCESS ? true:false;
	}
	
	public OrderFormUnPay getOrderFormUnPay(int typeId, HttpServletRequest request, HttpServletResponse response) {
		IPay pay = PayFactory.getPayInstance(typeId);

		return pay.getOrderFormUnPay(request, response);

	}

	@Override
	public String refund(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String corderid = request.getParameter("corderid");
		String companyid = request.getParameter("companyid");
		String newcorderid= request.getParameter("newcorderid");
		if (StringUtils.isNotBlank(corderid) && StringUtils.isNotBlank(companyid)) {
			OrderFormPay orderFormPay = orderFormPayDao.getOrderFormByCorderid(corderid, Integer.parseInt(companyid));
			if (orderFormPay != null) {
				IPay pay = PayFactory.getPayInstance(orderFormPay.getPaytype());
				logger.debug("开始要退款了###", orderFormPay.getPaytype()+"###");
				String result = pay.refund(request, response);
				logger.debug(LogFlags.PAY_REFUND, result);
				return result;
			} else {
				return new JsonBuilder().addRecord("errormsg", URLEncoder.encode("订单不存在!", "UTF-8")).addRecord("corderid", corderid)
						.addRecord("status", "0").getJson().toString();
			}
		}
		return new JsonBuilder().addRecord("errormsg", URLEncoder.encode("参数不完整!", "UTF-8")).addRecord("corderid", corderid).addRecord("status", "0")
				.getJson().toString();
	}

	@Override
	public String querystat(HttpServletRequest request, HttpServletResponse response) throws Exception {
		OrderFormPay orderFormPay = orderFormPayDao.getOrderFormByCorderid(request.getParameter("corderid"),
				Integer.parseInt(request.getParameter("companyid")));
		// 支持传支付订单号
		if (orderFormPay == null) {
			orderFormPay = orderFormPayDao.getOrderFormByOrderNumPay(request.getParameter("corderid"));
		}
		JSONObject json = new JSONObject();
		if (orderFormPay != null) {
			json.put("status", orderFormPay.getStatus());
			json.put("corderid", orderFormPay.getCorderid());
			json.put("ordernumber", orderFormPay.getOrdernumber());
			json.put("money", orderFormPay.getPrice());
			json.put("paytime", orderFormPay.getPaymentdate());
		} else {
			json.put("status", 0);
			json.put("corderid", "");
			json.put("money", "");
			json.put("paytime", "");
			json.put("ordernumber", "");
		}
		return json.toString();
	}

	@Override
	public String check(HttpServletRequest request, HttpServletResponse response) throws Exception {
		// 要查询的开始时间
		String gmt_start_time = request.getParameter("start");
		
		int payType=Integer.valueOf(request.getParameter("paytype"));

		// 要查询的结束时间
		String gmt_end_time = request.getParameter("end");

		List<Check> retList = new ArrayList<Check>();
		IPay payAli = PayFactory.getPayInstance(payType);
		int dayBetween = DateUtil.daysBetween(DateUtil.parseDate(gmt_start_time), DateUtil.parseDate(gmt_end_time));
		if (dayBetween > 1) {
			for (int i = 0; i < dayBetween + 1; i++) {
				request.setAttribute("starttime", DateUtil.format(DateUtil.addDays(DateUtil.parseDate(gmt_start_time), i)));
				request.setAttribute("endtime", DateUtil.format(DateUtil.addDays(DateUtil.parseDate(gmt_start_time), i + 1)));
				List<Check> checkali = payAli.check(request, response, payType);
				retList.addAll(checkali);
			}
		} else {
			request.setAttribute("starttime", gmt_start_time);
			request.setAttribute("endtime", gmt_end_time);
			List<Check> checkali = payAli.check(request, response,payType);
			retList.addAll(checkali);
		}
		payAfterCheck(retList);
		JSONObject json = new JSONObject();
		json.put("check", retList);
		return json.toString();
	}
	
	@Override
	public String misposCheck(HttpServletRequest request, HttpServletResponse response) throws Exception {
		int payType=Integer.valueOf(request.getParameter("paytype"));
		IMisPosPay pay = PayFactory.getMisPosPayInstance(payType);
		Map map = pay.misposCheck(request, response, payType);
		List<Check> listCheck = new ArrayList<Check>();
		List<Check> listCheckExt = new ArrayList<Check>();
		listCheck = (ArrayList)map.get("listCheck");
		listCheckExt = (ArrayList)map.get("listCheckExt");
		payAfterCheck(listCheckExt);
		JSONObject json = new JSONObject();
		json.put("check", listCheck);
		json.put("checkExt", listCheckExt);
		return json.toString();
	}
	
	/**
	 * 对账时如发现unpay里面有记录，则认为是付款成功的，需做相应的补单处理
	 * @param retList
	 */
	private void payAfterCheck(List<Check> retList )
	{
		logger.debug(LogFlags.TIME_PRINT, "开始补录相关数据" );
		for(Check check:retList)
		{
			String orderNumber=check.getOrdernumber();
			if(StringUtils.isEmpty(check.getCorderid()))//如果商户id为空时，才需要判断是否补单
			{
				logger.debug(LogFlags.TIME_PRINT, "订单号为此的是否需要补录数据?"+orderNumber );
					OrderFormUnPay orderFormUnpay=orderFormPayDao.getOrderFormByOrderNum(orderNumber);
					double price =Double.parseDouble(check.getPrice());
					try {
						if (orderFormUnpay != null&&price>0) {
							logger.debug(LogFlags.TIME_PRINT, "找到订单号为这个的需要补充收入数据==="+orderNumber );
							// 处理数据
							OrderFormPay form = payService.doPaySuccess(orderFormUnpay, check.getTranseq());
							logger.debug(LogFlags.TIME_PRINT, "收入数据补充完成" );
							check.setCorderid(form.getCorderid());
							logger.debug(LogFlags.TIME_PRINT, "设置收入商户id完成，商户id为==="+form.getCorderid() );
							//payService.doServerNotify(form);
							//logger.debug(LogFlags.TIME_PRINT, "业务数据补充完成");
						}
					} catch (Exception e) {
						logger.debug("补录收入数据异常",e.getMessage());
					}				
			}
			else
			{
				if(check.getPrice().contains("-"))//如果是退款，则补充退款相关记录
				{
					List<Refund> refundList=null;
					if(orderNumber.contains("refund"))
					{
						logger.debug(LogFlags.TIME_PRINT, "订单号含refund关键字===" );
						refundList=orderFormPayDao.getRefundByOuterfundno(orderNumber);//通过退款单号查找退款记录
					}
					else
					{
						logger.debug(LogFlags.TIME_PRINT, "订单号!!!!!不含refund关键字===" );
						refundList=orderFormPayDao.getRefundByOrderNumber(orderNumber);//通过订单号查找退款记录
					}
					if(null!=refundList && refundList.size()>0)
					{
						logger.debug(LogFlags.TIME_PRINT, "此退款单需要进行补录===" );
						Refund refund=(Refund)refundList.get(0);
						payService.doRefundSuccess(refund);
						OrderFormPay form =orderFormPayDao.getOrderFormByOrderNumPay(orderNumber);
						check.setCorderid(form.getMemo());
						logger.debug(LogFlags.TIME_PRINT, "设置退款新商户id完成，新商户id为==="+form.getMemo());
					}
				}
			}
		}
	}

	@Override
	public String getMobileForwardParameter(int pay_type_id, HttpServletRequest request, HttpServletResponse response) throws Exception {
		IMobilePay pay = PayFactory.getMobilePayInstance(pay_type_id);

		return pay.getMobileForwardParameter(request, response, pay_type_id);
	}

	@Override
	public String getWapForwardParameter(int pay_type_id, HttpServletRequest request, HttpServletResponse response) throws Exception {
		IMobilePay pay = PayFactory.getMobilePayInstance(pay_type_id);

		return pay.getWapForwardParameter(request, response, pay_type_id);
	}

	/**
	 * 获取wap端的支付加密串
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@Override
	public String getPhoneMessageParameter(int pay_type_id, HttpServletRequest request, HttpServletResponse response) throws Exception {
		IMobilePay pay = PayFactory.getMobilePayInstance(pay_type_id);

		return pay.getPhoneMessageForwardParameter(request, response, pay_type_id);
	}

	@Override
	public String pointPay(int pay_type_id, HttpServletRequest request, HttpServletResponse response) throws Exception {
		IPay pay = PayFactory.getPayInstance(pay_type_id);
		return pay.getForwardUrl(request, response, pay_type_id);
	}

	@Override
	public String iap(int pay_type_id, HttpServletRequest request, HttpServletResponse response) throws Exception {
		IMobilePay pay = PayFactory.getMobilePayInstance(pay_type_id);
		return pay.getIapResult(request, response, pay_type_id);
	}

	@Override
	public String cancelorder(int pay_type_id, HttpServletRequest request, HttpServletResponse response) throws Exception {
		return null ;
	}

	@Override
	public boolean cancelorderservernotify(int typeId, HttpServletRequest request, HttpServletResponse response) {
		return true;
	}

	@Override
	public String tax(int pay_type_id, HttpServletRequest request, HttpServletResponse response) throws Exception {
		return null ;
	}

	@Override
	public String autoPay(int typeId, HttpServletRequest request, HttpServletResponse response) {
		AutoPay pay = PayFactory.getAutoPayInstance(typeId);
		// 获取发送请求的url
		String result = pay.autoPayUrl(request, null, typeId);
		if (result.equalsIgnoreCase(PayStatus.PAY_SUCCESS_TOPAY)) {
			return JsonBuilder.sucess(result);
		}
		return JsonBuilder.fail(result);
	}

	@Override
	public String getTwoDimensionCode(int pay_type_id, HttpServletRequest request, HttpServletResponse response) throws Exception {
		ICodePay pay = PayFactory.getCodePayInstance(pay_type_id);

		return pay.getCodeForwardParameter(request, response, pay_type_id);
	}

	@Override
	public String iframePay(int typeId, HttpServletRequest request, HttpServletResponse response) {
		return null ;
	}


	@Override
	public String getMisposPayParam(int pay_type_id,String id,HttpServletRequest request, HttpServletResponse response) {
		IMisPosPay pay = PayFactory.getMisPosPayInstance(pay_type_id);

		return pay.getMisposPayParam(pay_type_id, id,request,response);
		
	}

	@Override
	public String getMisposRefundParam(int pay_type_id,String refundId,
			HttpServletRequest request, HttpServletResponse response) {
		
		IMisPosPay pay = PayFactory.getMisPosPayInstance(pay_type_id);

		return pay.getMisposRefundParam(pay_type_id, refundId,request,response);
		
	}
	
	@Override
	public boolean saveMisposMsg(int pay_type_id, HttpServletRequest request,
			HttpServletResponse response) {
		
		IMisPosPay pay = PayFactory.getMisPosPayInstance(pay_type_id);

		return pay.saveMisposMsg(pay_type_id,request,response);
		
	}

	@Override
	public String getMisposPayJson(int pay_type_id, HttpServletRequest request,
			HttpServletResponse response) {
		
		IMisPosPay pay = PayFactory.getMisPosPayInstance(pay_type_id);

		return pay.getMisposPayJson(pay_type_id,request,response);
		
	}

	@Override
	public String getMisposReprintParam(int pay_type_id,
			HttpServletRequest request, HttpServletResponse response) {

		IMisPosPay pay = PayFactory.getMisPosPayInstance(pay_type_id);

		return pay.getMisposReprintParam(pay_type_id,request,response);
		
	}
	@Override
	public String searchPayStatus(HttpServletRequest request,
			HttpServletResponse response) {
		try {
			logger.debug("查询支付状态开始","查询支付状态开始");
			int companyid = Integer.parseInt(request.getParameter("companyid"));
			String institution = request.getParameter("institution");
			String platform= request.getParameter("platform");
			String available= request.getParameter("available");
			String payMethod= request.getParameter("payMethod");
			logger.debug("查询条件","platform="+platform+"   available="+available+"  companyid="+companyid+"  institution="+institution+" payMethod="+payMethod);
			List<PaymentBean> list  = payTypeDao.getPayStatusList(companyid,institution,platform,available,payMethod);
			List<Map<String, String>> payStatusList = new ArrayList<Map<String, String>>();
			for(int i=0;i<list.size();i++){
				PaymentBean payType = list.get(i);
				Map<String, String> map = new HashMap<String,String>();
				new String(payType.getName());
				map.put("typeid", String.valueOf(payType.getTypeid()));
				if(payType.getAvailable()==""||payType.getAvailable()=="null"){
					map.put("available", "N");
				}else{
					map.put("available", payType.getAvailable());
				}
				map.put("paydesc",URLEncoder.encode(payType.getName(),"utf-8"));
				map.put("pay_method",URLEncoder.encode(payType.getChannel(),"utf-8"));
				map.put("platform", URLEncoder.encode(payType.getPlatform(),"utf-8"));
				map.put("institution",URLEncoder.encode(payType.getInstitution(),"utf-8"));
				payStatusList.add(map);
			}
			JSONObject json = new JSONObject();
			json.put("PayStatusList", payStatusList);
			logger.debug("查询支付状态结束","查询支付状态结束 list.size()="+list.size()+" json.toString()="+json.toString());
			return json.toString();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
		
	}
}
