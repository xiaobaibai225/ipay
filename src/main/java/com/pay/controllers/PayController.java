package com.pay.controllers;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.pay.framework.log.LogManager;
import com.pay.framework.payment.BasePay;
import com.pay.framework.util.MD5Util;
import com.pay.model.OrderFormUnPay;
import com.pay.service.PayService;

/**
 * 支付通用接口
 * 
 * @author PCCW
 * 
 */
@Controller
@RequestMapping("pay")
public class PayController {
	@Autowired
	private PayService payServiceExt;
	private static LogManager logger = LogManager
			.getLogger(PayController.class);
	private static final String LOG_KEY = "PayController";
	// 重新支付的url TODO 写到配置文件
	private static final String ERROR_PAGE_URL = "/500.jsp";

	/**
	 * 支付通用方法
	 * 
	 * 支付类型
	 * 
	 * @param request
	 * @return 没有返回
	 */
	@RequestMapping(value = "/{pay_type_id}", method = { RequestMethod.POST,
			RequestMethod.GET })
	public String pay(@PathVariable int pay_type_id,
			HttpServletRequest request, HttpServletResponse response) {
		boolean result = false;
		result = payServiceExt.pay(pay_type_id, request, response);
		if (result) {
			try {
				response.setContentType("text/html");
				PrintWriter out;
				out = response.getWriter();
				JSONObject json = new JSONObject();
				if (result) {
					json.accumulate("success", true);
				} else {
					json.accumulate("success", false);
				}
				out.println(json.toString());
				out.flush();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {
			return "redirect:" +ERROR_PAGE_URL;
		}
		return null;
	}
	
	/**
	 * 构造mispos付款参数
	 * 支付类型
	 * @param request
	 * 
	 */
	@RequestMapping(value = "/getMisposPayParam/{pay_type_id}", method = { RequestMethod.POST,
			RequestMethod.GET })
	public @ResponseBody
	String getMisposPayParam(@PathVariable int pay_type_id,HttpServletRequest request, HttpServletResponse response) {
		JSONObject json = new JSONObject();
		String result = "";
		try {
			String id = UUID.randomUUID().toString();
			json.put("id", id);
			result = payServiceExt.getMisposPayParam(pay_type_id,id,request, response);
		} catch (Exception e) {
			logger.debug(LOG_KEY+"-getMisposPayParam", e.getMessage());
			json.put("flag", false);
			json.put("msg","系统异常");
			return json.toString();
		}
		if(StringUtils.isEmpty(result)){
			json.put("flag",false);
			json.put("msg", "系统异常");
		}else{
			json.put("flag", true);
			json.put("msg", result);
		}
		return json.toString();
	}
	
	/**
	 * 构造mispos退款参数
	 * 支付类型
	 * @param request
	 * 
	 */
	@RequestMapping(value = "/getMisposRefundParam/{pay_type_id}", method = { RequestMethod.POST,
			RequestMethod.GET })
	public @ResponseBody
	String getMisposRefundParam(@PathVariable int pay_type_id,HttpServletRequest request, HttpServletResponse response) {
		JSONObject json = new JSONObject();
		String result = "";
		try {
			String refundId = UUID.randomUUID().toString();
			json.put("refundId", refundId);
			result = payServiceExt.getMisposRefundParam(pay_type_id,refundId,request, response);
		} catch (Exception e) {
			logger.debug(LOG_KEY+"-getMisposRefundParam", e.getMessage());
			json.put("flag", false);
			json.put("msg","系统异常");
			return json.toString();
		}
		if(StringUtils.isEmpty(result)){
			json.put("flag",false);
			json.put("msg", "系统异常");
		}else{
			json.put("flag", true);
			json.put("msg", result);
		}
		return json.toString();
	}
	
	/**
	 * 构造mispos重打印小票参数
	 * 支付类型
	 * @param request
	 * 
	 */
	@RequestMapping(value = "/getMisposReprintParam/{pay_type_id}", method = { RequestMethod.POST,
			RequestMethod.GET })
	public @ResponseBody
	String getMisposReprintParam(@PathVariable int pay_type_id,HttpServletRequest request, HttpServletResponse response) {
		JSONObject json = new JSONObject();
		String result = "";
		try {
			result = payServiceExt.getMisposReprintParam(pay_type_id,request, response);
		} catch (Exception e) {
			logger.debug(LOG_KEY+"-getMisposReprintParam", e.getMessage());
			json.put("flag", false);
			json.put("msg","系统异常");
			return json.toString();
		}
		if(StringUtils.isEmpty(result)){
			json.put("flag",false);
			json.put("msg", "系统异常");
		}else{
			json.put("flag", true);
			json.put("msg", result);
		}
		return json.toString();
	}
	
	/**
	 * 获取mispos支付信息
	 * 
	 * @param request
	 * 
	 */
	@RequestMapping(value = "/getMisposPayJson/{pay_type_id}", method = { RequestMethod.POST,
			RequestMethod.GET })
	public @ResponseBody
	String getMisposPayJson(@PathVariable int pay_type_id,HttpServletRequest request, HttpServletResponse response) {
		String result = "";
		try {
			result = payServiceExt.getMisposPayJson(pay_type_id,request, response);
			request.setCharacterEncoding("utf-8");  //这里不设置编码会有乱码
            response.setContentType("text/html;charset=utf-8");
            response.setHeader("Cache-Control", "no-cache");  
            PrintWriter out = response.getWriter();  //输出中文，这一句一定要放到response.setContentType("text/html;charset=utf-8"),  response.setHeader("Cache-Control", "no-cache")后面，否则中文返回到页面是乱码  
            out.print(result);
            out.flush();
            out.close();
		} catch (Exception e) {
			logger.debug(LOG_KEY+"-getMisposPayJson", e.getMessage());
			return null;
		}
		return null;
	}
	
	/**
	 * 保存mispos返回信息
	 * 即 保存交易小票信息
	 * 支付类型
	 * @param request
	 * 
	 */
	@RequestMapping(value = "/saveMisposMsg/{pay_type_id}", method = { RequestMethod.POST,
			RequestMethod.GET })
	public @ResponseBody
	String saveMisposMsg(@PathVariable int pay_type_id,HttpServletRequest request, HttpServletResponse response) {
		boolean result = false;
		try {
			result = payServiceExt.saveMisposMsg(pay_type_id,request, response);
		} catch (Exception e) {
			logger.debug(LOG_KEY+"-saveMisposMsg", e.getMessage());
			return "";
		}
		return result?"success":"";
	}
	
	
	/**
	 * 移动支付返回JSON数据
	 * @param pay_type_id
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/mobile/{pay_type_id}", method = {
			RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody
	String mobile(@PathVariable int pay_type_id, HttpServletRequest request,
			HttpServletResponse response) {
		String result = "";
		try {
			result = payServiceExt.getMobileForwardParameter(pay_type_id,
					request, response);

		} catch (Exception e) {
			JSONObject json = new JSONObject();
			e.printStackTrace();
			json.put("status", 0);
			json.put("errormsg", e.getMessage());
			return json.toString();
		}
		return result;
	}
	
	/**
	 * 手机支付结果异步通知
	 * @param pay_type_id
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/mobile/servernotify/{pay_type_id}", method = { RequestMethod.POST, RequestMethod.GET })
	public @ResponseBody
	String mobileServerNotify(@PathVariable int pay_type_id, HttpServletRequest request, HttpServletResponse response) {
		boolean result = false;
		result = payServiceExt.serverMobileNotify(pay_type_id, request, response);
		if (!result) {
			return "服务通知出现错误！";
		}
		return null;
	}

	/**
	 * 支付结果异步通知
	 * @param pay_type_id
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/servernotify/{pay_type_id}", method = {
			RequestMethod.POST, RequestMethod.GET })
	public String serverNotify(@PathVariable int pay_type_id,
			HttpServletRequest request, HttpServletResponse response) {
		boolean result = false;
		result = payServiceExt.serverNotify(pay_type_id, request, response);
		// 手机话费支付返回响应数据
		if (result) {
			try {
				response.setContentType("text/html");
				PrintWriter out;
				out = response.getWriter();
				out.println("true");
				out.flush();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
				logger.error(LOG_KEY, e.getMessage(), e);
			}
		}
		return null;
	}

	/**
	 * 退款异步通知
	 * @param pay_type_id
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/refundnotify/{pay_type_id}", method = {
			RequestMethod.POST, RequestMethod.GET })
	public String refundNotify(@PathVariable int pay_type_id,
			HttpServletRequest request, HttpServletResponse response) {
		boolean result = false;
		result = payServiceExt.refundNotify(pay_type_id, request, response);
		if (result) {
			try {
				response.setContentType("text/html");
				PrintWriter out;
				out = response.getWriter();
				out.println("success");
				out.flush();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
				logger.error(LOG_KEY, e.getMessage(), e);
			}
		}
		return null;
	}

	/**
	 * 页面同步通知
	 * @param pay_type_id
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/pagenotify/{pay_type_id}", method = {
			RequestMethod.POST, RequestMethod.GET })
	public String pageNotify(@PathVariable int pay_type_id,
			HttpServletRequest request, HttpServletResponse response) {
		boolean result = payServiceExt.pageNotify(pay_type_id, request,
				response);
		
		if (!result) {
			// 跳转到支付延时页面
			try {
				OrderFormUnPay order=payServiceExt.getOrderFormUnPay(pay_type_id, request, response);
				response.sendRedirect(order.getFronturl()+"&delayFlag=Y");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 退款请求
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/refund", method = RequestMethod.GET)
	public @ResponseBody
	String refund(HttpServletRequest request, HttpServletResponse response) {
		String result = "";
		try {
			result = payServiceExt.refund(request, response);
			if (result == null) {
				return "redirect:" +ERROR_PAGE_URL;
			}
		} catch (Exception e) {
 			JSONObject json = new JSONObject();
			e.printStackTrace();
			json.put("status", 0);
			json.put("errormsg", e.getMessage());
			return json.toString();
		}
		return result;
	}

	/**
	 * 查询交易状态
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/querystat", method = RequestMethod.GET)
	public @ResponseBody
	String querystat(HttpServletRequest request, HttpServletResponse response) {
		String result = "";
		try {
			String corderid = request.getParameter("corderid");
			String companyid = request.getParameter("companyid");
			String sign = request.getParameter("sign");
			String secKey = companyid + BasePay.SECKEY_SUFFIX;
			String signedSecKey = MD5Util.MD5Encode(secKey, "UTF-8");
			String MD5Value = MD5Util.MD5Encode("corderid=" + corderid + "&"
					+ signedSecKey + "&companyid=" + companyid, "UTF-8");
			if (sign == null || !sign.equalsIgnoreCase(MD5Value)) {
				JSONObject json = new JSONObject();
				json.put("status", 0);
				json.put("corderid", corderid);
				json.put("errormsg", "illegal sign");
				return json.toString();
			}
			result = payServiceExt.querystat(request, response);
		} catch (Exception e) {
			JSONObject json = new JSONObject();
			json.put("status", 0);
			json.put("errormsg", "error corderid or sign");
			return json.toString();
		}
		return result;
	}

	/**
	 * 对账请求
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/check", method = RequestMethod.GET)
	public @ResponseBody
	String check(HttpServletRequest request, HttpServletResponse response) {
		String result = "";
		try {
			result = payServiceExt.check(request, response);
		} catch (Exception e) {
			e.printStackTrace();
			JSONObject json = new JSONObject();
			json.put("status", 0);
			json.put("errormsg", "illegal sign");
			return json.toString();
		}
		return result;
	}

	/**
	 * MISPOS对账请求
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/misposCheck", method = RequestMethod.GET)
	public @ResponseBody
	String misposCheck(HttpServletRequest request, HttpServletResponse response) {
		String result = "";
		try {
			result = payServiceExt.misposCheck(request, response);
		} catch (Exception e) {
			e.printStackTrace();
			JSONObject json = new JSONObject();
			json.put("status", 0);
			json.put("errormsg", "illegal sign");
			return json.toString();
		}
		return result;
	}
	
	/**
	 * 查询支付状态
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/searchPayStatus", method = RequestMethod.GET)
	public @ResponseBody
	String searchPayStatus(HttpServletRequest request, HttpServletResponse response) {
		String result = "";
		try {
			result = payServiceExt.searchPayStatus(request, response);
		} catch (Exception e) {
			e.printStackTrace();
			JSONObject json = new JSONObject();
			json.put("status", 0);
			json.put("errormsg", "illegal sign");
			return json.toString();
		}
		return result; 
	}
}
