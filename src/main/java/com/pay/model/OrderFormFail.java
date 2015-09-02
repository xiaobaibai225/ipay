package com.pay.model;

public class OrderFormFail {
	
	private String ordernumber;
	
	private String reason;
	
	private int status;
	
	private int fail_counts;

	public String getOrdernumber() {
		
		return ordernumber;
	}

	public void setOrdernumber(String ordernumber) {
		this.ordernumber = ordernumber;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getFail_counts() {
		return fail_counts;
	}

	public void setFail_counts(Integer fail_counts) {
		this.fail_counts = fail_counts;
	}


}
