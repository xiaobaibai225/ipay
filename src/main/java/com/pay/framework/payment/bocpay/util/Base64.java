package com.pay.framework.payment.bocpay.util;

/**
 * com.bocnet.util.Base64
 * 
 * @description
 * 
 * @author bocnet@2014-3-18
 * 
 * @modified_by
 */
public final class Base64 {
	private static final Base64Coder coder = new Base64Coder();

	public static String encode(byte abyte0[]) {
		return coder.encode(abyte0);
	}

	public static byte[] decode(String s) {
		return coder.decode(s);
	}
}
