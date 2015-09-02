package com.pay.filter;

/**
 * User: Chen Yi
 * Date: 13-10-14
 */

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.pay.framework.log.LogManager;


/**
 * @Description: 对返回的数据使用JSONP形式封装
 *
 * @author PCCW
 *
 * @date 2013年9月4日 下午12:05:44
 */
public class JsonpFilter extends OncePerRequestFilter {
    private static final String JSONP_FLAG = "callback";
	LogManager logManager = LogManager.getLogger(JsonpFilter.class);
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String callback = request.getParameter(JSONP_FLAG);
        boolean doJsonp = StringUtils.isNotBlank(callback);
        if(logger.isDebugEnabled()){
            logger.debug("jsonp callback. ................ return..." + doJsonp);
        }
        
        if (doJsonp) {
            OutputStream out = response.getOutputStream();
            GenericResponseWrapper wrapper = new GenericResponseWrapper(response);
            filterChain.doFilter(request, wrapper);
            byte[] bytes=new String(callback + "("+new String(wrapper.getData())+");").getBytes();
            response.setContentLength(bytes.length);
            out.write(bytes);
            wrapper.setContentType("text/javascript;charset=UTF-8");
            out.flush();
            out.close();
        } else {
            filterChain.doFilter(request, response);
        }
    }

}

