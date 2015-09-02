package com.pay.framework.payment.icbcpay;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.lang.RandomStringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.pay.framework.log.LogManager;
import com.pay.framework.util.XmlUtil;

public class ICBCPayUtil {
	public static LogManager logger = LogManager.getLogger(ICBCPayUtil.class);
	public static final int BUFFER = 2048; 
	/**
	 * 把 xml 转化成 map
	 * @param xml
	 * @return
	 */
	public static Map readStringXmlOut(String xml) {
		Map map = new HashMap();
		Document doc = null;
		try {
			doc = DocumentHelper.parseText(xml); // 将字符串转为XML
			Element rootElt = doc.getRootElement(); // 获取根节点
			 for (Iterator ie = rootElt.elementIterator(); ie.hasNext();) {  
		            Element element = (Element) ie.next(); 
		            map.put(element.getName(), element.getData());
		            readElements(map,element);
			 }
		} catch (DocumentException e) {
			e.printStackTrace();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}
	public static Map readElements(Map map, Element element ){
		 for (Iterator ieson = element.elementIterator(); ieson.hasNext();) {  
	            Element elementSon = (Element) ieson.next();  
	            map.put(elementSon.getName(),  elementSon.getText());
	            if( elementSon.elementIterator().hasNext()){
	            	readElements(map,elementSon);
	            }
         }
		 return map;
	}
	/**
	 * request 转化成 map
	 * @param request
	 * @return
	 */
	public static Map<String, String> getAllRequestParam(HttpServletRequest request)
	  {
	    Map res = new HashMap();
	    Enumeration temp = request.getParameterNames();
	    if (null != temp) {
	      while (temp.hasMoreElements()) {
	        String en = (String)temp.nextElement();
	        String value = request.getParameter(en);
	        res.put(en, value);

	        if ((res.get(en) == null) || (res.get(en) == ""))
	        {
	          res.remove(en);
	        }
	      }
	    }
	    return res;
	  }
	
	/**
	 * 获取退款提交指令的xml
	 * @param paramsMap
	 * @return
	 */
	public static String getBackXmlStrByParams(Map paramsMap){
		StringBuffer sbf = new StringBuffer("");
		sbf.append("<?xml version=\"1.0\" encoding = \"GBK\"?>");
		sbf.append("<CMS>");
		sbf.append("<eb>");
		sbf.append("<pub>");
		sbf.append("<TransCode>"+paramsMap.get("TransCode")+"</TransCode>");
		sbf.append("<CIS>"+paramsMap.get("CIS")+"</CIS>");
		sbf.append("<BankCode>"+paramsMap.get("BankCode")+"</BankCode>");
		sbf.append("<ID>"+paramsMap.get("ID")+"</ID>");
		sbf.append("<TranDate>"+paramsMap.get("TranDate")+"</TranDate>");
		sbf.append("<TranTime>"+paramsMap.get("TranTime")+"</TranTime>");
		sbf.append("<fSeqno>"+paramsMap.get("fSeqno")+"</fSeqno>");
		sbf.append("</pub>");
		sbf.append("<in>");
		sbf.append("<TranType>"+paramsMap.get("TranType")+"</TranType>");
		sbf.append("<ShopType>"+paramsMap.get("ShopType")+"</ShopType>");
		sbf.append("<ShopCode>"+paramsMap.get("ShopCode")+"</ShopCode>");
		sbf.append("<ShopAcct>"+paramsMap.get("ShopAcct")+"</ShopAcct>");
		sbf.append("<OrderNum>"+paramsMap.get("OrderNum")+"</OrderNum>");
		sbf.append("<PayType>"+paramsMap.get("PayType")+"</PayType>");
		sbf.append("<PayDate>"+paramsMap.get("PayDate")+"</PayDate>");
		sbf.append("<TransferName>"+paramsMap.get("TransferName")+"</TransferName>");
		sbf.append("<TransferAccNo>"+paramsMap.get("TransferAccNo")+"</TransferAccNo>");
		sbf.append("<PayAmt>"+paramsMap.get("PayAmt")+"</PayAmt>");
		sbf.append("<SignTime>"+paramsMap.get("SignTime")+"</SignTime>");
		sbf.append("<ReqReserved1>"+paramsMap.get("ReqReserved1")+"</ReqReserved1>");
		sbf.append("<ReqReserved2>"+paramsMap.get("ReqReserved2")+"</ReqReserved2>");
		sbf.append("<AcctSeq>"+paramsMap.get("AcctSeq")+"</AcctSeq>");
		sbf.append("</in></eb></CMS>");
		logger.debug("info--退款提交 xml===", sbf.toString());
		return sbf.toString();
	}
	
	/**
	 * 获取退款查询的xml
	 * @param paramsMap
	 * @return
	 */
	public static String getBackB2CEFEINFXmlStrByParams(Map paramsMap){
		StringBuffer sbf = new StringBuffer("");
		sbf.append("<?xml version=\"1.0\" encoding = \"GBK\"?>");
		sbf.append("<CMS>");
		sbf.append("<eb>");
		sbf.append("<pub>");
		sbf.append("<TransCode>"+paramsMap.get("TransCode")+"</TransCode>");
		sbf.append("<CIS>"+paramsMap.get("CIS")+"</CIS>");
		sbf.append("<BankCode>"+paramsMap.get("BankCode")+"</BankCode>");
		sbf.append("<ID>"+paramsMap.get("ID")+"</ID>");
		sbf.append("<TranDate>"+paramsMap.get("TranDate")+"</TranDate>");
		sbf.append("<TranTime>"+paramsMap.get("TranTime")+"</TranTime>");
		sbf.append("<fSeqno>"+paramsMap.get("fSeqno")+"</fSeqno>");
		sbf.append("</pub>");
		sbf.append("<in>");
		sbf.append("<Ordertype>"+paramsMap.get("Ordertype")+"</Ordertype>");
		sbf.append("<ShopType>"+paramsMap.get("ShopType")+"</ShopType>");
		sbf.append("<ShopCode>"+paramsMap.get("ShopCode")+"</ShopCode>");
		sbf.append("<ShopAcct>"+paramsMap.get("ShopAcct")+"</ShopAcct>");
		sbf.append("<QrySerialNo>"+paramsMap.get("QrySerialNo")+"</QrySerialNo>");
		sbf.append("<QryOrderNum>"+paramsMap.get("QryOrderNum")+"</QryOrderNum>");
		sbf.append("<BeginDate>"+paramsMap.get("BeginDate")+"</BeginDate>");
		sbf.append("<EndDate>"+paramsMap.get("EndDate")+"</EndDate>");
		sbf.append("<BeginTime>"+paramsMap.get("BeginTime")+"</BeginTime>");
		sbf.append("<EndTime>"+paramsMap.get("EndTime")+"</EndTime>");
		sbf.append("<ResultType>"+paramsMap.get("ResultType")+"</ResultType>");
		sbf.append("<PTOrderNo>"+paramsMap.get("PTOrderNo")+"</PTOrderNo>");
		sbf.append("<NextTag>"+paramsMap.get("NextTag")+"</NextTag>");
		sbf.append("<IfSeqno>"+paramsMap.get("IfSeqno")+"</IfSeqno>");
		sbf.append("<AcctSeq>"+paramsMap.get("AcctSeq")+"</AcctSeq>");
		sbf.append("</in></eb></CMS>");
		logger.debug("info--退款查询 xml===", sbf.toString());
		return sbf.toString();
	}
	/**
	 * 获取退款查询的xml
	 * @param paramsMap
	 * @return
	 */
	public static String getBackB2CEFEINFXmlStrByParamsB2B(Map paramsMap){
		StringBuffer sbf = new StringBuffer("");
		sbf.append("<?xml version=\"1.0\" encoding = \"GBK\"?>");
		sbf.append("<CMS>");
		sbf.append("<eb>");
		sbf.append("<pub>");
		sbf.append("<TransCode>"+paramsMap.get("TransCode")+"</TransCode>");
		sbf.append("<CIS>"+paramsMap.get("CIS")+"</CIS>");
		sbf.append("<BankCode>"+paramsMap.get("BankCode")+"</BankCode>");
		sbf.append("<ID>"+paramsMap.get("ID")+"</ID>");
		sbf.append("<TranDate>"+paramsMap.get("TranDate")+"</TranDate>");
		sbf.append("<TranTime>"+paramsMap.get("TranTime")+"</TranTime>");
		sbf.append("<fSeqno>"+paramsMap.get("fSeqno")+"</fSeqno>");
		sbf.append("</pub>");
		sbf.append("<in>");
		sbf.append("<Ordertype>"+paramsMap.get("Ordertype")+"</Ordertype>");
		sbf.append("<ShopType>"+paramsMap.get("ShopType")+"</ShopType>");
		sbf.append("<ShopCode>"+paramsMap.get("ShopCode")+"</ShopCode>");
		sbf.append("<ShopAcct>"+paramsMap.get("ShopAcct")+"</ShopAcct>");
		sbf.append("<QrySerialNo>"+paramsMap.get("QrySerialNo")+"</QrySerialNo>");
		sbf.append("<QryOrderNum>"+paramsMap.get("QryOrderNum")+"</QryOrderNum>");
		sbf.append("<BeginDate>"+paramsMap.get("BeginDate")+"</BeginDate>");
		sbf.append("<EndDate>"+paramsMap.get("EndDate")+"</EndDate>");
		sbf.append("<BeginTime>"+paramsMap.get("BeginTime")+"</BeginTime>");
		sbf.append("<EndTime>"+paramsMap.get("EndTime")+"</EndTime>");
		sbf.append("<ResultType>"+paramsMap.get("ResultType")+"</ResultType>");
		sbf.append("<NextTag>"+paramsMap.get("NextTag")+"</NextTag>");
		sbf.append("<ReqReserved1>"+paramsMap.get("ReqReserved1")+"</ReqReserved1>");
		sbf.append("<ReqReserved2>"+paramsMap.get("ReqReserved2")+"</ReqReserved2>");
		sbf.append("</in></eb></CMS>");
		logger.debug("info--退款查询 xml===", sbf.toString());
		return sbf.toString();
	}
	/**
	 * 
	 * @param hostname
	 * @param port
	 * @param xmlStr
	 * @param flag
	 * @return
	 * 签名
	 */
	public static String socketICBCEBSS(String hostname, int port,String xmlStr,boolean flag){
		String xmlstrB = StrToBinstr(xmlStr);
		
		StringBuilder result= new StringBuilder();
		try {
			Socket socket = new Socket(hostname,port);
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			StringBuffer sb = new StringBuffer();
			//请求的连接地址
			sb.append("POST /ServletBindUrl HTTP/1.1\r\n").append("Host:"+ hostname + "\r\n")
			.append("Content-Type:INFOSEC_SIGN/1.0\r\n")
			.append("Content-Length:"+xmlStr.getBytes().length+"\r\n")//11 这个数值是看底下内容的长度的 即多少个字符
			.append("\r\n")
			.append(xmlStr+"\r\n"); //内容
			
			out.write(sb.toString());//xmlstrB
			out.flush();
			//打印响应
			String line = "";
			while((line = in.readLine()) != null){
				result.append(line);
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result.toString();
	}
	
	//将字符串转换成二进制字符串，以空格相隔
	private static String StrToBinstr(String str) {
		char[] strChar=str.toCharArray();
		String result="";
		for(int i=0;i<strChar.length;i++){
			result +=Integer.toBinaryString(strChar[i])+ "";
		}
		return result;
	}
	
	/**
	 * 获取查询（支付或退款流水）的xml
	 * 为了获取兑账文件名
	 * @param paramsMap
	 * @return
	 */
	public static String getQueryXmlStrByParams(Map paramsMap){
		StringBuffer sbf = new StringBuffer("");
		sbf.append("<?xml version=\"1.0\" encoding = \"GBK\"?>");
		sbf.append("<CMS>");
		sbf.append("<eb>");
		sbf.append("<pub>");
		sbf.append("<TransCode>"+paramsMap.get("TransCode")+"</TransCode>");
		sbf.append("<CIS>"+paramsMap.get("CIS")+"</CIS>");
		sbf.append("<BankCode>"+paramsMap.get("BankCode")+"</BankCode>");
		sbf.append("<ID>"+paramsMap.get("ID")+"</ID>");
		sbf.append("<TranDate>"+paramsMap.get("TranDate")+"</TranDate>");
		sbf.append("<TranTime>"+paramsMap.get("TranTime")+"</TranTime>");
		sbf.append("<fSeqno>"+paramsMap.get("fSeqno")+"</fSeqno>");
		sbf.append("</pub>");
		sbf.append("<in>");
		sbf.append("<QryFlag>"+paramsMap.get("QryFlag")+"</QryFlag>");
		sbf.append("<ShopType>"+paramsMap.get("ShopType")+"</ShopType>");
		sbf.append("<ShopCode>"+paramsMap.get("ShopCode")+"</ShopCode>");
		sbf.append("<ShopAcct>"+paramsMap.get("ShopAcct")+"</ShopAcct>");
		sbf.append("<QrySerialNo>"+paramsMap.get("QrySerialNo")+"</QrySerialNo>");
		sbf.append("<QryOrderNum>"+paramsMap.get("QryOrderNum")+"</QryOrderNum>");
		sbf.append("<BeginDate>"+paramsMap.get("BeginDate")+"</BeginDate>");
		sbf.append("<EndDate>"+paramsMap.get("EndDate")+"</EndDate>");
		sbf.append("<BeginTime>"+paramsMap.get("BeginTime")+"</BeginTime>");
		sbf.append("<EndTime>"+paramsMap.get("EndTime")+"</EndTime>");
		sbf.append("<ResultType>"+paramsMap.get("ResultType")+"</ResultType>");
		sbf.append("<NextTag>"+paramsMap.get("NextTag")+"</NextTag>");
		sbf.append("<ErpOrder>"+paramsMap.get("ErpOrder")+"</ErpOrder>");
		sbf.append("<QueryType>"+paramsMap.get("QueryType")+"</QueryType>");
		sbf.append("<AcctSeq>"+paramsMap.get("AcctSeq")+"</AcctSeq>");
		sbf.append("</in>");
		sbf.append("</eb>");
		sbf.append("</CMS>");
		
		logger.debug("info--订购兑账 xml===", sbf.toString());
		return sbf.toString();
	}
	
	/**
	 * 获取查询（支付或退款流水）的xml
	 * 为了获取兑账文件名
	 * @param paramsMap
	 * @return
	 */
	public static String getQueryXmlStrByParamsB2B(Map paramsMap){
		StringBuffer sbf = new StringBuffer("");
		sbf.append("<?xml version=\"1.0\" encoding = \"GBK\"?>");
		sbf.append("<CMS>");
		sbf.append("<eb>");
		sbf.append("<pub>");
		sbf.append("<TransCode>"+paramsMap.get("TransCode")+"</TransCode>");
		sbf.append("<CIS>"+paramsMap.get("CIS")+"</CIS>");
		sbf.append("<BankCode>"+paramsMap.get("BankCode")+"</BankCode>");
		sbf.append("<ID>"+paramsMap.get("ID")+"</ID>");
		sbf.append("<TranDate>"+paramsMap.get("TranDate")+"</TranDate>");
		sbf.append("<TranTime>"+paramsMap.get("TranTime")+"</TranTime>");
		sbf.append("<fSeqno>"+paramsMap.get("fSeqno")+"</fSeqno>");
		sbf.append("</pub>");
		sbf.append("<in>");
		sbf.append("<ShopType>"+paramsMap.get("ShopType")+"</ShopType>");
		sbf.append("<ShopCode>"+paramsMap.get("ShopCode")+"</ShopCode>");
		sbf.append("<ShopAcct>"+paramsMap.get("ShopAcct")+"</ShopAcct>");
		sbf.append("<QrySerialNo>"+paramsMap.get("QrySerialNo")+"</QrySerialNo>");
		sbf.append("<QryOrderNum>"+paramsMap.get("QryOrderNum")+"</QryOrderNum>");
		sbf.append("<BeginDate>"+paramsMap.get("BeginDate")+"</BeginDate>");
		sbf.append("<EndDate>"+paramsMap.get("EndDate")+"</EndDate>");
		sbf.append("<BeginTime>"+paramsMap.get("BeginTime")+"</BeginTime>");
		sbf.append("<EndTime>"+paramsMap.get("EndTime")+"</EndTime>");
		sbf.append("<ResultType>"+paramsMap.get("ResultType")+"</ResultType>");
		sbf.append("<NextTag>"+paramsMap.get("NextTag")+"</NextTag>");
		sbf.append("<ReqReserved1>"+paramsMap.get("ReqReserved1")+"</ReqReserved1>");
		sbf.append("<ReqReserved2>"+paramsMap.get("ReqReserved2")+"</ReqReserved2>");
		sbf.append("</in>");
		sbf.append("</eb>");
		sbf.append("</CMS>");
		
		logger.debug("info--订购兑账 xml===", sbf.toString());
		return sbf.toString();
	}
	
	/**
	 * 签名请求
	 * @param seqno
	 * @param xmlContent
	 * @param url
	 * @param charset
	 * @param orderType
	 * @param custMap
	 * @param entityMap
	 * @param bankFlag
	 * @return
	 * @throws HttpUFCException
	 */
	public static String httpPostRequest102(String seqno, String xmlContent, String url, 
			String charset, String orderType
			, String bankFlag,Map<String,String> custMap) {
		String resContent = null;
		try {
			// -------------------------------------------------------------------------------------------------------------------------------------------
			MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager(); // 多线程
			// 多线程
			HttpClient h = new HttpClient(connectionManager);
			// 测试是否超时
			HttpConnectionManagerParams managerParams = h.getHttpConnectionManager().getParams();
			// 设置连接超时时间(单位毫秒)
			managerParams.setConnectionTimeout(70000);
			// 设置读数据超时时间(单位毫秒)
			managerParams.setSoTimeout(70000);
			PostMethod postMethod = new PostMethod(url);
			postMethod.getParams().setContentCharset(charset);
			if("sign".equals(orderType)){// 工行签名
				//工行特制-解签
				postMethod.setRequestHeader("Content-Type", "INFOSEC_SIGN/1.0");
				postMethod.setRequestContentLength(xmlContent.length());
				//设置报文参数
				postMethod.setRequestBody(xmlContent);
			}else{
				// 退款 
				postMethod.addParameters(new NameValuePair[]{
						new NameValuePair("Version", (String) custMap.get("Version")),
						new NameValuePair("TransCode", (String) custMap.get("TransCode")),
						new NameValuePair("BankCode", (String) custMap.get("BankCode")),
						new NameValuePair("GroupCIS", (String) custMap.get("GroupCIS")),
						new NameValuePair("ID", (String) custMap.get("ID")),
						new NameValuePair("PackageID", (String) custMap.get("PackageID")),
						new NameValuePair("Cert", (String) custMap.get("Cert")),
						new NameValuePair("reqData", (String) custMap.get("reqData"))
				} );
				
			}
			Long startTime = 0l;// 定义连接数据开始时间
			Long endTime = 0l;// 定义连接数据结束时间
			startTime = System.currentTimeMillis();// 创建开始时间
			int status = h.executeMethod(postMethod);
			endTime = System.currentTimeMillis();// 创建结束时间
			logger.debug("工行银企互联退款",seqno + "Http请求响应状态码:" + status + "  请求用时:" + (endTime - startTime) + "ms");
			if (status == HttpStatus.SC_OK) {
				resContent = readResContent(seqno, postMethod.getResponseBodyAsStream(), charset, bankFlag, orderType);
				logger.debug("工行银企互联退款",seqno + "请求" + url + "完成! " + "响应结果报文:" + resContent);
			}
		} catch (HttpException e) {
			logger.debug("工行银企互联退款",seqno + "通过Http请求前置机Http出错:" + e.getMessage());
		} catch (IOException e) {
			logger.debug("工行银企互联退款",seqno + "通过Http请求前置机IO错误:" + e.getMessage());
		}
		return resContent;
	}
	
	/**
	  * 方法描述：从InputStream读取响应报文内容
	  * @version: 1.0
	  * @author: www.sinoufc.com
	  * @time: 2014-2-10 下午4:54:34
	  */
	private static String readResContent(String seqno, InputStream in, String charset, String bankFlag, String orderType) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(in, charset));
		String line = "";
		StringBuffer buffer = new StringBuffer();
		while ((line = br.readLine()) != null) {
			buffer.append(line);
		}
		String resContent = buffer.toString();

		//响应报文写入文件中
		IOUtils.writeLogXml(seqno, resContent, bankFlag, orderType, "res");

		//去掉报文中的空白与换行符
		resContent = ComUtils.removeXMLWhiteSpace(resContent);

		return resContent;
	}
	
	/*
	 * 讲xml多个标签转化成list
	 */
	public static Map<String, Object> Dom2Map(Document doc){ 
        Map<String, Object> map = new HashMap<String, Object>(); 
        if(doc == null) 
            return map; 
        Element root = doc.getRootElement(); 
        for (Iterator iterator = root.elementIterator(); iterator.hasNext();) { 
            Element e = (Element) iterator.next(); 
            List list = e.elements(); 
            if(list.size() > 0){ 
                map.put(e.getName(), Dom2Map(e)); 
            }else 
                map.put(e.getName(), e.getText()); 
        } 
        return map; 
    } 
 
    public static Map Dom2Map(Element e){ 
        Map map = new HashMap(); 
        List list = e.elements(); 
        if(list.size() > 0){ 
            for (int i = 0;i < list.size(); i++) { 
                Element iter = (Element) list.get(i); 
                List mapList = new ArrayList(); 
                 
                if(iter.elements().size() > 0){ 
                    Map m = Dom2Map(iter); 
                    if(map.get(iter.getName()) != null){ 
                        Object obj = map.get(iter.getName()); 
                        if(!obj.getClass().getName().equals("java.util.ArrayList")){ 
                            mapList = new ArrayList(); 
                            mapList.add(obj); 
                            mapList.add(m); 
                        } 
                        if(obj.getClass().getName().equals("java.util.ArrayList")){ 
                            mapList = (List) obj; 
                            mapList.add(m); 
                        } 
                        map.put(iter.getName(), mapList); 
                    }else 
                        map.put(iter.getName(), m); 
                } 
                else{ 
                    if(map.get(iter.getName()) != null){ 
                        Object obj = map.get(iter.getName()); 
                        if(!obj.getClass().getName().equals("java.util.ArrayList")){ 
                            mapList = new ArrayList(); 
                            mapList.add(obj); 
                            mapList.add(iter.getText()); 
                        } 
                        if(obj.getClass().getName().equals("java.util.ArrayList")){ 
                            mapList = (List) obj; 
                            mapList.add(iter.getText()); 
                        } 
                        map.put(iter.getName(), mapList); 
                    }else 
                        map.put(iter.getName(), iter.getText()); 
                } 
            } 
        }else 
            map.put(e.getName(), e.getText()); 
        return map; 
    } 
	
    /**
     * 获得指令序列id，规则 yyyyMMdd+7位随机数
     * @return
     */
    public static String getFSeqno(){
    	String str = "";
    	Date date = new Date();
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    	str = sdf.format(date);
    	str+=RandomStringUtils.randomNumeric(7);
    	return str;
    }
}
