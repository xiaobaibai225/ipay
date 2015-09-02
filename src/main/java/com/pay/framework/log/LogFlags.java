package com.pay.framework.log;

/**
 * 日志的flag
 * @author PCCW
 *
 */
public class LogFlags {
	
	////////////////// 性能相关 ///////////////////
	
	// 时间打印的flag
	public static final String TIME_PRINT = "speed";
	
	///////////////// 支付相关  ///////////////////
	// to pay 
	public static final String PAY_TO_PAY = "跳转支付页面";
	// server notify
	public static final String PAY_SERVER_NOTIFY = "服务端通知";
	// page notify
	public static final String PAY_PAGE_NOTIFY = "页面通知";
	// refund 
	public static final String PAY_REFUND = "退款";
	// mobile notify
	public static final String PAY_MOBILE_SERVER_NOTIFY = "移动支付服务端通知";
	
	public static final String PAY_CANCEL_SERVER_NOTIFY = "取消订单服务端通知";
}
