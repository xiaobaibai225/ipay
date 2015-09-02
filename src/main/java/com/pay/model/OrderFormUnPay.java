package com.pay.model;

import java.util.Date;

import com.pay.framework.annotation.db.Column;
import com.pay.framework.annotation.db.DBExclude;
import com.pay.framework.annotation.db.Table;

@Table("orderform_unpay")
public class OrderFormUnPay {

	// 商户订单号
	@Column("order_number")
	private String ordernumber;
	// 商户订单号
	@Column("corder_id")
	private String corderid;
	// 用户名
	@Column("user_name")
	private String username;
	// 交易金额
	@Column("price")
	private long price;

	// 提交时间
	@Column("submit_date")
	private String submitdate;
	// 支付方式
	@Column("pay_type")
	private Integer paytype;
	// 产品id
	@Column("product_id")
	private String productid;
	// 用户id
	@Column("user_id")
	private String userid;
	// ip
	private String ip;
	// 注释
	private String memo;
	// 支付部门编号
	@Column("dept_id")
	private String deptid;
	// 是否充值
	@Column("charge_type")
	private String chargetype;
	// 商品类型
	@Column("product_type")
	private String producttype;
	// 商品名称
	@Column("product_name")
	private String productname;
	// 商品描述
	@Column("product_desc")
	private String productdesc;
	// 购买数量
	@Column("product_num")
	private int productnum;
	// 公司id
	@Column("company_id")
	private String companyid;
	// 过期时间
	@Column("expired_time")
	private Date expiredtime;
	// 服务器notifyurl
	@Column("back_url")
	private String backurl;
	// 页面通知url
	@Column("front_url")
	private String fronturl;
	//购买方式
	@Column("buy_type")
	private String buyType;
	@Column("defaultbank")
	private String defaultbank;// 默认银行
	// 扩展字段
	@Column("ext")
	private String ext;
	@Column("ext1")
	private String ext1;
	@Column("ext2")
	private String ext2;
	@Column("ext3")
	private String ext3;
	@Column("ext4")
	private String ext4;
	@Column("ext5")
	private String ext5;
	@DBExclude
	private String signNo;// 一键支付签约号
	@DBExclude
	private String confirmNo;// 用户确认码
	@DBExclude
	private String money;// 金额
	@Column("open_id")
	private String openId;
	@Column("international_card_type")
	private String internationalCardType;
	
	public String getOpenId() {
		return openId;
	}
	public void setOpenId(String openId) {
		this.openId = openId;
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
	public String getInternationalCardType() {
		return internationalCardType;
	}
	public void setInternationalCardType(String internationalCardType) {
		this.internationalCardType = internationalCardType;
	}
	public String getSignNo() {
		return signNo;
	}
	public void setSignNo(String signNo) {
		this.signNo = signNo;
	}
	public String getConfirmNo() {
		return confirmNo;
	}
	public void setConfirmNo(String confirmNo) {
		this.confirmNo = confirmNo;
	}
	public String getOrdernumber() {
		return ordernumber;
	}
	public void setOrdernumber(String ordernumber) {
		this.ordernumber = ordernumber;
	}
	public String getCorderid() {
		return corderid;
	}
	public void setCorderid(String corderid) {
		this.corderid = corderid;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}

	public long getPrice() {
		return price;
	}
	public void setPrice(long price) {
		this.price = price;
	}
	public String getMoney() {
		return money;
	}
	public void setMoney(String money) {
		this.money = money;
	}
	public String getSubmitdate() {
		return submitdate;
	}
	public void setSubmitdate(String submitdate) {
		this.submitdate = submitdate;
	}
	public Integer getPaytype() {
		return paytype;
	}
	public void setPaytype(Integer paytype) {
		this.paytype = paytype;
	}
	public String getProductid() {
		return productid;
	}
	public void setProductid(String productid) {
		this.productid = productid;
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
	public String getDeptid() {
		return deptid;
	}
	public void setDeptid(String deptid) {
		this.deptid = deptid;
	}
	public String getChargetype() {
		return chargetype;
	}
	public void setChargetype(String chargetype) {
		this.chargetype = chargetype;
	}
	public String getProductname() {
		return productname;
	}
	public void setProductname(String productname) {
		this.productname = productname;
	}
	public String getProductdesc() {
		return productdesc;
	}
	public void setProductdesc(String productdesc) {
		this.productdesc = productdesc;
	}
	public int getProductnum() {
		return productnum;
	}
	public void setProductnum(int productnum) {
		this.productnum = productnum;
	}
	public Date getExpiredtime() {
		return expiredtime;
	}
	public void setExpiredtime(Date expiredtime) {
		this.expiredtime = expiredtime;
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
	public String getBuyType() {
		return buyType;
	}
	public void setBuyType(String buyType) {
		this.buyType = buyType;
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
	public String getCompanyid() {
		return companyid;
	}
	public void setCompanyid(String companyid) {
		this.companyid = companyid;
	}

}
