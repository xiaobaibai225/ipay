package com.pay.framework.payment.unionpay.moto.util;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class PropertiesUtil {
	
    private static PropertiesUtil instance = new PropertiesUtil();  
    
    private PropertiesUtil (){}
    
    public static PropertiesUtil getInstance() {  
    	return instance;  
    }  
	
	
	public String  getProValue(String code)
	{
		String value="";
		Properties properties = new Properties();  
		InputStream inputStream = this.getClass().getResourceAsStream("motocode.properties");  
		BufferedReader bf = new BufferedReader(new    InputStreamReader(inputStream));  
		try {
			properties.load(bf);
			value=properties.getProperty(code); 
		} catch (IOException e) {
			e.printStackTrace();
		}  
		return value;
	}

}
