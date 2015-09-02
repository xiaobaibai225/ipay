package com.pay.framework.payment.ccbmispostpay;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pay.framework.http.HttpRequester;
import com.pay.framework.http.HttpResponse;
import com.pay.framework.log.LogManager;
import com.pay.framework.payment.BasePay;
import com.pay.framework.payment.IMisPosPay;
import com.pay.framework.payment.IPay;
import com.pay.framework.payment.bocpay.BOCPayUtil;
import com.pay.framework.service.OrderFormService;
import com.pay.framework.service.PayService;
import com.pay.framework.util.DateUtil;
import com.pay.framework.util.MD5Util;
import com.pay.framework.util.MoneyUtil;
import com.pay.framework.util.StringUtil;
import com.pay.model.Check;
import com.pay.model.MisPosOrderFormPay;
import com.pay.model.MisPosOrderFormUnPay;
import com.pay.model.OrderFormPay;
import com.pay.model.OrderFormUnPay;
import com.pay.model.PayStatus;
import com.pay.model.PaymentBean;
import com.pay.model.Refund;

@Component("ccbmispostpay")
public class CCBMisPostPay extends BasePay implements IPay,IMisPosPay{

	static LogManager logger = LogManager.getLogger(CCBMisPostPay.class);
	
	@Autowired
	private OrderFormService orderFormService;
	@Autowired
	private com.pay.framework.service.MisPosOrderFormService misPosOrderFormService;
	
	@Autowired
	private PayService payService;
	public static final String yyyyMMddHHmmss = "yyyyMMddHHmmss";
	
	@Override
	public String getForwardUrl(HttpServletRequest request,
			HttpServletResponse response, int payTypeId) {
			
	    logger.debug("进入建行pos支付", "进入建行pos支付。。。。。。payTypeId："+payTypeId);
//		// 处理支付通用逻辑CCBMisPos
		OrderFormUnPay orderform = beforePay(request, payTypeId);
		if (null == orderform) {
			return null;
		}
		if(StringUtils.isBlank(orderform.getCorderid())){
			return null;
		}
		//更新字表数据
		misPosOrderFormService.updateByCorderId(orderform.getOrdernumber(),orderform.getCorderid());
			
		PaymentBean payment = payTypeService.getPayType(orderform.getPaytype());
//		// 把请求参数打包成数组
		Map<String, String> m = new HashMap<String, String>();
//		m.put("transType", "S1");//交易指令 Q1签到 Q2结算 Q3重打印 S1消费  -- 2
//		m.put("transAmount", StringUtil.leftPad(orderform.getPrice()+"", 12, '0'));// 交易金额   12
//		m.put("loyalty", StringUtil.rightPad("", 12, ' '));// /积分 12
//		m.put("MisTrace", StringUtil.leftPad(CCBMisPostPayUtil.MisTrace(), 6, '0'));// MIS流水号  6
//		m.put("InstallmentTimes", StringUtil.rightPad("", 2, ' '));// 分期期数 03,06,09,12,18,24,36 2
//		m.put("oldAuthNo", StringUtil.rightPad("", 6, ' '));// 原交易授权号 6
//		m.put("oldPostrace", StringUtil.rightPad("", 6, ' ')); // 原交易流水号（撤销交易需要）  6
//		m.put("oldHostTrace", StringUtil.rightPad("", 12, ' ')); // 原交易系统检索号（退货交易需要）  12 
//		m.put("oldTransDate", StringUtil.rightPad("", 8, ' ')); // 原交易日期（退货交易需要） 8
//		m.put("cashPcNum", StringUtil.rightPad("001", 20, ' '));// 后补空格 收银台号（打印小票需要） 20  == 
//		m.put("cashierNum", StringUtil.rightPad("001", 20, ' '));// 后补空格 收银员工号（打印小票需要）20
//		m.put("notifyUrl", payment.getBgurl());// 支付后台通知地址
//		m.put("PayUrl", payment.getExt4());// 获取支付参数地址
//		m.put("payInfUrl", payment.getExt8());// 获取已支付信息
//		m.put("refundUrl", payment.getExt3());// 获取退款参数地址
//		m.put("reprintUrl", payment.getExt5());// 重打印地址
//		m.put("saveUrl", payment.getExt6());// 保存小票信息地址
//		m.put("OCXUrl32", payment.getExt7());// 控件地址
//		m.put("OCXUrl64", payment.getExt10());// 控件地址
		
		m.put("price", orderform.getMoney());
		m.put("orderNum",orderform.getOrdernumber());
		m.put("corderid",orderform.getCorderid());
		m.put("typeId", String.valueOf(payment.getTypeid()));
		logger.debug("建行mis pos 请求参数",m.toString());
		// 建立请求 
		//String sHtmlText = BOCPaySubmit.buildForwardUrl(m, payment.getPosturl());
		logger.debug("建行mis post支付请求页面URL", "建行mis post支付请求页面URL:"+payment.getPosturl());
		String sHtmlText = CCBMisPostPaySubmit.createHtml(payment.getPosturl(), m);
		logger.debug("建行 mis post 建立请求", "建行 mis post建立请求:"+sHtmlText);
		return sHtmlText;
		
	}

