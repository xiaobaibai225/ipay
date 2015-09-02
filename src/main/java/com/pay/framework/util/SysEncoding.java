package com.pay.framework.util;

/**
 * 系统编码配置
 * @author zhangyouxing
 *
 */
public class SysEncoding {
    private String request;
    private String response;
    private String page;
    
    public SysEncoding(String request, String response, String page) {
	super();
	this.request = request;
	this.response = response;
	this.page = page;
    }
    public String getPage() {
        return page;
    }
    public void setPage(String page) {
        this.page = page;
    }
    public String getRequest() {
        return request;
    }
    public void setRequest(String request) {
        this.request = request;
    }
    public String getResponse() {
        return response;
    }
    public void setResponse(String response) {
        this.response = response;
    }
    
}
