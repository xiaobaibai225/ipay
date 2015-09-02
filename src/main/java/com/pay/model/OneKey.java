package com.pay.model;

import com.pay.framework.annotation.db.Column;
import com.pay.framework.annotation.db.Table;

@Table("PAY_ALIPAY_CONTRACT")
public class OneKey {
	@Column("userid") 
	private String userId;
	@Column("username")
	private String userName;
	@Column("external_sign_no")
	private String signNo;
	@Column("BUYER_LOGON_ID")
	private String buyId;
	@Column("company_id")
	private int companyId;
	private int flag;
	private int paytype;
	@Column("submit_date")
	private String submitDate;
	@Column("done_date")
	private String doneDate;


	public String getSubmitDate() {
		return submitDate;
	}

	public void setSubmitDate(String submitDate) {
		this.submitDate = submitDate;
	}

	public String getDoneDate() {
		return doneDate;
	}

	public void setDoneDate(String doneDate) {
		this.doneDate = doneDate;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getSignNo() {
		return signNo;
	}

	public void setSignNo(String signNo) {
		this.signNo = signNo;
	}

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}
	
	public int getPaytype() {
		return paytype;
	}

	public void setPaytype(int paytype) {
		this.paytype = paytype;
	}
	
	public String getBuyId() {
		return buyId;
	}

	public void setBuyId(String buyId) {
		this.buyId = buyId;
	}
	
	public int getCompanyId() {
		return companyId;
	}

	public void setCompanyId(int companyId) {
		this.companyId = companyId;
	}

}
