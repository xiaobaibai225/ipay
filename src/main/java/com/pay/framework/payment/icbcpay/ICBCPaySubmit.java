package com.pay.framework.payment.icbcpay;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;

import cn.com.infosec.icbc.ReturnValue;

import com.pay.framework.http.HttpRequester;
import com.pay.framework.http.HttpResponse;
import com.pay.framework.log.LogManager;
import com.pay.framework.payment.ccbpay.CCBPayUtil;
import com.pay.framework.payment.unionpay.util.DateStyle;
import com.pay.framework.util.XmlUtil;
import com.pay.model.Check;
import com.pay.model.PaymentBean;

public class ICBCPaySubmit {
	public static LogManager logger = LogManager.getLogger(ICBCPay.class);
	/**
	 * 构造HTTP POST交易表单的方法示例
	 * 
	 * @param action
	 *            表单提交地址
	 * @param hiddens
	 *            以MAP形式存储的表单键值
	 * @return 构造好的HTTP POST交易表单
	 */
	public static String createHtml(String action, Map<String, String> hiddens) {
		StringBuffer sf = new StringBuffer();
		sf.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=GBK\"/></head><body>");
		sf.append("<form id = \"pay_form_icbcpay\" action=\"" + action
				+ "\" method=\"post\">");
		if (null != hiddens && 0 != hiddens.size()) {
			Set<Entry<String, String>> set = hiddens.entrySet();
			Iterator<Entry<String, String>> it = set.iterator();
			while (it.hasNext()) {
				Entry<String, String> ey = it.next();
				String key = ey.getKey();
				String value = ey.getValue();
				sf.append("<input type=\"hidden\" name=\"" + key + "\" id=\""
						+ key + "\" value=\"" + value + "\"/>");
			}
		}
		sf.append("</form>");
		sf.append("</body>");
		sf.append("<script type=\"text/javascript\">");
		sf.append("document.all.pay_form_icbcpay.submit();");
		sf.append("</script>");
		sf.append("</html>");
		return sf.toString();
	}

	/**
	 * 获得签名数据，并提交请求
	 * 
	 * @param sParaTemp
	 * @param payment
	 * @return
	 */
	public static String refundSocketEBSS(Map<String, String> sParaTemp,
			PaymentBean payment) {
		String errorMsg = "";
		String ncip = payment.getExt1();// ip
		String ncEnPort = payment.getExt2();// 加密断开
		String ncVPort = payment.getExt3();// nc 验签端口
		String refundXml = ICBCPayUtil.getBackXmlStrByParams(sParaTemp);
		System.out.println("请求包:" + refundXml);
		// String result = ICBCPayUtil.socketICBCEBSS(ncip,
		// Integer.parseInt(ncVPort), refundXml,false);
		String url = "http://" + ncip + ":" + ncVPort;
		String resSignContent = ICBCPayUtil.httpPostRequest102(
				sParaTemp.get("fSeqno"), refundXml, url, "GBK", "sign",
				"ibcbb2c",null);
		logger.debug("工行银企互联", "退款签名报文---" + resSignContent);
		System.out.println(resSignContent + "----");

		// 解析响应报文
		// 解析签名报文
		String result = "";
		try {
			// HashMap<String, Object> signMap =
			// XmlProcessorUtils.packResMap(sParaTemp.get("fSeqno"),
			// resSignContent, "ibcbb2c",
			// BusinessConstant.PARA_CONFIG_BANKFLAG_T + "sign");
			// 提交http 请求
			HashMap<String, String> signMap = XmlProcessorUtils
					.getMap(resSignContent);
			result = (String) signMap.get("html/body/sign");
			logger.debug("工行银企互联", "退款签名数据:" + result);
		} catch (XmlUFCException e1) {
			e1.printStackTrace();
		}

		String orderSearchUrl = "http://" + ncip + ":" + ncEnPort
				+ "/servlet/ICBCCMPAPIReqServlet";
		Map mapOrderSearch = new HashMap();
		// TODO 构建请求
		//mapOrderSearch.put("userID", sParaTemp.get("ID"));// 证书ID
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSS");
		String SendTime = sdf.format(new Date());
		//mapOrderSearch.put("SendTime", SendTime);// 该请求交易时间 TODO 获取请求时间 ？
		mapOrderSearch.put("Version", "0.0.1.0");
		mapOrderSearch.put("TransCode", sParaTemp.get("TransCode"));// TransCode=交易代码（区分交易类型，每个交易固定)
		mapOrderSearch.put("BankCode",sParaTemp.get("BankCode"));//new String("蚕俭圃桩蓿皖型参钧".getBytes("GBK")) // BankCode=客户的归属单位  
		mapOrderSearch.put("GroupCIS", sParaTemp.get("CIS"));// GroupCIS=客户的归属编码 sParaTemp.get("CIS")
		mapOrderSearch.put("ID", sParaTemp.get("ID"));// 客户的证书ID（无证书客户可空)
		mapOrderSearch.put("PackageID", sParaTemp.get("fSeqno"));// PackageID=客户的指令包序列号（由客户ERP系统产生，不可重复),退款单id
		// 列ID”字段（PackageID）由企业产生，产生规则为当前日期（北京时间，格式为yyyyMMdd）＋7位序列号（例如200212230000001，为2002年12月23日发送的一个交易请求包的包序列ID）
		mapOrderSearch.put("Cert", "");// 客户的证书公钥信息（进行BASE64编码；NC客户送空)
		mapOrderSearch.put("reqData", result);// 签名后的客户请求数据
		logger.debug("工行银企互联", "请求数据" + mapOrderSearch.toString());
		System.out.println(mapOrderSearch.toString());
		
		//使用httpclient 请求
//		String resContent = ICBCPayUtil.httpPostRequest102(
//				sParaTemp.get("fSeqno"), refundXml, orderSearchUrl, "GBK", "res",
//				"ibcbb2c",mapOrderSearch);
//		System.out.println("----------------"+resContent+"-----");
		
		HttpRequester httpRequester = new HttpRequester();
		HttpResponse httpRsSearch;
		try {
			logger.debug("工行银企互联", "退货请求url:"+orderSearchUrl);
			httpRsSearch = httpRequester.sendPost(orderSearchUrl,mapOrderSearch);
			if (httpRsSearch.getCode() == 200) {
				// 获得响应报文，
				String refundContent = httpRsSearch.getContent();
				logger.debug("工行支付", "工行订单查询返回的报文" + refundContent);
				System.out.println("refundContent===" + refundContent);

				// 如果返回error code 处理
				if (refundContent.contains("errorCode=")) {
					// TODO 处理异常
				} else {
					// TODO 验证返回数据
					// 1.64 解码
					String reqData = refundContent.split("reqData=")[1];
					logger.debug("工行退款", "退款响应的reqData：" + reqData);
					byte[] reqDataByte = reqData.getBytes();
					byte[] reqDataDe = ReturnValue.base64dec(reqDataByte);
					String reqDataStr = new String(reqDataDe, "GBK");
					logger.debug("工行退款", "退款响应的reqData解码后：" + reqDataStr);
					System.out.println(reqDataStr);
					
					// 解析xml文件，
					Map refundReturnMap = ICBCPayUtil.readStringXmlOut(reqDataStr);
					String RetCode = (String)refundReturnMap.get("RetCode");
					if("0".equals(RetCode)){
						// 退款成功
						errorMsg = "success";
					}else{
						// 退款失败
						String RetMsg = (String)refundReturnMap.get("RetMsg");
						errorMsg = errorMsg;
					}
				}
				// 解析订单查询报文
			} else {
				System.out.println("1111");
				logger.debug("工行支付", "中行订单查询失败");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return errorMsg;
	}

	public static List<Check> checkSocketEBSS(Map<String, String> sParaTemp,
			PaymentBean payment,String searchflag) throws Exception {
		List<Check> checklist = new ArrayList<Check>();
		// 获取兑账请求socket xml字符串
		String ncip = payment.getExt1();// ip
		String ncEnPort = payment.getExt2();// 加密断开
		String sendXml = ICBCPayUtil.getQueryXmlStrByParams(sParaTemp);
		if("1".equals(searchflag)){//订购查询
			sendXml = ICBCPayUtil.getQueryXmlStrByParams(sParaTemp);
		}else if("2".equals(searchflag)){//退货查询
			sendXml = ICBCPayUtil.getBackB2CEFEINFXmlStrByParams(sParaTemp);
		}
		System.out.println("请求报文："+sendXml);
		// http 请求对账接口
		String orderSearchUrl = "http://" + ncip + ":" + ncEnPort
				+ "/servlet/ICBCCMPAPIReqServlet";
		Map mapOrderSearch = new HashMap();
		// TODO 构建请求
		//mapOrderSearch.put("userID", "证书ID");
		//mapOrderSearch.put("PackageID", "包序列ID");
		//mapOrderSearch.put("SendTime", "请求时间");
		mapOrderSearch.put("Version", "0.0.1.0");
		mapOrderSearch.put("TransCode",sParaTemp.get("TransCode"));
		mapOrderSearch.put("BankCode", payment.getExt5());
		mapOrderSearch.put("GroupCIS", payment.getExt6());
		mapOrderSearch.put("ID", payment.getExt4());
		mapOrderSearch.put("PackageID", sParaTemp.get("fSeqno"));//TODO  指令包序列号
		mapOrderSearch.put("Cert", "");// 客户的证书公钥信息（进行BASE64编码；NC客户送空)
		mapOrderSearch.put("reqData", sendXml);// 签名后的客户请求数据

		Map propertes = new HashMap();
		propertes.put("Content-Type", "application/x-www-form-urlencoded");

		HttpRequester httpRequester = new HttpRequester();
		HttpResponse httpRsSearch;
		try {
			httpRsSearch = httpRequester.sendPost(orderSearchUrl,
					mapOrderSearch, propertes);
			if (httpRsSearch.getCode() == 200) {
				// 获得响应报文，
				String refundContent = httpRsSearch.getContent();
				logger.debug("工行支付", "工行支付订单查询返回的报文" + refundContent);
				System.out.println("工行支付订单查询返回的报文" + refundContent);
				// 如果返回error code 处理
				if (refundContent.contains("errorCode=")) {
					// TODO 处理异常
				} else {
					// TODO 验证返回数据
					String reqData = refundContent.split("reqData=")[1];
					logger.debug("工行退款", "退款响应的reqData：" + reqData);
					byte[] reqDataByte = reqData.getBytes();
					byte[] reqDataDe = ReturnValue.base64dec(reqDataByte);
					String reqDataStr = new String(reqDataDe, "GBK");
					logger.debug("工行退款", "退款响应的reqData解码后：" + reqDataStr);
					System.out.println(reqDataStr);
					// 查询成功
					// 解析xml文件，
					Document doc = DocumentHelper.parseText(reqDataStr);
					// 获得rd节点，转化成list
					Map<String, Object> map = ICBCPayUtil.Dom2Map(doc);
					logger.debug("工行对账", "对账map"+map.toString());
					System.out.println(map.toString());
					Map ebmap = (Map)map.get("eb");
					Map pubMap = (Map)ebmap.get("pub");
					Map outMap = (Map)ebmap.get("out");
					
					String RetCode = (String)pubMap.get("RetCode");
					if("0".equals(RetCode)){
						// 获取对账文件成功。
						if(outMap.get("rd") instanceof java.util.AbstractList){
							List list = (ArrayList)outMap.get("rd");
							Iterator it = list.iterator();
							while(it.hasNext()){
								Map tmp = (HashMap)it.next();
								Check check = new Check();
								
								check.setOrdernumber((String)tmp.get("OrderNum"));
								if("1".equals(searchflag)){
									String money = (String)tmp.get("OrderAmt");
									String m  = BigDecimal.valueOf(Long.valueOf(money)).divide(new BigDecimal(100)).toString();
									String td=com.pay.framework.payment.unionpay.util.DateUtil.DateToString(com.pay.framework.payment.unionpay.util.DateUtil.StringToDate((String)tmp.get("TranTime"), DateStyle.YYYYMMDDHHMMSSCN),DateStyle.YYYY_MM_DD_HH_MM_SS);
									check.setTransdate(td);
									check.setPrice(""+m);
									check.setNetvalue(""+m);
								}else{
									String money = (String)tmp.get("TranAmt");
									String m  = BigDecimal.valueOf(Long.valueOf(money)).divide(new BigDecimal(100)).toString();
									String td=com.pay.framework.payment.unionpay.util.DateUtil.DateToString(com.pay.framework.payment.unionpay.util.DateUtil.StringToDate(((String)tmp.get("TranTime"))+"00", DateStyle.YYYYMMDDHHMMSSCN),DateStyle.YYYY_MM_DD_HH_MM_SS);
									check.setTransdate(td);
									check.setPrice("-"+m);
									check.setNetvalue("-"+m);
								}
								check.setTranseq((String)tmp.get("SerialNo"));
								check.setBank("ICBC");
								check.setAccountno((String)tmp.get("PayAccNo"));
								check.setAccountname((String)tmp.get("PayAccNameCN"));
								check.setFee("0.00");
								check.setPaytype(""+payment.getTypeid());
								checklist.add(check);
							}
						}else{
							Map tmp = (HashMap)outMap.get("rd");
							Check check = new Check();
							check.setOrdernumber((String)tmp.get("OrderNum"));
							if("1".equals(searchflag)){
								String money = (String)tmp.get("OrderAmt");
								String m  = BigDecimal.valueOf(Long.valueOf(money)).divide(new BigDecimal(100)).toString();
								check.setPrice(""+m);
								check.setNetvalue(""+m);
								String td=com.pay.framework.payment.unionpay.util.DateUtil.DateToString(com.pay.framework.payment.unionpay.util.DateUtil.StringToDate((String)tmp.get("TranTime"), DateStyle.YYYYMMDDHHMMSSCN),DateStyle.YYYY_MM_DD_HH_MM_SS);
								check.setTransdate(td);
							}else{
								String money = (String)tmp.get("TranAmt");
								String m  = BigDecimal.valueOf(Long.valueOf(money)).divide(new BigDecimal(100)).toString();
								check.setPrice("-"+m);
								check.setNetvalue("-"+m);
								String td=com.pay.framework.payment.unionpay.util.DateUtil.DateToString(com.pay.framework.payment.unionpay.util.DateUtil.StringToDate(((String)tmp.get("TranTime"))+"00", DateStyle.YYYYMMDDHHMMSSCN),DateStyle.YYYY_MM_DD_HH_MM_SS);
								check.setTransdate(td);
							}
							check.setTranseq((String)tmp.get("SerialNo"));
							check.setBank("ICBC");
							check.setAccountno((String)tmp.get("PayAccNo"));
							check.setAccountname((String)tmp.get("PayAccNameCN"));
							check.setFee("0.00");
							
							check.setPaytype(""+payment.getTypeid());
							checklist.add(check);
						}
						
						
					}else{
						// 获取对账文件失败
						String RetMsg = (String)pubMap.get("RetMsg");
						
					}
				}
				// 解析订单查询报文
			} else {
				logger.debug("工行银企互联", "工行对账失败");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return checklist;
	}
	
	public static List<Check> checkSocketEBSSB2B(Map<String, String> sParaTemp,
			PaymentBean payment,String searchflag) throws Exception {
		List<Check> checklist = new ArrayList<Check>();
		// 获取兑账请求socket xml字符串
		String ncip = payment.getExt1();// ip
		String ncEnPort = payment.getExt2();// 加密断开
		String sendXml = ICBCPayUtil.getQueryXmlStrByParamsB2B(sParaTemp);
		if("1".equals(searchflag)){//订购查询
			sendXml = ICBCPayUtil.getQueryXmlStrByParamsB2B(sParaTemp);
		}else if("2".equals(searchflag)){//退货查询
			sendXml = ICBCPayUtil.getBackB2CEFEINFXmlStrByParamsB2B(sParaTemp);
		}
		System.out.println("请求报文："+sendXml);
		// http 请求对账接口
		String orderSearchUrl = "http://" + ncip + ":" + ncEnPort
				+ "/servlet/ICBCCMPAPIReqServlet";
		Map mapOrderSearch = new HashMap();
		// TODO 构建请求
		//mapOrderSearch.put("userID", "证书ID");
		//mapOrderSearch.put("PackageID", "包序列ID");
		//mapOrderSearch.put("SendTime", "请求时间");
		mapOrderSearch.put("Version", "0.0.0.1");
		mapOrderSearch.put("TransCode",sParaTemp.get("TransCode"));
		mapOrderSearch.put("BankCode", payment.getExt5());
		mapOrderSearch.put("GroupCIS", payment.getExt6());
		mapOrderSearch.put("ID", payment.getExt4());
		mapOrderSearch.put("PackageID", sParaTemp.get("fSeqno"));//TODO  指令包序列号
		mapOrderSearch.put("Cert", "");// 客户的证书公钥信息（进行BASE64编码；NC客户送空)
		mapOrderSearch.put("reqData", sendXml);// 签名后的客户请求数据

		Map propertes = new HashMap();
		propertes.put("Content-Type", "application/x-www-form-urlencoded");

		HttpRequester httpRequester = new HttpRequester();
		HttpResponse httpRsSearch;
		try {
			httpRsSearch = httpRequester.sendPost(orderSearchUrl,
					mapOrderSearch, propertes);
			if (httpRsSearch.getCode() == 200) {
				// 获得响应报文，
				String refundContent = httpRsSearch.getContent();
				logger.debug("工行支付", "工行支付订单查询返回的报文" + refundContent);
				System.out.println("工行支付订单查询返回的报文" + refundContent);
				// 如果返回error code 处理
				if (refundContent.contains("errorCode=")) {
					// TODO 处理异常
				} else {
					// TODO 验证返回数据
					String reqData = refundContent.split("reqData=")[1];
					logger.debug("工行退款", "退款响应的reqData：" + reqData);
					byte[] reqDataByte = reqData.getBytes();
					byte[] reqDataDe = ReturnValue.base64dec(reqDataByte);
					String reqDataStr = new String(reqDataDe, "GBK");
					logger.debug("工行退款", "退款响应的reqData解码后：" + reqDataStr);
					System.out.println(reqDataStr);
					// 查询成功
					// 解析xml文件，
					Document doc = DocumentHelper.parseText(reqDataStr);
					// 获得rd节点，转化成list
					Map<String, Object> map = ICBCPayUtil.Dom2Map(doc);
					logger.debug("工行对账", "对账map"+map.toString());
					System.out.println(map.toString());
					Map ebmap = (Map)map.get("eb");
					Map pubMap = (Map)ebmap.get("pub");
					Map outMap = (Map)ebmap.get("out");
					
					String RetCode = (String)pubMap.get("RetCode");
					if("0".equals(RetCode)){
						// 获取对账文件成功。
						if(outMap.get("rd") instanceof java.util.AbstractList){
							List list = (ArrayList)outMap.get("rd");
							Iterator it = list.iterator();
							while(it.hasNext()){
								Map tmp = (HashMap)it.next();
								Check check = new Check();
								
								check.setOrdernumber((String)tmp.get("OrderNum"));
								if("1".equals(searchflag)){
									String money = (String)tmp.get("OrderAmt");
									String m  = BigDecimal.valueOf(Long.valueOf(money)).divide(new BigDecimal(100)).toString();
									String td=com.pay.framework.payment.unionpay.util.DateUtil.DateToString(com.pay.framework.payment.unionpay.util.DateUtil.StringToDate((String)tmp.get("TranTime"), DateStyle.YYYYMMDDHHMMSSCN),DateStyle.YYYY_MM_DD_HH_MM_SS);
									check.setTransdate(td);
									check.setPrice(""+m);
									check.setNetvalue(""+m);
								}else{
									String money = (String)tmp.get("TranAmt");
									String m  = BigDecimal.valueOf(Long.valueOf(money)).divide(new BigDecimal(100)).toString();
									String td=com.pay.framework.payment.unionpay.util.DateUtil.DateToString(com.pay.framework.payment.unionpay.util.DateUtil.StringToDate(((String)tmp.get("TranTime"))+"00", DateStyle.YYYYMMDDHHMMSSCN),DateStyle.YYYY_MM_DD_HH_MM_SS);
									check.setTransdate(td);
									check.setPrice("-"+m);
									check.setNetvalue("-"+m);
								}
								check.setTranseq((String)tmp.get("SerialNo"));
								check.setBank("ICBC");
								check.setAccountno((String)tmp.get("PayAccNo"));
								check.setAccountname((String)tmp.get("PayAccNameCN"));
								check.setFee("0.00");
								check.setPaytype(""+payment.getTypeid());
								checklist.add(check);
							}
						}else{
							Map tmp = (HashMap)outMap.get("rd");
							Check check = new Check();
							check.setOrdernumber((String)tmp.get("OrderNum"));
							if("1".equals(searchflag)){
								String money = (String)tmp.get("OrderAmt");
								String m  = BigDecimal.valueOf(Long.valueOf(money)).divide(new BigDecimal(100)).toString();
								check.setPrice(""+m);
								check.setNetvalue(""+m);
								String td=com.pay.framework.payment.unionpay.util.DateUtil.DateToString(com.pay.framework.payment.unionpay.util.DateUtil.StringToDate((String)tmp.get("TranTime"), DateStyle.YYYYMMDDHHMMSSCN),DateStyle.YYYY_MM_DD_HH_MM_SS);
								check.setTransdate(td);
							}else{
								String money = (String)tmp.get("TranAmt");
								String m  = BigDecimal.valueOf(Long.valueOf(money)).divide(new BigDecimal(100)).toString();
								check.setPrice("-"+m);
								check.setNetvalue("-"+m);
								String td=com.pay.framework.payment.unionpay.util.DateUtil.DateToString(com.pay.framework.payment.unionpay.util.DateUtil.StringToDate(((String)tmp.get("TranTime"))+"00", DateStyle.YYYYMMDDHHMMSSCN),DateStyle.YYYY_MM_DD_HH_MM_SS);
								check.setTransdate(td);
							}
							check.setTranseq((String)tmp.get("SerialNo"));
							check.setBank("ICBC");
							check.setAccountno((String)tmp.get("PayAccNo"));
							check.setAccountname((String)tmp.get("PayAccNameCN"));
							check.setFee("0.00");
							
							check.setPaytype(""+payment.getTypeid());
							checklist.add(check);
						}
						
						
					}else{
						// 获取对账文件失败
						String RetMsg = (String)pubMap.get("RetMsg");
						
					}
				}
				// 解析订单查询报文
			} else {
				logger.debug("工行银企互联", "工行对账失败");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return checklist;
	}
	
}
