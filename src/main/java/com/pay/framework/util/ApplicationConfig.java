package com.pay.framework.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 * 用以获取application.properties 配置的内容
 * @author houzhaowei
 *
 */
public class ApplicationConfig {

	private static final String applicationProperties = "WEB-INF/classes/application.properties";
	
	private static Properties p;
	
	
	static {
		try {
			String path = System.getProperty("ipay.root");
			System.out.println("application path = "+path+applicationProperties);
			InputStream in = new BufferedInputStream(new FileInputStream(path + applicationProperties));
			p = new Properties();
			p.load(in); 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 根据key 获取value
	 * @param key
	 * @return value
	 */
	public static String get(String key){
		String result = null;
		result = p.getProperty(key);
		return result;
	}
	
}
