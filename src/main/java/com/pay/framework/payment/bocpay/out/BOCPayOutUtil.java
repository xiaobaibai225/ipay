package com.pay.framework.payment.bocpay.out;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class BOCPayOutUtil {
	public static Map<String,String> getRequestParameter(HttpServletRequest request){
		Map map = new HashMap();
		Enumeration e = request.getParameterNames();
		if(e!=null){
			while(e.hasMoreElements()){
				String key = (String) e.nextElement();
				String value = request.getParameter(key);
				map.put(key, value);
				if(map.get(key)==null||map.get(key)==""){
					map.remove(key);
				}
			}
		}
		return map;
	}
	
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
	 * 把 String 转化成 map
	 * @param String
	 * @return
	 */
	public static Map readStringOut(String s) {
		Map map = new HashMap();
		String[] str = s.split("&");
		String[] keyAndValue = null;
		for(int i=0;i<str.length;i++){
			keyAndValue = str[i].split("=");
			if(!(keyAndValue.length<2)){
				map.put(keyAndValue[0], keyAndValue[1]);
			}
		}
		return map;
	}
}
