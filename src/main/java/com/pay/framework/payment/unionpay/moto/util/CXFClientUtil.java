package com.pay.framework.payment.unionpay.moto.util;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

public class CXFClientUtil {  
      
    public static final int CXF_CLIENT_CONNECT_TIMEOUT = 10 * 1000;  
    public static final int CXF_CLIENT_RECEIVE_TIMEOUT = 90 * 1000;  

    public static void configTimeout(Object service) {  
        Client proxy = ClientProxy.getClient(service);  
        HTTPConduit conduit = (HTTPConduit) proxy.getConduit();  
        HTTPClientPolicy policy = new HTTPClientPolicy();  
        policy.setConnectionTimeout(CXF_CLIENT_CONNECT_TIMEOUT);  
        policy.setReceiveTimeout(CXF_CLIENT_RECEIVE_TIMEOUT);  
        conduit.setClient(policy);  
    }  
      
}  