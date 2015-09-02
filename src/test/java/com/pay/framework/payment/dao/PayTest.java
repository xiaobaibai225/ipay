package com.pay.framework.payment.dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import net.sf.json.JSONObject;


import com.pay.framework.payment.BasePay;
import com.pay.framework.payment.alipay.Md5Encrypt;
import com.pay.framework.util.DateUtil;
import com.pay.framework.util.MD5Util;

public class PayTest {

	public static void main(String[] args) throws Exception {
		String companyId="350001";
		String corderid="2015080800001";	
		String price="1000";
		String signedSecKey = MD5Util.MD5Encode(companyId+ BasePay.SECKEY_SUFFIX, "UTF-8");
		// 需要签名的串9c882f601325e4ccb23a47ca04af5ce4
		String validateStr = "corderid=" + corderid + "&userid=17904223&price="+price
			+ "&companyid=" + companyId + "&" + signedSecKey + "&deptid=111";
		//corderid=1986516123423&userid=17904223&price=0.01&companyid=350001&9c882f601325e4ccb23a47ca04af5ce4&deptid=111
		String MD5Value = Md5Encrypt.md5(validateStr); // fa24a26d1316a32cf016df41f9314de0
		System.out.println(MD5Value);
		
		String secKey = companyId + BasePay.SECKEY_SUFFIX;
		String signedSecKey1 = MD5Util.MD5Encode(secKey, "UTF-8");
		String md5Str = "corderid=" + corderid + "&" + signedSecKey1 + "&companyid=" + companyId;
		String md5Value = MD5Util.MD5Encode(md5Str, "UTF-8");
		System.out.println(md5Value); // 9b50b415508dae2b275e06c7089fb844
		
	
		//String td = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new SimpleDateFormat("yyyy-MM-ddHH:mm:ss").parse("2015-08-03"+"12:20:23"));
		//System.out.println(td);
	//	File file = new File("D:/payLogs/线上推送格式文件20150803.txt");
		
	//	InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "GBK");
	//	BufferedReader br = new BufferedReader(isr);
	//	String s = null;
		//s = br.readLine();
	//	while((s = br.readLine())!=null){
	//		if(s.length()<=0) continue;
	//		System.out.println(s);
	//	}
		
		
		//String searchUrl = String.format("%ssearchPayStatus?companyid=%s&available=%s&platform=%s&institution=%s&payMethod=%s", "http://192.168.220.35:8080/ipay/pay/", "350001", "Y", "WEB", "","");
		String searchUrl = String.format("%ssearchPayStatus?companyid=%s&available=%s&platform=%s&institution=%s&payMethod=%s", "http://192.168.220.35:8080/ipay/pay/", "350001", "Y", "WEB", "","");
		try {
			URL url = new URL(searchUrl);
			URLConnection conn = url.openConnection();
			conn.setConnectTimeout(30000);
			conn.setReadTimeout(30000); 
			StringBuffer document = new StringBuffer();
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
			String line = null;
			while ((line = reader.readLine()) != null){
				document.append(line + " ");
			}
			reader.close();
			System.out.println(document.toString());
			JSONObject jsonObject = JSONObject.fromObject(document.toString());
			//List<Map<String,String>> list = (List<Map<String, String>>) jsonObject.get("PayStatusList");
			System.out.println(java.net.URLDecoder.decode(document.toString(), "utf-8") );
		} catch (Exception e) {
			//throw new RestServiceException("N", e.getMessage());
			System.out.println(e.getMessage());
		}
		//String a ="http://soho3q.sohochina.com/ipay/pay/mobile/119?backurl=http%3A%2F%2Fsoho3q.sohochina.com%2Fsoho%2Fentry%2Fweb%2FonlineAction.do%3FactionType%3DserverNotify%26ajax%3Dtrue%26batchId%3D40285ca34f44ae04014f44d0cf2c000e%26batchAmount%3D2000.00%26orderCategory%3DHOUSE_OR_STATION%26currentLang%3Dzh_CN&buyType=HOUSES&chargetype=HOUSE_OR_STATION&companyid=350001&corderid=40285ca34f44ae04014f44d0cf2c000e&defaultbank=alipayWap-B2C&deptid=111&ext=http%3A%2F%2Fsoho3q.sohochina.com%2Fsoho%2Fentry%2Fweb%2FonlineAction.do%3FactionType%3DrefundNotify%26ajax%3Dtrue&fronturl=http%3A%2F%2Fsoho3q.sohochina.com%2Fhtml5%2Fz_success.jsp%3Fbill_id%3D40285ca34f44ae04014f44d0cf2c000e&money=2000.00&productdesc=%E6%9C%9B%E4%BA%ACSOHO.%E7%AC%AC3%E5%B1%82.%E5%8A%9E%E5%85%AC%E6%A1%8C3-D18&productid=402853a94dae7011014dae7160a90080&productname=%E6%9C%9B%E4%BA%ACSOHO.%E7%AC%AC3%E5%B1%82.%E5%8A%9E%E5%85%AC%E6%A1%8C3-D18&productnum=1&producttype=OPEN_STATION&sign=8125d94358d87534703c067ede10e048&userid=40285caa4e1e4334014e1f2a36b80018&username=&internationalCardType=";
		//String b = new String(a.getBytes("ISO-8859-1"),"utf-8");
		//b= java.net.URLEncoder.encode(a,"utf-8");
		//System.out.println(b);
		//System.out.println(java.net.URLDecoder.decode(a, "utf-8"));
		
//		Document document = null;
//		try {
//			SAXReader reader = new SAXReader();
//			File f = new File("D:/workplace/ipay/paramCheck.xml");
//			document = reader.read(f);
//		} catch (DocumentException e) {
//			System.out.println("paramCheck.xml 文件读取失败");  
//		}
//	   int len =	getContentByKey(document,"//param/name");
//	   System.out.println(len);
//		
		/*String str = "http://soho3q.sohochina.com/ipay/pay/fatepay/123";
		String regEx="/ipay/pay/[0-9]+$";  
        Pattern pattern = Pattern.compile(regEx);  
        Matcher matcher = pattern.matcher(str);  
        if(!matcher.find()){  
            System.out.println("error");  
        }else{
        	System.out.println("ok");
        }  */
		String rowString= "1  2  3  44  55 66    77  8 9     10   ";
		rowString = rowString.replaceAll(" {2,}", " ");
		System.out.println(rowString);
		System.out.println(new BigDecimal("  13,111.00  ".replaceAll(",", "").trim()));
		Timestamp transactionDate = null;//交易日期
		DateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		Date tDate = format.parse("2015"+"0317111032");
		DateFormat format2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		transactionDate = Timestamp.valueOf(format2.format(tDate));
		System.err.println(transactionDate);
	}

	public static int getContentByKey(Document document, String level) {
		List<String> valueList = new ArrayList<String>();
		List list = document.selectNodes(level);
		if(list!=null&&list.size()>0){
			Iterator iter = list.iterator();
			while (iter.hasNext()) {
				Element element = (Element) iter.next();
				String value = element.getText();
				valueList.add(value);
			}
		}
		if(valueList==null||valueList.size()<=0){
			return Integer.MAX_VALUE;
		}else{
			return Integer.parseInt(valueList.get(0));
		}
	}
	
}
