package com.pay.model;

/**
 * 支付状态
 * @author PCCW1
 *
 */
public class PayStatus {
	public static final int PAY_FAIL = 0;//支付失败
	public static final int PAY_SUCCESS = 1;//支付、消费都成功
	public static final int PAY_SUCCESS_NOT_XIAOFEI = 2;//支付成功，消费没成功
	public static final int PAY_SUCCESS_REFUND = 3;//成功退款
	public static final int PAY_NO_ORDER = 3;//订单号不存在
	public static final int PAY_NOT_MATCH = 4;//金额不匹配
	public static final int PAY_VALIDATE_FAIL = 5;//验证失败
	public static final String PAY_SUCCESS_TOPAY = "success";
	public static final int REFUND_SUCCESS=1;//退款成功
	public static final int REFUND_FAIL=0;//退款失败
	
}
