package com.pay.framework.payment.bocpay.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * com.bocnet.utils.PropertiesLoader
 * 
 * @description 属性文件加载器(优先加载类路径下配置文件)
 * @author dawei@2014-3-18
 * @modified_by
 */
public class PropertiesLoader {

	private static Properties prop;

	static {
		String fileName = "boc-config.properties";
		prop = new Properties();
		InputStream in = null;
		try {
			in = new FileInputStream(fileName);//
			InputStreamReader isr = new InputStreamReader(in, "UTF-8");
			prop.load(isr);
		} catch (Throwable t) {
			t.printStackTrace();
			try {
				in = new FileInputStream(fileName);
				prop.load(in);
			} catch (Throwable t1) {
				t1.printStackTrace();
				throw new RuntimeException("fail to load " + fileName);
			}
		} finally {
			try {
				in.close();
			} catch (Exception fe) {
			}
		}
	}

	public static String getProperty(String name) {
		String value = prop.getProperty(name);
		if (value != null) {
			value = new String(value.getBytes());
		}
		return value;
	}
	public static void setProperty(String key, String value) {
		prop.setProperty(key, value);
	}
	public static void main(String[] args) {
		String pwd = PropertiesLoader.getProperty("KeyStoreMcpPassword");
		System.out.println(pwd);
	}

}
