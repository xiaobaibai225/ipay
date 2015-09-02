package com.pay.model;

import com.pay.framework.annotation.db.Column;
import com.pay.framework.annotation.db.DBExclude;
import com.pay.framework.annotation.db.Table;

@Table("orderform")
public class OrderFormPay {

	private String ordernumber;

	private String username;

	//显示的金额
	@DBExclude
	private String money;
	
	private long price;

	private String submitdate;

	private String paymentdate;

	private String dodate;

	private String status;

	private int paytype;

	private String transeq;

	private String userid;

	private String ip;

	private String memo;
	// 商品类型
	private String producttype;
	// 商户订单号
	private String corderid;

	// 支付部门编号
	private String deptid;

	// 产品id
	private String productid;

	private String chargetype;
	// 公司id 
	private String companyid;

	// 服务器notifyurl
	private String backurl;

	// 页面通知url
	private String fronturl;

	// 商品名称
	private String productname;

	private String defaultbank;// 默认银行
	
	private String buyType;
	
	private String openId;
	
	private String ext1;
	
	private String ext2;
	
	private String ext3;
	
	private String ext4;
	
	private String ext5;
	
	public String getOpenId() {
		return openId;
	}
	public String getExt1() {
		return ext1;
	}
	public void setExt1(String ext1) {
		this.ext1 = ext1;
	}
	public String getExt2() {
		return ext2;
	}
	public void setExt2(String ext2) {
		this.ext2 = ext2;
	}
	public String getExt3() {
		return ext3;
	}
	public void setExt3(String ext3) {
		this.ext3 = ext3;
	}
	public String getExt4() {
		return ext4;
	}
	public void setExt4(String ext4) {
		this.ext4 = ext4;
	}
	public String getExt5() {
		return ext5;
	}
	public void setExt5(String ext5) {
		this.ext5 = ext5;
	}
	public void setOpenId(String openId) {
		this.openId = openId;
	}

	public String getBuyType() {
		return buyType;
	}

	public void setBuyType(String buyType) {
		this.buyType = buyType;
	}


	public String getOrdernumber() {
		return ordernumber;
	}

	public void setOrdernumber(String ordernumber) {
		this.ordernumber = ordernumber;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}


	public String getSubmitdate() {
		return submitdate;
	}

	public void setSubmitdate(String submitdate) {
		this.submitdate = submitdate;
	}

	public String getPaymentdate() {
		return paymentdate;
	}

	public void setPaymentdate(String paymentdate) {
		this.paymentdate = paymentdate;
	}


	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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


	public String getMoney() {
		return money;
	}

	public void setMoney(String money) {
		this.money = money;
	}

	public long getPrice() {
		return price;
	}

	public void setPrice(long price) {
		this.price = price;
	}

	public String getDodate() {
		return dodate;
	}

	public void setDodate(String dodate) {
		this.dodate = dodate;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public String getProducttype() {
		return producttype;
	}

	public void setProducttype(String producttype) {
		this.producttype = producttype;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public String getCorderid() {
		return corderid;
	}

	public void setCorderid(String corderid) {
		this.corderid = corderid;
	}

	public String getDeptid() {
		return deptid;
	}

	public void setDeptid(String deptid) {
		this.deptid = deptid;
	}

	public String getProductid() {
		return productid;
	}

	public void setProductid(String productid) {
		this.productid = productid;
	}

	public String getChargetype() {
		return chargetype;
	}

	public void setChargetype(String chargetype) {
		this.chargetype = chargetype;
	}

	public String getCompanyid() {
		return companyid;
	}

	public void setCompanyid(String companyid) {
		this.companyid = companyid;
	}

	public String getBackurl() {
		return backurl;
	}

	public void setBackurl(String backurl) {
		this.backurl = backurl;
	}

	public String getFronturl() {
		return fronturl;
	}

	public void setFronturl(String fronturl) {
		this.fronturl = fronturl;
	}

	public String getProductname() {
		return productname;
	}

	public void setProductname(String productname) {
		this.productname = productname;
	}

	public String getDefaultbank() {
		return defaultbank;
	}

	public void setDefaultbank(String defaultbank) {
		this.defaultbank = defaultbank;
	}

	public String getExt() {
		return ext;
	}

	public void setExt(String ext) {
		this.ext = ext;
	}

	public int getProductnum() {
		return productnum;
	}

	public void setProductnum(int productnum) {
		this.productnum = productnum;
	}

	// 扩展字段
	private String ext;

	/**
	 * 购买数量
	 */
	private int productnum;


	

}
