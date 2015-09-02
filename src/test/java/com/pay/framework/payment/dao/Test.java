package com.pay.framework.payment.dao;

import java.util.Date;

import com.pay.framework.util.DateUtil;

public class Test {

	public static void main(String[] args) {
		
		String dd="2014-12-15";
	Date d=	DateUtil.formatToDate(dd, DateUtil.C_DATE_PATTON_DEFAULT);
       System.out.println(d.getTime());
       System.out.print(System.currentTimeMillis());
	}

}
