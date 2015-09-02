package com.pay.framework.payment.alipay.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.NameValuePair;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import com.pay.framework.log.LogManager;
import com.pay.framework.payment.alipay.config.AlipayConfig;
import com.pay.framework.payment.alipay.sign.MD5;
import com.pay.framework.payment.alipay.sign.RSASignature;
import com.pay.framework.payment.alipay.util.httpClient.HttpProtocolHandler;
import com.pay.framework.payment.alipay.util.httpClient.HttpRequest;
import com.pay.framework.payment.alipay.util.httpClient.HttpResponse;
import com.pay.framework.payment.alipay.util.httpClient.HttpResultType;

/* *
 *类名：AlipaySubmit
 *功能：支付宝各接口请求提交类
 *详细：构造支付宝各接口表单HTML文本，获取远程HTTP数据
 *版本：3.3
 *日期：2012-08-13
 *说明：
 *以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己网站的需要，按照技术文档编写,并非一定要使用该代码。
 *该代码仅供学习和研究支付宝接口使用，只是提供一个参考。
 */

public class AlipaySubmit {

	private static LogManager logManager = LogManager.getLogger(AlipaySubmit.class);

	/**
	 * 生成签名结果
	 * 
	 * @param sPara
	 *            要签名的数组
	 * @return 签名结果字符串
	 */
	public static String buildRequestMysign(Map<String, String> sPara, String key) {
		String prestr = AlipayCore.createLinkString(sPara); // 把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
		logManager.debug("AlipaySubmit-buildRequestMysign", prestr);
		String mysign = "";
		if (AlipayConfig.sign_type.equals("MD5")) {
			mysign = MD5.sign(prestr, key, AlipayConfig.input_charset);
		}
		return mysign;
	}

	/**
	 * 生成要请求给支付宝的参数数组
	 * 
	 * @param sParaTemp
	 *            请求前的参数数组
	 * @return 要请求的参数数组
	 */
	public static Map<String, String> buildRequestPara(Map<String, String> sParaTemp, String key) {
		// 除去数组中的空值和签名参数
		Map<String, String> sPara = AlipayCore.paraFilter(sParaTemp);
		// 生成签名结果
		String mysign = buildRequestMysign(sPara, key);
		logManager.debug("签名结果", mysign);
		// 签名结果与签名方式加入请求提交参数组中
		sPara.put("sign", mysign);
		sPara.put("sign_type", AlipayConfig.sign_type);

		return sPara;
	}

	// /**
	// * 建立请求，以表单HTML形式构造（默认）
	// * @param sParaTemp 请求参数数组
	// * @param strMethod 提交方式。两个值可选：post、get
	// * @param strButtonName 确认按钮显示文字
	// * @return 提交表单HTML文本
	// */
	// public static String buildRequest(Map<String, String> sParaTemp, String
	// strMethod, String strButtonName) {
	// //待请求参数数组
	// Map<String, String> sPara = buildRequestPara(sParaTemp,key);
	// List<String> keys = new ArrayList<String>(sPara.keySet());
	//
	// StringBuffer sbHtml = new StringBuffer();
	//
	// sbHtml.append("<form id=\"alipaysubmit\" name=\"alipaysubmit\" action=\""
	// + ALIPAY_GATEWAY_NEW
	// + "_input_charset=" + AlipayConfig.input_charset + "\" method=\"" +
	// strMethod
	// + "\">");
	//
	// for (int i = 0; i < keys.size(); i++) {
	// String name = (String) keys.get(i);
	// String value = (String) sPara.get(name);
	//
	// sbHtml.append("<input type=\"hidden\" name=\"" + name + "\" value=\"" +
	// value + "\"/>");
	// }
	//
	// //submit按钮控件请不要含有name属性
	// sbHtml.append("<input type=\"submit\" value=\"" + strButtonName +
	// "\" style=\"display:none;\"></form>");
	// sbHtml.append("<script>document.forms['alipaysubmit'].submit();</script>");
	//
	// return sbHtml.toString();
	// }

