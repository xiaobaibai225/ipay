package com.pay.framework.payment.icbcpay;

public class UFCException extends Exception{
	/* serialVersionUID: serialVersionUID */
	private static final long serialVersionUID = -7009699203670111341L;

	public UFCException() {
		super();
	}

	public UFCException(String message, Throwable cause) {
		super(message, cause);
	}

	public UFCException(String message) {
		super(message);
	}

	public UFCException(Throwable cause) {
		super(cause);
	}
}
