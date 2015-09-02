package com.pay.model;
public class ComplexOrderBean {
	int errorCode;
	OrderFormPay orderform;
	OrderFormUnPay orderformunpay;
	public final static int ERROR_ORDER_ISNULL=1;//订单不存在
	public final static int ERROR_ORDER_DIGEST_FAIL=2;//签名错误
	public final static int SUCCESSED=0;
	public ComplexOrderBean(int errorCode, OrderFormPay orderform, OrderFormUnPay orderformunpay) {
		super();
		this.errorCode = errorCode;
		this.orderform = orderform;
		this.orderformunpay = orderformunpay;
	}
	public int getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
	public OrderFormPay getOrderform() {
		return orderform;
	}
	public void setOrderform(OrderFormPay orderform) {
		this.orderform = orderform;
	}
	public OrderFormUnPay getOrderformunpay() {
		return orderformunpay;
	}
	public void setOrderformunpay(OrderFormUnPay orderformunpay) {
		this.orderformunpay = orderformunpay;
	}
	
}
