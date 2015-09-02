package com.pay.framework.payment.bocpay.out.util;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public class SHA256Util {
	//static String SECURE_SECRET = "61hvr629khsgwsjc8p3skec01vvty35c";

    static final char[] HEX_TABLE = new char[] {
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    static String hashKeys = new String();
    static String hashValues = new String();
    
	/*public String getSHA256(HttpServletRequest request){
		Map fields = new HashMap();
	    Enumeration e = request.getParameterNames();

	    while (e.hasMoreElements()) {
	        String fieldName = (String) e.nextElement();
	        String fieldValue = request.getParameter(fieldName);
	        if ((fieldValue != null) && (fieldValue.length() > 0)) {
	            fields.put(fieldName, fieldValue);
	        }
	    }

	    
	    String vpcURL = (String) fields.get("GatewayClientURL");
	    fields.remove("GatewayClientURL");
	    fields.remove("SubButL");

	    
	    fields.put("submit", "Continue");

	    
	    if (SECURE_SECRET != null && SECURE_SECRET.length() > 0) {
	        String secureHash = hashAllFields(fields);
	        fields.put("HASH", secureHash);
	    }
		return null;
	}*/
	
   public static String hashAllFields(Map fields, String SECURE_SECRET) {

	    hashKeys = "";
	    hashValues = "";
	    
	    List fieldNames = new ArrayList(fields.keySet());
	    Collections.sort(fieldNames);
	
	    StringBuffer buf = new StringBuffer();
	    buf.append(SECURE_SECRET);
	
	    Iterator itr = fieldNames.iterator();
	
	    while (itr.hasNext()) {
	        String fieldName = (String) itr.next();
	        String fieldValue = (String) fields.get(fieldName);
	            hashKeys += fieldName + ", ";
	        if ((fieldValue != null) && (fieldValue.length() > 0)) {
	            buf.append(fieldValue);
	        }
	    }
	
	    MessageDigest sha256 = null;
	    byte[] ba = null;
	
	    try {
	        sha256 = MessageDigest.getInstance("SHA-256");
	        ba = sha256.digest(buf.toString().getBytes("UTF-8"));
	    } catch (Exception e) {} 
	
	    hashValues = buf.toString();
	    return hex(ba);
	
	} 
    static String hex(byte[] input) {
        
        StringBuffer sb = new StringBuffer(input.length * 2);

        
        for (int i = 0; i < input.length; i++) {
            sb.append(HEX_TABLE[(input[i] >> 4) & 0xf]);
            sb.append(HEX_TABLE[input[i] & 0xf]);
        }
        return sb.toString();
    }
}