	/**
	 * 构建跳转url
	 * 
	 * @param sParaTemp
	 * @return
	 */
	public static String buildForwardUrl(String gateUrl,Map<String, String> sParaTemp, String key) {
		// 待请求参数数组
		Map<String, String> sPara = buildRequestPara(sParaTemp, key);
		logManager.debug("组装请求参数finished", null);
		List<String> keys = new ArrayList<String>(sPara.keySet());
		StringBuffer sbHtml = new StringBuffer(gateUrl + "?_input_charset=" + AlipayConfig.input_charset);
		logManager.debug("组装请求参数sbHtml", null);
		for (int i = 0; i < keys.size(); i++) {
			String name = (String) keys.get(i);
			String value = (String) sPara.get(name);
			try {
				sbHtml.append("&" + name + "=" + URLEncoder.encode(value, "utf-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		logManager.debug("组装请求参数sbHtml...................", sbHtml.toString());
		return sbHtml.toString();
	}

	/**
	 * 构建跳转url
	 * 
	 * @param sParaTemp
	 * @return
	 */
	public static String buildMobileForwardUrl(Map<String, String> sParaTemp, String key) {
		// 待请求参数数组
		sParaTemp = AlipayCore.paraFilter(sParaTemp);
		// 把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
		String prestr = AlipayCore.createLinkString(sParaTemp);
		// 生成签名结果
		String mysign = RSASignature.sign(prestr, key);
		sParaTemp.put("sign", mysign);
		sParaTemp.put("sign_type", AlipayConfig.SIGN_TYPE_RSA);
		List<String> keys = new ArrayList<String>(sParaTemp.keySet());
		StringBuffer sbHtml = new StringBuffer("");
		for (int i = 0; i < keys.size(); i++) {
			String name = (String) keys.get(i);
			String value = (String) sParaTemp.get(name);
			try {
				if (i > 0) {
					sbHtml.append("&" + name + "=" + URLEncoder.encode(value, "utf-8"));
				} else {
					sbHtml.append(name + "=" + URLEncoder.encode(value, "utf-8"));
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return sbHtml.toString();
	}

	/**
	 * 构建跳转url
	 * 
	 * @param sParaTemp
	 * @return
	 */
	public static String buildForwardUrlPhoneMessage(String gateUrl,Map<String, String> sParaTemp, String key) {
		// 待请求参数数组
		// Map<String, String> sPara = buildRequestPara(sParaTemp,key);
		List<String> keys = new ArrayList<String>(sParaTemp.keySet());
		StringBuffer sbHtml = new StringBuffer(gateUrl + "?_input_charset=" + AlipayConfig.input_charset);

		for (int i = 0; i < keys.size(); i++) {
			String name = (String) keys.get(i);
			String value = (String) sParaTemp.get(name);
			try {
				sbHtml.append("&" + name + "=" + URLEncoder.encode(value, "utf-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		return sbHtml.toString();
	}

	/**
	 * 建立请求，以表单HTML形式构造，带文件上传功能
	 * 
	 * @param sParaTemp
	 *            请求参数数组
	 * @param strMethod
	 *            提交方式。两个值可选：post、get
	 * @param strButtonName
	 *            确认按钮显示文字
	 * @param strParaFileName
	 *            文件上传的参数名
	 * @return 提交表单HTML文本
	 */
	public static String buildRequest(String gateUrl,Map<String, String> sParaTemp, String strMethod, String strButtonName, String strParaFileName, String key) {
		// 待请求参数数组
		Map<String, String> sPara = buildRequestPara(sParaTemp, key);
		List<String> keys = new ArrayList<String>(sPara.keySet());

		StringBuffer sbHtml = new StringBuffer();

		sbHtml.append("<form id=\"alipaysubmit\" name=\"alipaysubmit\"  enctype=\"multipart/form-data\" action=\"" + gateUrl
				+ "?_input_charset=" + AlipayConfig.input_charset + "\" method=\"" + strMethod + "\">");

		for (int i = 0; i < keys.size(); i++) {
			String name = (String) keys.get(i);
			String value = (String) sPara.get(name);

			sbHtml.append("<input type=\"hidden\" name=\"" + name + "\" value=\"" + value + "\"/>");
		}

		sbHtml.append("<input type=\"file\" name=\"" + strParaFileName + "\" />");

		// submit按钮控件请不要含有name属性
		sbHtml.append("<input type=\"submit\" value=\"" + strButtonName + "\" style=\"display:none;\"></form>");

		return sbHtml.toString();
	}

	/**
	 * 建立请求，以模拟远程HTTP的POST请求方式构造并获取支付宝的处理结果
	 * 如果接口中没有上传文件参数，那么strParaFileName与strFilePath设置为空值 如：buildRequest("",
	 * "",sParaTemp)
	 * 
	 * @param strParaFileName
	 *            文件类型的参数名
	 * @param strFilePath
	 *            文件路径
	 * @param sParaTemp
	 *            请求参数数组
	 * @return 支付宝处理结果
	 * @throws Exception
	 */
	public static String buildRequest(String gateUrl,String strParaFileName, String strFilePath, Map<String, String> sParaTemp, String key) throws Exception {
		// 待请求参数数组
		Map<String, String> sPara = buildRequestPara(sParaTemp, key);

		HttpProtocolHandler httpProtocolHandler = HttpProtocolHandler.getInstance();

		HttpRequest request = new HttpRequest(HttpResultType.BYTES);
		// 设置编码集
		request.setCharset(AlipayConfig.input_charset);

		request.setParameters(generatNameValuePair(sPara));
		request.setUrl(gateUrl + "?_input_charset=" + AlipayConfig.input_charset);

		HttpResponse response = httpProtocolHandler.execute(request, strParaFileName, strFilePath);
		if (response == null) {
			return null;
		}

		String strResult = response.getStringResult();

		return strResult;
	}

	/**
	 * MAP类型数组转换成NameValuePair类型
	 * 
	 * @param properties
	 *            MAP类型数组
	 * @return NameValuePair类型数组
	 */
	private static NameValuePair[] generatNameValuePair(Map<String, String> properties) {
		NameValuePair[] nameValuePair = new NameValuePair[properties.size()];
		int i = 0;
		for (Map.Entry<String, String> entry : properties.entrySet()) {
			nameValuePair[i++] = new NameValuePair(entry.getKey(), entry.getValue());
		}

		return nameValuePair;
	}

	/**
	 * 用于防钓鱼，调用接口query_timestamp来获取时间戳的处理函数 注意：远程解析XML出错，与服务器是否支持SSL等配置有关
	 * 
	 * @return 时间戳字符串
	 * @throws IOException
	 * @throws DocumentException
	 * @throws MalformedURLException
	 */
	public static String query_timestamp(String gateUrl,String partner) throws MalformedURLException, DocumentException, IOException {

		// 构造访问query_timestamp接口的URL串
		String strUrl = gateUrl + "?service=query_timestamp&partner=" + partner;
		StringBuffer result = new StringBuffer();

		SAXReader reader = new SAXReader();
		Document doc = reader.read(new URL(strUrl).openStream());

		List<Node> nodeList = doc.selectNodes("//alipay/*");

		for (Node node : nodeList) {
			// 截取部分不需要解析的信息
			if (node.getName().equals("is_success") && node.getText().equals("T")) {
				// 判断是否有成功标示
				List<Node> nodeList1 = doc.selectNodes("//response/timestamp/*");
				for (Node node1 : nodeList1) {
					result.append(node1.getText());
				}
			}
		}

		return result.toString();
	}

	/**
	 * 构造模拟远程HTTP的POST请求，获取支付宝的返回XML处理结果
	 * 
	 * @param sParaTemp
	 *            请求参数数组
	 * @param gateway
	 *            网关地址
	 * @return 支付宝返回XML处理结果
	 * @throws Exception
	 */
	public static String sendPostInfo(Map<String, String> sParaTemp, String gateway, String key) throws Exception {
		// 待请求参数数组
		Map<String, String> sPara = buildRequestPara(sParaTemp, key);

		HttpProtocolHandler httpProtocolHandler = HttpProtocolHandler.getInstance();

		HttpRequest request = new HttpRequest(HttpResultType.BYTES);
		// 设置编码集
		request.setCharset(AlipayConfig.input_charset);

		request.setParameters(generatNameValuePair(sPara));
		request.setUrl(gateway + "_input_charset=" + AlipayConfig.input_charset);

		HttpResponse response = httpProtocolHandler.execute(request);
		if (response == null) {
			return null;
		}

		String strResult = response.getStringResult();

		return strResult;
	}
	
	
	
	/**
	 * 构建支付宝wap端跳转url
	 * 
	 * @param sParaTemp
	 * @return
	 */
	public static String buildWapForwardUrl(String gateUrl,Map<String, String> sParaTemp, String key) {
		// 待请求参数数组
		Map<String, String> sPara = buildWapRequestPara(sParaTemp, key);
		logManager.debug("组装请求参数finished", null);
		List<String> keys = new ArrayList<String>(sPara.keySet());
		StringBuffer sbHtml = new StringBuffer(gateUrl + "?_input_charset=" + AlipayConfig.input_charset);
		logManager.debug("组装请求参数sbHtml", "keys.size()="+keys.size());
		for (int i = 0; i < keys.size(); i++) {
			String name = (String) keys.get(i);
			String value = (String) sPara.get(name);
			try {
				sbHtml.append("&" + name + "=" + URLEncoder.encode(value, "utf-8"));
			} catch (UnsupportedEncodingException e) {
				logManager.debug("组装请求参数sbHtml 异常","组装请求参数sbHtml 异常 e.getMessage()="+e.getMessage());
				e.printStackTrace();
			}
		}
		logManager.debug("组装请求参数sbHtml...................", sbHtml.toString());
		return sbHtml.toString();
	}
	
	 /**
     * 生成要请求给支付宝wap的参数数组
     * @param sParaTemp 请求前的参数数组
     * @return 要请求的参数数组
     */
    private static Map<String, String> buildWapRequestPara(Map<String, String> sParaTemp ,String private_key) {
        //除去数组中的空值和签名参数
        Map<String, String> sPara = AlipayCore.paraFilter(sParaTemp);
        //生成签名结果
        String mysign = buildWapRequestMysign(sPara ,private_key);

        //签名结果与签名方式加入请求提交参数组中
        sPara.put("sign", mysign);
        sPara.put("sign_type", AlipayConfig.SIGN_TYPE_RSA);
        logManager.debug("签名sign..................", "签名sign="+mysign);
        return sPara;
    }
	
    /**
     * 生成支付宝wap签名结果
     * @param sPara 要签名的数组
     * @return 签名结果字符串
     */
	public static String buildWapRequestMysign(Map<String, String> sPara,String private_key) {
    	String prestr = AlipayCore.createLinkString(sPara); //把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
    	logManager.debug("需要签名的参数", "需要签名的参数 prestr="+prestr);
    	String mysign = "";
        if(AlipayConfig.SIGN_TYPE_RSA.equals("RSA") ){
        	mysign = RSA.sign(prestr, private_key, AlipayConfig.input_charset);
        }
        return mysign;
    }
	
}
