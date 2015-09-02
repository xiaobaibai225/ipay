package com.pay.framework.payment.icbcpay;

import java.util.HashMap;

public class BusinessConstant {
	public static String PROJECT_CONFIG_FILE_PATH="D:/sinoufc/config/";
	public static String PROJECT_LOG_PATH ="D:/sinoufc/log/";
	public static final String PARA_CONFIG_BANKFLAG_T = "t"; //业务标志
	
	
	//存放对应关系,  1层:区分哪家银行  2层:区分某个银行的某种交易类型  3层:某种交易类型的对应关系
	public static HashMap<String, HashMap<String, HashMap<String, String>>> relationMap = new HashMap<String, HashMap<String, HashMap<String, String>>>();

		
	public static HashMap<String, HashMap<String, String>> getRelationMap(String bankFlag) {
		return BusinessConstant.relationMap.get(bankFlag);
	}

	public static void setRelationMap(String bankFlag, HashMap<String, HashMap<String, String>> relationMap) {
		BusinessConstant.relationMap.put(bankFlag, relationMap);
	}

}
