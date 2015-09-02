package com.pay.model;

import com.pay.framework.annotation.db.Column;
import com.pay.framework.annotation.db.Table;

/**
 * iap支付
 * @author PCCW
 *
 */
@Table("IAP_RECEIPT_DATA")
public class ReceiptData {
	@Column("order_number")
	private java.lang.String ordernumber;
	@Column("RECEIPT_DATA")
	private java.lang.String receiptData; //退款交易号
	@Column("product_id")
	private java.lang.String productId;
	
	@Column("purchase_date_ms")
	private long purchaseDate;
	
	//提交时间
	@Column("submit_date")
	private String submitdate;

	public String getSubmitdate() {
		return submitdate;
	}

	public void setSubmitdate(String submitdate) {
		this.submitdate = submitdate;
	}

	public java.lang.String getOrdernumber() {
		return ordernumber;
	}

	public void setOrdernumber(java.lang.String ordernumber) {
		this.ordernumber = ordernumber;
	}

	public java.lang.String getReceiptData() {
		return receiptData;
	}

	public void setReceiptData(java.lang.String receiptData) {
		this.receiptData = receiptData;
	}

	public java.lang.String getProductId() {
		return productId;
	}

	public void setProductId(java.lang.String productId) {
		this.productId = productId;
	}

	public long getPurchaseDate() {
		return purchaseDate;
	}

	public void setPurchaseDate(long purchaseDate) {
		this.purchaseDate = purchaseDate;
	}



}
