package com.pay.framework.util;

import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;


/**
 * 参数通用类，可以判断字符串是否为空，是否为数字，是否为完美通行证等
 * 
 * @author houzhaowei
 * 
 */
public class ParamUtil {

	/**
	 * 判断字符串是否为空
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isNull(String s) {
		if (s == null || s.trim().length() == 0) {
			return true;
		}
		return false;
	}

	/**
	 * 判断是否为正数
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isNum(String s) {

		try {
			if (Integer.parseInt(s) <= 0)
				return false;
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 判断是否为数字
	 * @param str
	 * @return
	 */
	public static boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("[0-9]*");
		return pattern.matcher(str).matches();
	}
	
	public static boolean isDouble(String str) {    
	    Pattern pattern = Pattern.compile("^[-\\+]?[.\\d]*$");    
	    return pattern.matcher(str).matches();    
	  }
	
	/* 
	  * 判断是否为整数  
	  * @param str 传入的字符串  
	  * @return 是整数返回true,否则返回false  
	*/  
	  public static boolean isInteger(String str) {    
	    Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");    
	    return pattern.matcher(str).matches();    
	  }
	  
	  public static Integer getInteger(HttpServletRequest request, String param) {
			
	 		Integer inte = null;
			String str = request.getParameter(param);
			if (!"null".equals(str)&&!"".equals(str)&&str!=null&&str.length() > 0) {
				try{
					inte = new Integer(str.trim());
				}catch(Exception e){
					
				}
			}
			return inte;
	 	}
	  public static void main(String[] args) {
			System.out.println(ParamUtil.isDouble("2.5"));
		}
}
