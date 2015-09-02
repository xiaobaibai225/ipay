package com.pay.framework.payment.bocpay.util;

/**
 * com.bocnet.util.Coder
 * 
 * @description
 * 
 * @author bocnet@2014-3-18
 * 
 * @modified_by
 */
public interface Coder {

	public String encode(byte[] data);

	public byte[] decode(String string);

}