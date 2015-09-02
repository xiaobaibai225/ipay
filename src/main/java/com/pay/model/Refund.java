package com.pay.model;

import com.pay.framework.annotation.db.Column;
import com.pay.framework.annotation.db.Table;

/**
 * 退款
 * @author PCCW
 *
 */
@Table("pay_refund")
public class Refund {
	@Column("order_number")
	private String ordernumber;
	@Column("outer_fund_no")
	private String outerfundno; //退款交易号
	@Column("total_money")
	private long totalmoney;
	@Column("refund_money")
	private long refundmoney;
	@Column("corder_id")
	private String corderid;
	@Column("status")
	private int status;
	@Column("company_id")
	private int companyid;
	@Column("paytype")
	private int paytype;
	@Column("transeq")
	private String transeq; //流水号
	@Column("refund_date")
	private String refundDate;
	public String getOrdernumber() {
		return ordernumber;
	}
	public void setOrdernumber(String ordernumber) {
		this.ordernumber = ordernumber;
	}
	public String getOuterfundno() {
		return outerfundno;
	}
	public void setOuterfundno(String outerfundno) {
		this.outerfundno = outerfundno;
	}
	public long getTotalmoney() {
		return totalmoney;
	}
	public void setTotalmoney(long totalmoney) {
		this.totalmoney = totalmoney;
	}
	public long getRefundmoney() {
		return refundmoney;
	}
	public void setRefundmoney(long refundmoney) {
		this.refundmoney = refundmoney;
	}
	public String getCorderid() {
		return corderid;
	}
	public void setCorderid(String corderid) {
		this.corderid = corderid;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getCompanyid() {
		return companyid;
	}
	public void setCompanyid(int companyid) {
		this.companyid = companyid;
	}
	public int getPaytype() {
		return paytype;
	}
	public void setPaytype(int paytype) {
		this.paytype = paytype;
	}
	public String getTranseq() {
		return transeq;
	}
	public void setTranseq(String transeq) {
		this.transeq = transeq;
	}
	public String getRefundDate() {
		return refundDate;
	}
	public void setRefundDate(String refundDate) {
		this.refundDate = refundDate;
	}

   
}
