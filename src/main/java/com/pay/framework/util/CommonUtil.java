package com.pay.framework.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.apache.commons.lang.RandomStringUtils;



public class CommonUtil {
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
  
    private static String getBusinessNumber() {
        Date date = new Date();
        return sdf.format(date);
    }
    
    public static String getCmbOrderFormNumber() {
        Date d = new Date();
    	//给出定单日期
    	//给出不重复的定单号
    	Integer number = d.getYear() * 366 + d.getMonth() * 31 + d.getDate();
    	number = (number *24 + d.getHours() ) * 60 + d.getMinutes();
    	number = number * 60 + d.getSeconds();
    	
    	String ordernumber = number.toString().substring(1);
    	ordernumber += new Random().nextInt(10);
    	
        return  ordernumber;
    }
    

    
    /**
     * 获取订单号，同步
     * @return
     */
	public static String getOrderFormNumber(){
    	return getBusinessNumber() + RandomStringUtils.randomNumeric(6);
    }
}
