package com.pay.model;

public class Check {
	private String corderid;
	private String price;
	private String transdate;
	private String ordernumber;
	private String paytype;
	private String transeq;
	private String bank;
	private String accountno;
	private String accountname;
	private String fee;
	private String netvalue;
	
	private String ext;
	public String getNetvalue() {
		return netvalue;
	}
	public void setNetvalue(String netvalue) {
		this.netvalue = netvalue;
	}
	public String getFee() {
		return fee;
	}
	public void setFee(String fee) {
		this.fee = fee;
	}
	public String getPaytype() {
		return paytype;
	}
	public void setPaytype(String paytype) {
		this.paytype = paytype;
	}
	public String getTranseq() {
		return transeq;
	}
	public void setTranseq(String transeq) {
		this.transeq = transeq;
	}
	public String getBank() {
		return bank;
	}
	public void setBank(String bank) {
		this.bank = bank;
	}
	public String getAccountno() {
		return accountno;
	}
	public void setAccountno(String accountno) {
		this.accountno = accountno;
	}
	public String getAccountname() {
		return accountname;
	}
	public void setAccountname(String accountname) {
		this.accountname = accountname;
	}
	public String getCorderid() {
		return corderid;
	}
	public void setCorderid(String corderid) {
		this.corderid = corderid;
	}
	public String getPrice() {
		return price;
	}
	public void setPrice(String price) {
		this.price = price;
	}
	public String getTransdate() {
		return transdate;
	}
	public void setTransdate(String transdate) {
		this.transdate = transdate;
	}
	public String getOrdernumber() {
		return ordernumber;
	}
	public void setOrdernumber(String ordernumber) {
		this.ordernumber = ordernumber;
	}
	public String getExt() {
		return ext;
	}
	public void setExt(String ext) {
		this.ext = ext;
	}

}