	@Override
	public int doPaymentServerNotify(HttpServletRequest request,
			HttpServletResponse response) {

		logger.debug("doPaymentServerNotify:", "建行mis pos 支付后台通知......");
		try {
			Map reqParam = CCBMisPostPayUtil.getAllRequestParam(request);
			logger.debug("建行 mis pos 支付", "建行 mis pos 支付主动通知返回参数:"+reqParam.toString());
			String orderNo = (String) reqParam.get("orderNo");
				OrderFormUnPay orderFormUnpay = orderFormService.queryOrderFormUnPaymentByOrderNumber(orderNo);
				if (orderFormUnpay == null) { // unpay 表没有数据
					try {
						PrintWriter out = response.getWriter();
						response.setHeader("Cache-Control", "no-cache");
						out.write("noorder");
						out.flush();
						out.close();
					} catch (IOException e) {
						logger.debug("Mis post ccb 在没有unpay的情况下跳转前台地址失败",null);
					}
					logger.debug("info","支付后台-----Mis post ccb 支付接收后台通知结束......,状态：【"+PayStatus.PAY_NO_ORDER+"】");
					return PayStatus.PAY_NO_ORDER;
				}
				//PaymentBean payment = payTypeService.getPayType(orderFormUnpay.getPaytype());
				
				super.afterServerNotify(orderFormUnpay,"");
				
				try {
					PrintWriter out = response.getWriter();
					response.setHeader("Cache-Control", "no-cache");
					out.write("success");
					out.flush();
					out.close();
					logger.debug("建行mispos 后台通知","建行mispos 后台通知交易成功");
					return PayStatus.PAY_SUCCESS;
				} catch (IOException e) {
					e.printStackTrace();
					logger.debug("建行mispos 后台通知","建行mispos 后台通知交易异常");
				}
				
		} catch (Exception e) {
			try {
				PrintWriter out = response.getWriter();
				response.setHeader("Cache-Control", "no-cache");
				out.write("fail");
				out.flush();
				out.close();
				logger.debug("建行mispos 后台通知","建行mispos 后台通知交易失败");
				return PayStatus.PAY_FAIL;
			}catch (Exception ee) {
				
			}
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
		// TODO Auto-generated method stub
		logger.debug("建行mis pos 退款 请求参数开始","建行mis pos 退款 请求参数开始");
		Refund refund = super.beforeRefund(request);
		if(null==refund){
			JSONObject json = new JSONObject();
			json.put("status", "0");
			json.put("errormsg", "already refund all");
			return json.toString();
		}
		OrderFormPay orderform = null;
		String corderid = request.getParameter("corderid");
		String companyid = request.getParameter("companyid");
		String refund_fee = request.getParameter("refund_fee");
		String refundfee = ((new BigDecimal(refund_fee.trim())).divide(new BigDecimal("100"))).toString();
		String newcorderid = request.getParameter("newcorderid");
		String sign = request.getParameter("sign");
		orderform = orderFormService.getOrderFormByCorderid(corderid, Integer.parseInt(companyid));
		if (null == orderform) {
			JSONObject json = new JSONObject();
			json.put("status", "0");
			json.put("errormsg", "refund no order");
			return json.toString();
		}
		PaymentBean payment = payTypeService.getPayType(orderform.getPaytype());
//		// 把请求参数打包成数组
		Map<String, String> m = new HashMap<String, String>();
		m.put("orderNum", orderform.getOrdernumber());
		m.put("corderid", corderid);
//		m.put("companyid", companyid);
		m.put("refund_fee", refundfee);
//		m.put("newcorderid", newcorderid);
		m.put("outerfundno", refund.getOuterfundno());
//		m.put("sign", sign);
		m.put("typeId", String.valueOf(payment.getTypeid()));
		
		logger.debug("建行mis pos 退款 请求参数",m.toString());
		// 建立请求
		//String sHtmlText = CCBMisPostPaySubmit.createHtml(payment.getExt1(), m);
		String sHtmlText = payment.getExt1()+"?typeId="+payment.getTypeid()+"&outerfundno="+refund.getOuterfundno()+"&refund_fee="+refundfee+"&corderid="+corderid+"&orderNum="+orderform.getOrdernumber();
		logger.debug("建行 mis post 建立退款请求参数结束", "建行 mis post建立退款请求结束:"+sHtmlText);
		response.sendRedirect(sHtmlText);
		
		JSONObject json = new JSONObject();
		json.put("status", "1");
		json.put("corderid", refund.getCorderid());
		json.put("money", refund.getRefundmoney());
		json.put("errormsg", "");
		return json.toString();
	}

	@Override
	public int refundNotify(HttpServletRequest request,
			HttpServletResponse response) {
		 logger.debug("建行mispos 退款后台通知开始","建行mispos 退款后台通知开始");
		try {
			Map reqParam = CCBMisPostPayUtil.getAllRequestParam(request);
			logger.debug("建行 mis pos 支付", "建行 mis pos 支付主动通知返回参数:"+reqParam.toString());
			String ordernumber = (String) reqParam.get("orderNum");
			int statu = PayStatus.REFUND_FAIL;
			logger.debug("建行mispos 退款后台通知","ordernumber="+ordernumber);
			if(null==ordernumber||!StringUtils.isNotBlank(ordernumber)){
				 PrintWriter out = response.getWriter();
				 response.setHeader("Cache-Control", "no-cache");
				 out.write("fail");
				 out.flush();
				 out.close();
				 return PayStatus.REFUND_FAIL;
			 }
			 afterRefund(ordernumber);
			 orderFormService.updatePayStatus(ordernumber, PayStatus.PAY_SUCCESS_REFUND);
			 statu = PayStatus.PAY_SUCCESS_REFUND;
			 PrintWriter out = response.getWriter();
			 response.setHeader("Cache-Control", "no-cache");
			 out.write("success");
			 out.flush();
			 out.close();
		     logger.debug("建行mispos 退款后台通知结束","建行mispos 退款后台通知结束---statu="+statu);
		     return statu;
		} catch (Exception e) {
			try {
				PrintWriter out = response.getWriter();
				response.setHeader("Cache-Control", "no-cache");
				out.write("fail");
				out.flush();
				out.close();
				logger.debug("建行mispos 退款后台通知失败","建行mispos 退款后台通知失败");
				return PayStatus.PAY_FAIL;
			}catch (Exception ee) {
				
			}
		}
		return PayStatus.PAY_FAIL;
		
		/*orderFormService.updatePayStatus(refund.getOrdernumber(), PayStatus.PAY_SUCCESS_REFUND);
		orderFormService.updateRefund(refund.getOuterfundno(), PayStatus.REFUND_SUCCESS);
		OrderFormPay orderFormPay = orderFormService.getOrderByOrderNum(refund.getOrdernumber());
		// kapstoy 调用消费接口
		HttpRequester httpRequester = new HttpRequester();
		HttpResponse httpResponse = null;
		String url = orderFormPay.getExt();
		url = url + "&corderid=" + orderFormPay.getCorderid();
		url=url+"&newcorderid="+orderFormPay.getMemo();
		BigDecimal aa=new BigDecimal(resultArray[1]).multiply(new BigDecimal(100));
		url = url + "&money=" +aa.intValue();
		String secKey = orderFormPay.getCompanyid() + SECKEY_SUFFIX;
		String signedSecKey = MD5Util.MD5Encode(secKey, "UTF-8");
		String md5Str = "corderid=" + orderFormPay.getCorderid() + "&" + signedSecKey + "&companyid=" + orderFormPay.getCompanyid();
		String md5Value = MD5Util.MD5Encode(md5Str, "UTF-8");
		url = url + "&sign=" + md5Value;
		try {
			httpResponse = httpRequester.sendGet(url.replaceAll("\\s", ""));
		} catch (IOException e) {
			logManager.debug("mispos", "mispos退款通知处理业务 server error url:" + url + ",corderid=" + orderFormPay.getCorderid()
					+ ",orderid=" + orderFormPay.getOrdernumber() + ", errormsg:" + e.getMessage());
			e.printStackTrace();
			return PayStatus.PAY_FAIL;
		}
		String content = httpResponse.getContent();
		if (content != null && content.equalsIgnoreCase("success")) {
			logManager.debug("mispos refund,", "mispos退款通知处理业务 成功 url:" + url + ",corderid=" + orderFormPay.getCorderid() );
			orderFormService.updateRefund(refund.getOuterfundno(), 3);//3的状态时退款成功，业务处理也成功
		}
		else
		{
			logManager.debug("mispos refund,", "mispos退款通知处理业务 失败 url:" + url + ",corderid=" +orderFormPay.getCorderid());
			return PayStatus.PAY_FAIL;
		}
		return PayStatus.PAY_SUCCESS; // 请不要修改或删除
*/		
		
	}

	@Override
	public List<Check> check(HttpServletRequest request,
			HttpServletResponse response, int payTypeId) throws Exception {
		return null;
	}

	@Override
	public OrderFormUnPay getOrderFormUnPay(HttpServletRequest request,
			HttpServletResponse response) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMisposPayParam(int payTypeId, String id,
			HttpServletRequest request, HttpServletResponse response) {

		StringBuilder sb = new StringBuilder();
		try{
			MisPosOrderFormUnPay misPosOrderFormUnPay = new MisPosOrderFormUnPay();
			String orderNum =request.getParameter("orderNum");
			if (StringUtils.isEmpty(orderNum)) {
				orderNum = (String) request.getAttribute("orderNum");
			}
			String corderid =request.getParameter("corderid");
			if (StringUtils.isEmpty(corderid)) {
				corderid = (String) request.getAttribute("corderid");
			}
			String transType = "S1";
			String money =request.getParameter("price");
			if (StringUtils.isEmpty(money)) {
				money = (String) request.getAttribute("price");
			}
			logger.debug("获取参数  ","id=="+id+"    price====="+money+"   corderid="+corderid+"   orderNum="+orderNum);
			String price = MoneyUtil.getPayMoney(money);
			String transAmount = StringUtil.leftPad(price, 12, '0');
			String loyalty = StringUtil.rightPad("", 12, ' ');
			String MisTrace = CCBMisPostPayUtil.MisTrace();
			String InstallmentTimes = StringUtil.rightPad("", 2, ' ');
			String oldAuthNo = StringUtil.rightPad("", 6, ' ');
			String oldPostrace = StringUtil.rightPad("", 6, ' ');
			String oldHostTrace = StringUtil.rightPad("", 12, ' ');
			String oldTransDate = StringUtil.rightPad("", 8, ' ');
			String cashPcNum = StringUtil.rightPad("001", 20, ' ');
			String cashierNum = StringUtil.rightPad("001", 20, ' ');
			
			misPosOrderFormUnPay.setId(id);
			misPosOrderFormUnPay.setCorderid(corderid);
			misPosOrderFormUnPay.setPaytype(payTypeId);
			misPosOrderFormUnPay.setOrdernumber(orderNum);
			misPosOrderFormUnPay.setTransAmount(price);
			misPosOrderFormUnPay.setTransType("S1");
			misPosOrderFormUnPay.setMisTrace(MisTrace);
			misPosOrderFormUnPay.setSubmitdate(DateUtil.getCurrentDateStr(DateUtil.C_TIME_PATTON_DEFAULT));
			misPosOrderFormUnPay.setStatus("2"); //未支付
			misPosOrderFormService.addUnPay(misPosOrderFormUnPay);
			
			sb.append(transType).append(transAmount).append(loyalty).append(MisTrace)
			.append(InstallmentTimes).append(oldAuthNo).append(oldPostrace).append(oldHostTrace)
			.append(oldTransDate).append(cashPcNum).append(cashierNum);
		}
		catch (Exception e) {
			logger.debug("获取支付参数失败","e.getmessgae()==="+ e.getMessage());
			return "";
		}
		return sb.toString();	
		
	}

	@Override
	public String getMisposRefundParam(int payTypeId, String refundId,
			HttpServletRequest request, HttpServletResponse response) {

		StringBuilder sb = new StringBuilder();
		try{
			String id =request.getParameter("id");
			if (StringUtils.isEmpty(id)) {
				id = (String) request.getAttribute("id");
			}
			String outerfundno =request.getParameter("outerfundno");
			if (StringUtils.isEmpty(outerfundno)) {
				outerfundno = (String) request.getAttribute("outerfundno");
			}
			String money =request.getParameter("price");
			if (StringUtils.isEmpty(money)) {
				money = (String) request.getAttribute("price");
			}
			logger.debug("获取退款参数  ","id=="+id+"    price=="+money);
			MisPosOrderFormUnPay misPosOrderFormUnPay = new MisPosOrderFormUnPay();
			MisPosOrderFormPay misPosOrderFormPay = misPosOrderFormService.findFormPayById(id);
			BeanUtils.copyProperties(misPosOrderFormUnPay, misPosOrderFormPay);
			misPosOrderFormUnPay.setId(refundId);
			misPosOrderFormUnPay.setOrid(id);
			misPosOrderFormUnPay.setSubmitdate(DateUtil.getCurrentDateStr(DateUtil.C_TIME_PATTON_DEFAULT));
			
			String price = MoneyUtil.getPayMoney(money);
			String transAmount = StringUtil.leftPad(price, 12, '0');
			String loyalty = StringUtil.rightPad("", 12, ' ');
			String MisTrace = CCBMisPostPayUtil.MisTrace();
			String InstallmentTimes = StringUtil.rightPad("", 2, ' ');
			String oldAuthNo = misPosOrderFormUnPay.getAuthorNum();
			String oldPostrace = misPosOrderFormUnPay.getPosTraceNum();
			String oldHostTrace = misPosOrderFormUnPay.getHostTrace();
			String oldTransDate = misPosOrderFormUnPay.getTransDat();
			String cashPcNum = StringUtil.rightPad("001", 20, ' ');
			String cashierNum = StringUtil.rightPad("001", 20, ' ');
			
			misPosOrderFormUnPay.setPaytype(payTypeId);
			misPosOrderFormUnPay.setTransType("S3");
			misPosOrderFormUnPay.setMisTrace(MisTrace);
			misPosOrderFormUnPay.setTransAmount(price);
			misPosOrderFormUnPay.setSubmitdate(DateUtil.getCurrentDateStr(DateUtil.C_TIME_PATTON_DEFAULT));
			misPosOrderFormUnPay.setExt1(outerfundno);
			misPosOrderFormUnPay.setStatus("4"); //未退款
			misPosOrderFormService.addUnPay(misPosOrderFormUnPay);
			
			sb.append("S3").append(transAmount).append(loyalty).append(MisTrace)
			.append(InstallmentTimes).append(oldAuthNo).append(oldPostrace).append(oldHostTrace)
			.append(oldTransDate).append(cashPcNum).append(cashierNum);
		}
		catch (Exception e) {
			logger.debug("获取支付参数失败","e.getmessgae()==="+ e.getMessage());
			return "";
		}
		return sb.toString();
	}

	@Override
	public String getMisposReprintParam(int payTypeId,
			HttpServletRequest request, HttpServletResponse response) {

		StringBuilder sb = new StringBuilder();
		try{
			String id =request.getParameter("id");
			if (StringUtils.isEmpty(id)) {
				id = (String) request.getAttribute("id");
			}
			MisPosOrderFormPay misPosOrderFormPay = new MisPosOrderFormPay();
			misPosOrderFormPay = misPosOrderFormService.findFormPayById(id);

			String transType = "Q3";
			String transAmount = StringUtil.leftPad("", 12, '0');
			String loyalty = StringUtil.rightPad("", 12, ' ');
			String MisTrace = "";
			if(misPosOrderFormPay!=null){
				MisTrace = misPosOrderFormPay.getPosTraceNum();
			}else{
				MisTrace = StringUtil.rightPad("", 6, ' ');
			}
			String InstallmentTimes = StringUtil.rightPad("", 2, ' ');
			String oldAuthNo = StringUtil.rightPad("", 6, ' ');
			String oldPostrace = StringUtil.rightPad("", 6, ' ');
			String oldHostTrace = StringUtil.rightPad("", 12, ' ');
			String oldTransDate = StringUtil.rightPad("", 8, ' ');
			String cashPcNum = StringUtil.rightPad("001", 20, ' ');
			String cashierNum = StringUtil.rightPad("001", 20, ' ');
			
			sb.append(transType).append(transAmount).append(loyalty).append(MisTrace)
			.append(InstallmentTimes).append(oldAuthNo).append(oldPostrace).append(oldHostTrace)
			.append(oldTransDate).append(cashPcNum).append(cashierNum);
		}
		catch (Exception e) {
			logger.debug("获取重打印参数失败","e.getmessgae()==="+ e.getMessage());
			return "";
		}
		return sb.toString();
	}

	@Override
	public String getMisposPayJson(int payTypeId, HttpServletRequest request,
			HttpServletResponse response) {

		try {
			String corderid =request.getParameter("corderid");
			if (StringUtils.isEmpty(corderid)) {
				corderid = (String) request.getAttribute("corderid");
			}
			List<MisPosOrderFormPay> payList = new ArrayList<MisPosOrderFormPay>();
			List<MisPosOrderFormPay> refundList = new ArrayList<MisPosOrderFormPay>();
			payList = misPosOrderFormService.getMisPosOrderFormPayByCorderid(corderid,payTypeId);//已经支付的信息
			refundList = misPosOrderFormService.getMisPosOrderFormPayRefundByCorderid(corderid,payTypeId);//已退款的信息
			JSONArray json = new JSONArray();
			BigDecimal rate =  new BigDecimal("100");
		    for(MisPosOrderFormPay a : payList){
		        JSONObject obj = new JSONObject();
		        BigDecimal totalRefund = new BigDecimal("0");
		        BigDecimal currentRefund = new BigDecimal("0");//当前退款
		        for(int i= 0;refundList!=null&&refundList.size()>0&&i<refundList.size();i++){
		        	MisPosOrderFormPay refundFormPay  = refundList.get(i);
		        	if(a.getId().equals(refundFormPay.getOrid())){
		        		totalRefund=totalRefund.add(new BigDecimal(refundFormPay.getTransAmount().trim()));
		        	}
		        	if(a.getId().equals(refundFormPay.getOrid())&&refundFormPay.getExt1()!=null&&!"".equals(refundFormPay.getExt1())){
		        		currentRefund=currentRefund.add(new BigDecimal(refundFormPay.getTransAmount().trim()));
		        	}
		        }
		        if(totalRefund.compareTo(new BigDecimal(a.getTransAmount().trim()))>=0){
		        	//continue;//已全部退完
		        }
		        obj.put("id", a.getId());
		        obj.put("transAmount",(new BigDecimal(a.getTransAmount().trim())).divide(rate));//总金额
		        obj.put("allowRefund", ((new BigDecimal(a.getTransAmount().trim())).subtract(totalRefund)).divide(rate));//允许退款的金额
		        obj.put("currentRefund", currentRefund.divide(rate));//当前已经退款金额
		        obj.put("cardName", a.getCardName());//对应行
		        obj.put("cardNameCode", a.getCardNameCode());//卡号
		        json.add(obj);
		    }
		    logger.debug("查询支付信息 返回json串","json.toString()=="+json.toString());
	 	    return json.toString();
	  } catch (Exception e) {
		return e.getMessage();
	  }
   }

	@Override
	public boolean saveMisposMsg(int payTypeId, HttpServletRequest request,
			HttpServletResponse response) {

		boolean flag =  false;
		try{
			String id =request.getParameter("id");
			if (StringUtils.isEmpty(id)) {
				id = (String) request.getAttribute("id");
			}
			MisPosOrderFormUnPay misPosOrderFormUnPay = misPosOrderFormService.findFormUnPayById(id);
			if(misPosOrderFormUnPay==null){
				if(null==misPosOrderFormService.findFormPayById(id)){
					return false;
				}else{
					return true;
				}
			}
			MisPosOrderFormPay misPosOrderFormPay = new MisPosOrderFormPay();
			BeanUtils.copyProperties(misPosOrderFormPay, misPosOrderFormUnPay);
			String msg =request.getParameter("msg");
			if (StringUtils.isEmpty(msg)) {
				msg = (String) request.getAttribute("msg");
			}
			logger.debug("----mispos支付结果返回信息信息  ----","id=="+id+"    msg====="+msg);
			if("00".equals(msg.substring(0, 2))){
				String str = CCBMisPostPayUtil.string_real(msg,"#");
				logger.debug("----转换后的字符串  ----","str====="+str);
				misPosOrderFormPay.setMerchantName(str.substring(44, 84).replaceAll("#", ""));
				misPosOrderFormPay.setMerchantNum(str.substring(84, 99).replaceAll("#", ""));
				misPosOrderFormPay.setTerminalNum(str.substring(99, 107).replaceAll("#", ""));
				misPosOrderFormPay.setTransCardNum(str.substring(107, 126).replaceAll("#", ""));
				misPosOrderFormPay.setExpDat(str.substring(126, 130).replaceAll("#", ""));
				misPosOrderFormPay.setBatchNum(str.substring(130, 136).replaceAll("#", ""));
				misPosOrderFormPay.setOldbatchNum(str.substring(136, 142).replaceAll("#", ""));
				misPosOrderFormPay.setPosTraceNum(str.substring(142, 148).replaceAll("#", ""));
				misPosOrderFormPay.setOldposTraceNum(str.substring(148, 154).replaceAll("#", ""));
				misPosOrderFormPay.setHostTrace(str.substring(154, 166).replaceAll("#", ""));
				misPosOrderFormPay.setSettleDat(str.substring(166, 174).replaceAll("#", ""));
				misPosOrderFormPay.setTransDat(str.substring(174, 182).replaceAll("#", ""));
				misPosOrderFormPay.setTransTim(str.substring(182, 188).replaceAll("#", ""));
				misPosOrderFormPay.setAuthorNum(str.substring(188, 194).replaceAll("#", ""));
				misPosOrderFormPay.setTransAmount(str.substring(194, 206).replaceAll("#", ""));
				misPosOrderFormPay.setLoyalty(str.substring(206, 218).replaceAll("#", ""));
				misPosOrderFormPay.setCardType(str.substring(218, 221).replaceAll("#", ""));
				misPosOrderFormPay.setCardNameCode(str.substring(221, 225).replaceAll("#", ""));
				misPosOrderFormPay.setCardName(str.substring(225, 245).replaceAll("#", ""));
				if(StringUtils.isEmpty(misPosOrderFormPay.getOrid())){
					misPosOrderFormPay.setStatus("1");//已支付
				}else{
					misPosOrderFormPay.setStatus("3");//已退款
				}
				misPosOrderFormPay.setExt((misPosOrderFormPay.getHostTrace()+misPosOrderFormPay.getTransDat()+misPosOrderFormPay.getTerminalNum()).replaceAll(" ", ""));
				misPosOrderFormService.addPay(misPosOrderFormPay);
				misPosOrderFormService.delUnPayById(id);
				flag = true;
			}
		}catch (Exception e) {
			logger.debug("保存mispos支付信息异常",e.getMessage());
			return flag;
		}
		return flag;
	}

	
	public  Map<String,List> misposCheck1(HttpServletRequest request,
			HttpServletResponse response, int payType) throws Exception {
		if (!beforeCheck(request)) {
			return null;
		}
		Map<String,List> map = new HashMap<String,List>();
		List<Check> listCheck = new ArrayList<Check>();
		List<Check> listCheckExt = new ArrayList<Check>();
		
		List<Check> listPay = new ArrayList<Check>();
		List<Check> listRefund = new ArrayList<Check>();
		
		String gmt_start_time = request.getParameter("start");
		// 要查询的结束时间
		String gmt_end_time = request.getParameter("end");

		List<Check> retList = new ArrayList<Check>();
		int dayBetween = DateUtil.daysBetween(DateUtil.parseDate(gmt_start_time), DateUtil.parseDate(gmt_end_time));
		if (dayBetween > 1) {
			for (int i = 0; i < dayBetween + 1; i++) {
				//根据日期构造文件的名称  获取文件   解析文件
				//File file = new File("http://114.251.247.103/ipay/file/misposFile/线上推送格式文件20150803.txt");
				File  file = new File("/export/home/SOHO/data/web/file/misposFile/线上推送格式文件20150803.txt");
				if(file.exists()){
				    logger.debug("读取对账文件开始","读取对账文件开始+listPay.size()=="+listPay.size());
					Map mapext = CCBMisPostPayUtil.getChecks1(file);	
					listPay.addAll((ArrayList)mapext.get("payList"));
					listRefund.addAll((ArrayList)mapext.get("refundList"));
				    logger.debug("读取对账文件结束","读取对账文件结束+listPay.size()=="+listPay.size()+"===listRefund.size()="+listRefund.size());
				}
			}
		} else {
			//根据日期构造文件的名称  获取文件   解析文件
			File file = new File("/export/home/SOHO/data/web/file/misposFile/线上推送格式文件20150803.txt");
			if(file.exists()){
				logger.debug("读取对账文件开始","读取对账文件开始+listPay.size()=="+listPay.size());
				Map mapext = CCBMisPostPayUtil.getChecks1(file);	
			    listPay.addAll((ArrayList)mapext.get("payList"));
			    listRefund.addAll((ArrayList)mapext.get("refundList"));
			    logger.debug("读取对账文件结束","读取对账文件结束+listPay.size()=="+listPay.size()+"===listRefund.size()="+listRefund.size());
			}
		}
		//子对账
		for(int i=0;i<listPay.size();i++ ){
			MisPosOrderFormPay orderFormPay = new MisPosOrderFormPay();
			Check check = listPay.get(i);

			orderFormPay = misPosOrderFormService.getMisPosOrderFormPayByExt(check.getExt());
			if(null!=orderFormPay){
				logger.debug("遍历对账文件 listPay寻找相应的pay","orderfromPay.getOrdernumber()="+orderFormPay.getOrdernumber());
				check.setOrdernumber(orderFormPay.getOrdernumber());
			}
		}
		for(int i=0;i<listRefund.size();i++ ){
			MisPosOrderFormPay orderFormPay = new MisPosOrderFormPay();
			Check check = listRefund.get(i);
			orderFormPay = misPosOrderFormService.getMisPosOrderFormPayByExt(check.getExt());
			if(null!=orderFormPay){
				logger.debug("遍历对账文件 listRefund寻找相应的pay","orderfromPay.getOrdernumber()="+orderFormPay.getOrdernumber());
				check.setOrdernumber(orderFormPay.getOrdernumber());
			}
		}
		//合并listpay   
		while(listPay!=null&&listPay.size()>0){
			Check check = new Check();
			BeanUtils.copyProperties(check, listPay.get(0));
			logger.debug("listPay中合并前的check======check.getTranseq()="+check.getTranseq(), "check.getFee()="+check.getFee()+"check.getPrice()="+check.getPrice());
			listCheck.add(listPay.get(0));
			listPay.remove(listPay.get(0));
			for(int i=0;i<listPay.size();i++){
				Check check1 = listPay.get(i);
				if(!"".equals(check.getOrdernumber())&&check.getOrdernumber().equals(check1.getOrdernumber())){
					check.setTranseq(check.getTranseq()+","+check1.getTranseq());
                    if(check.getTransdate().compareTo(check1.getTransdate())<0){
                    	check.setTransdate(check1.getTransdate());
                    	check.setOrdernumber(check1.getOrdernumber());
                    }
                    check.setFee(Double.toString(Double.parseDouble(check.getFee())+Double.parseDouble(check1.getFee())));
                    check.setPrice(Double.toString(Double.parseDouble(check.getPrice())+Double.parseDouble(check1.getPrice())));
                    check.setNetvalue(Double.toString(Double.parseDouble(check.getNetvalue())+Double.parseDouble(check1.getNetvalue())));
                    listCheck.add(check1);
                    listPay.remove(check1);
                    i--;
				}
			}
			logger.debug("listPay中合并后的check======check.getTranseq()="+check.getTranseq(), "check.getFee()="+check.getFee()+"check.getPrice()="+check.getPrice());
			listCheckExt.add(check);
		}
		//合并listRefund
		while(listRefund!=null&&listRefund.size()>0){
			Check check = new Check();
			BeanUtils.copyProperties(check, listRefund.get(0));
			listCheck.add(listRefund.get(0));
			listRefund.remove(listRefund.get(0));
			for(int i=0;i<listRefund.size();i++){
				Check check1 = listRefund.get(i);
				if(check.getOrdernumber().equals(check1.getOrdernumber())){
					check.setTranseq(check.getTranseq()+","+check1.getTranseq());
                    if(check.getTransdate().compareTo(check1.getTransdate())<0){
                    	check.setTransdate(check1.getTransdate());
                    	check.setOrdernumber(check1.getOrdernumber());
                    }
                    check.setFee(Double.toString(Double.parseDouble(check.getFee())+Double.parseDouble(check1.getFee())));
                    check.setPrice(Double.toString(Double.parseDouble(check.getPrice())+Double.parseDouble(check1.getPrice())));
                    check.setNetvalue(Double.toString(Double.parseDouble(check.getNetvalue())+Double.parseDouble(check1.getNetvalue())));
                    listCheck.add(check1);
                    listRefund.remove(check1);
                    i--;
				}
			}
			listCheckExt.add(check);
		}
		logger.debug("listCheck.size()="+listCheck.size(), "listCheckExt.size()="+listCheckExt.size());
		map.put("listCheck", listCheck);
		map.put("listCheckExt", listCheckExt);
		return map;
	}
	@Override
	public  Map<String,List> misposCheck(HttpServletRequest request,
			HttpServletResponse response, int payType) throws Exception {
		if (!beforeCheck(request)) {
			return null;
		}
		PaymentBean payment = payTypeService.getPayType(payType);
		Map<String,List> map = new HashMap<String,List>();
		List<Check> listCheck = new ArrayList<Check>();
		List<Check> listCheckExt = new ArrayList<Check>();
		
		List<Check> list = new ArrayList<Check>();
		String gmt_start_time = request.getParameter("start");
		Date date = DateUtil.parseDate(gmt_start_time);
		// 要查询的结束时间
		String gmt_end_time = request.getParameter("end");
		int dayBetween = DateUtil.daysBetween(date, DateUtil.parseDate(gmt_end_time));
		if (dayBetween > 1) {
			for (int i = 0; i < dayBetween + 1; i++) {
				if(i>0){
					date = DateUtil.addDate(gmt_start_time);
				}
				String dt =DateUtil.formatTime(date,"yyyyMMdd");
				String filePath = payment.getExt()+"文件头名"+dt+".txt";
				//根据日期构造文件的名称  获取文件   解析文件
				//File  file = new File("/export/home/SOHO/data/web/file/misposFile/线上推送格式文件20150803.txt");
				File file = new File(filePath);
				if(file.exists()){
				    logger.debug("读取对账文件开始","读取对账文件开始+list.size()=="+list.size());
				    List<Check> lt = CCBMisPostPayUtil.getChecks(file);	
					list.addAll(lt);
				    logger.debug("读取对账文件结束","读取对账文件结束+list.size()=="+list.size());
				}
			}
		} else {
			String dt =DateUtil.formatTime(date,"yyyyMMdd");
			String filePath = payment.getExt()+"文件头名"+dt+".txt";
			//根据日期构造文件的名称  获取文件   解析文件
			//File file = new File("/export/home/SOHO/data/web/file/misposFile/线上推送格式文件20150803.txt");
			File file = new File(filePath);
			if(file.exists()){
				 logger.debug("读取对账文件开始","读取对账文件开始+list.size()=="+list.size());
			     List<Check> rt = CCBMisPostPayUtil.getChecks(file);	
				 list.addAll(rt);
			     logger.debug("读取对账文件结束","读取对账文件结束+list.size()=="+list.size());
			}
		}
		//子对账
		for(int i=0;i<list.size();i++ ){
			MisPosOrderFormPay misposOrderFormPay = null;
			Check check = list.get(i);
			misposOrderFormPay = misPosOrderFormService.getMisPosOrderFormPayByExt(check.getExt());
			if(null!=misposOrderFormPay){
				logger.debug("遍历对账文件 list寻找相应的pay","misposOrderFormPay.getOrdernumber()="+misposOrderFormPay.getOrdernumber());
				check.setOrdernumber(misposOrderFormPay.getOrdernumber());
				if(misposOrderFormPay.getExt1()!=null&&!"".equals(misposOrderFormPay.getExt1())){
					check.setPaytype("S3");//退款
				}else {
					check.setPaytype("S1");//支付
				}
				OrderFormPay orderFormPay = payService.getOrderFormByOrderNumPay(misposOrderFormPay.getOrdernumber());
				if (orderFormPay != null&&"S1".equals(check.getPaytype())) {
	    			check.setCorderid(orderFormPay.getCorderid());//支付应收id
				}
				if (orderFormPay != null&&"S3".equals(check.getPaytype())) {
	    			check.setCorderid(orderFormPay.getMemo());//支付应收id
				}
			}
		}
		//合并list
		while(list!=null&&list.size()>0){
			Check check = new Check();
			BeanUtils.copyProperties(check, list.get(0));
			logger.debug("list中合并前的check======check.getTranseq()="+check.getTranseq(), "check.getFee()="+check.getFee()+"check.getPrice()="+check.getPrice());
			listCheck.add(list.get(0));
			list.remove(list.get(0));
			for(int i=0;i<list.size();i++){
				Check check1 = list.get(i);
				if(!"".equals(check.getOrdernumber())&&check.getOrdernumber().equals(check1.getOrdernumber())&&check.getPaytype().equals(check1.getPaytype())){
					check.setTranseq(check.getTranseq()+","+check1.getTranseq());
                    if(check.getTransdate().compareTo(check1.getTransdate())<0){
                    	check.setTransdate(check1.getTransdate());
                    	check.setOrdernumber(check1.getOrdernumber());
                    }
                    check.setFee(Double.toString(Double.parseDouble(check.getFee())+Double.parseDouble(check1.getFee())));
                    check.setPrice(Double.toString(Double.parseDouble(check.getPrice())+Double.parseDouble(check1.getPrice())));
                   // check.setNetvalue(Double.toString(Double.parseDouble(check.getNetvalue())+Double.parseDouble(check1.getNetvalue())));
                    listCheck.add(check1);
                    list.remove(check1);
                    i--;
				}
			}
			logger.debug("list中合并后的check======check.getTranseq()="+check.getTranseq(), "check.getFee()="+check.getFee()+"check.getPrice()="+check.getPrice());
			listCheckExt.add(check);
		}
		logger.debug("listCheck.size()="+listCheck.size(), "listCheckExt.size()="+listCheckExt.size());
		map.put("listCheck", listCheck);
		map.put("listCheckExt", listCheckExt);
		return map;
	}
	
}
