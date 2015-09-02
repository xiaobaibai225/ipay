package com.pay.framework.payment.icbcpay;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ComUtils {
	
	public static String getCurrentDate2(String formatStr) {
		SimpleDateFormat sdf = new SimpleDateFormat(formatStr);
		String date = sdf.format(new Date());
		return date;
	}
	/**
	 * 
	 * @Title: removePathLastStr 
	 * @Description: 去除路径最后的斜杠 
	 * @param @param path
	 * @param @return    设定文件 
	 * @return String    返回类型 
	 * @throws
	 */
	public static String removePathLastStr(String path){
		
		int len = path.length();
		if (path != null) {
			if (len == 1) {
				path = (path.equals("/") || path.equals("\\"))?"":path;
			}else{
				int a = path.lastIndexOf("/"); // 字符串中最后一个“/”的索引位置
				int b = path.lastIndexOf("\\"); // 字符串中最后一个“\\”的索引位置
				
				if ((len == a+1) || (len == b+1)) {
					// 去除路径的最后斜杠
					path = path.substring(0, len-1);
				}
			}
		}
		
		return path;
	}
	
	/**
	  * 方法描述：去掉XML的格式化空格
	  * <br>如果><之间只有空白字符(空白字符包括新行、tab和空格)去掉
	  * <br>将字段内容的两头空白字符去掉
	  * @param xml String
	  * @return String
	  * @version: 1.0
	  * @author: www.sinoufc.com
	  * @time: 2013-12-17 下午4:11:31
	  */
	public static String removeXMLWhiteSpace(String xml) {
		int xmlLength = xml.length();
		StringBuffer xmlSB = new StringBuffer(xmlLength);
		char c = 0;
		//符号标志
		boolean flag = false;
		StringBuffer temp = null;
		for (int i = 0; i < xmlLength; i++) {
			c = xml.charAt(i);
			if (c == '<' && temp != null) {
				flag = false;
				String s = temp.toString();
				if (s.trim().length() > 0) {
					xmlSB.append(s.trim());
				}
			}
			if (flag) {
				temp.append(c);
			} else {
				xmlSB.append(c);
			}
			if (c == '>') {
				flag = true;
				temp = new StringBuffer();
			}
		}
		xml = xmlSB.toString();
		return xml;
	}

}
