package com.pay.framework.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.json.JSONObject;

/**
 * 返回JSON构造器
 * 
 * @author PCCW
 * @version 2013-07-15
 */
public class JsonBuilder {

	public final static int SUCESS_CODE = 0;
	public final static int FAIL_CODE = 1;

	JSONObject json = new JSONObject();

	public JSONObject getJson() {
		return json;
	}

	public JsonBuilder addRecord(String key, String value) {
		json.put(key, value);
		return this;
	}

	/**
	 * 返回成功JSON串
	 * 
	 * @param content
	 * @return
	 */
	public static String sucess(String content) {
		JSONObject json = new JSONObject();
		json.put("code", SUCESS_CODE);
		json.put("msg", content);
		return json.toString();
	}
	
	
	/**
	 * 返回成功JSON串
	 * 
	 * @param content
	 * @return
	 */
	public static String json(Map<String,String> content) {
		JSONObject json = new JSONObject();
		Iterator<Entry<String, String>> it = content.entrySet().iterator();
		while (it.hasNext()) {
		   Map.Entry<String,String> entry = (Map.Entry) it.next();
		   Object key = entry.getKey();
		   Object value = entry.getValue();
		   json.put(key, value);
		 }
		return json.toString();
	}

	/**
	 * 返回失败JSON串
	 * 
	 * @param content
	 * @return
	 */
	public static String fail(String content) {
		JSONObject json = new JSONObject();
		json.put("code", FAIL_CODE);
		json.put("msg", content);
		return json.toString();
	}

	public static void main(String[] args) {
		Map<String,String> haha = new HashMap<String,String>();
		haha.put("code", "1");
		haha.put("money", "1");
		System.out.println(JsonBuilder.json(haha));
	}
}
