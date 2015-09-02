package com.pay.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class EncodeFilterAbs implements Filter{

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		// TODO Auto-generated method stub
		HttpServletRequest request = (HttpServletRequest) req;  
        HttpServletResponse response = (HttpServletResponse) resp;  
          
        //判断如果是index.jspx 放行  
        String uri = request.getRequestURI() == null ? "" : request.getRequestURI();  
        //uri = uri.substring(uri.lastIndexOf("/")+1);  
        System.out.println("当前请求的路径: " + uri);  
        // 工行请求编码
        if(uri.endsWith("pay/servernotify/32") || uri.endsWith("pay/servernotify/31")) { 
        	System.out.println("编码");
        	String striso = request.getParameter("PayeeName");// 编码付款人
        	String strGbk = new String(striso.getBytes("iso-8859-1"),"GBK");
        	String strEncode = java.net.URLEncoder.encode(strGbk, "UTF-8");
        	System.out.println(strEncode);
        	request.setAttribute("PayeeNameAttr", strEncode);
        	System.out.println(java.net.URLDecoder.decode(strEncode, "UTF-8"));
        	//request.getParameterMap().put("PayeeName", strEncode);
        	chain.doFilter(request, response);  
        } else {    //下面是判断是否有session，也就是用户是否已登录状态；                                                                                                                                          
            
        	//所有人都能请求到的URI，放行  
        	chain.doFilter(request, response);  
        }     
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub
		
	}

}
