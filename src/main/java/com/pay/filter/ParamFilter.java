package com.pay.filter;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class ParamFilter implements Filter{

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
  
			HttpServletRequest request = (HttpServletRequest) req;  
	        HttpServletResponse response = (HttpServletResponse) resp;
	          
	        String uri = request.getRequestURI() == null ? "" : request.getRequestURI();  
	        String regEx="/ipay/pay/[0-9]+$";  
	        Pattern pattern = Pattern.compile(regEx);  
	        Matcher matcher = pattern.matcher(uri);  
	        if(!matcher.find()){  
	        	  chain.doFilter(request, response);
		    }else{
		          Map<String, String> params = new HashMap<String, String>();
		    	  Map<String, String[]> requestParams = request.getParameterMap();
		    	  boolean flag = true;
		    	  Document document = null;
		    	  try {
		    			SAXReader reader = new SAXReader();
	    				File f = new File(request.getRealPath("")+"/paramCheck.xml");
	    				document = reader.read(f);
	    				for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
	    					String name = (String) iter.next();
	    					String[] values = (String[]) requestParams.get(name);
	    					String valueStr = "";
	    					for (int i = 0; i < values.length; i++) {
	    						valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
	    					}
	    					int len = valueStr.length();
	    					//此处拦截  超过长度的参数 
	    					if(len>getContentByKey(document,"//param/"+name)){
	    						flag =false;
	    						response.setContentType("text/html");
	    						PrintWriter out;
	    						out = response.getWriter();
	    						out.println("参数"+name+"的值过长！");
	    						out.flush();
	    						out.close();
	    					}
	    				}
				 }catch (DocumentException e) {
						System.out.println("paramCheck.xml 文件读取失败");  
						flag =false;
						PrintWriter out;
						out = response.getWriter();
						out.println("系统异常");
						out.flush();
						out.close();
				}
	    		if(flag){
	    			chain.doFilter(request, response); 
	    		}
		  }  
	}

	@Override
	public void destroy() {

	}
	
	//<?xml version="1.0" encoding="utf-8"?><param><typeid>10</typeid><name>50</name></param>
    //   //param/typeid
	public int getContentByKey(Document document, String level) {
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
