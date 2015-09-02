package com.pay.model;


import com.pay.framework.annotation.db.Table;

@Table("mispos_orderform")
public class MisPosOrderFormPay {

	private String id;
	private String orid;
	//支付订单号
	private String ordernumber;
	//商户订单号
	private String corderid;
	//交易类型
	private String transType;
	//
	private String misTrace;
	//交易状态
	private String status;
	//提交日期
	private String submitdate;
	//商户名称
	private String merchantName;
	//商户编号
	private String merchantNum;
	//终端编号
	private String terminalNum;
	//交易卡号
	private String transCardNum;
	//卡片有效期
	private String expDat;
	//交易批次号
	private String batchNum;
	//原交易批次号
	private String oldbatchNum;
	//交易凭证号
	private String posTraceNum;
	//原交易凭证号
	private String oldposTraceNum;
	//交易系统检索号
	private String hostTrace;
	//银行记帐日期
	private String settleDat;
	//交易日期
	private String transDat;
	//交易时间
	private String transTim;
	//授权号
	private String authorNum;
	//交易金额
	private String transAmount;
	//积分
	private String loyalty;
	//卡类型
	private String cardType;
	//发卡行代码
	private String cardNameCode;
	//发卡行名称
	private String cardName;
	//
	private int paytype;
	private String ext;
	private String ext1;
	private String ext2;
	private String ext3;
	private String ext4;

	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
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
	public String getTransType() {
		return transType;
	}
	public void setTransType(String transType) {
		this.transType = transType;
	}
	public String getMerchantName() {
		return merchantName;
	}
	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}
	public String getMerchantNum() {
		return merchantNum;
	}
	public void setMerchantNum(String merchantNum) {
		this.merchantNum = merchantNum;
	}
	public String getTerminalNum() {
		return terminalNum;
	}
	public void setTerminalNum(String terminalNum) {
		this.terminalNum = terminalNum;
	}
	public String getTransCardNum() {
		return transCardNum;
	}
	public void setTransCardNum(String transCardNum) {
		this.transCardNum = transCardNum;
	}
	public String getExpDat() {
		return expDat;
	}
	public void setExpDat(String expDat) {
		this.expDat = expDat;
	}
	public String getBatchNum() {
		return batchNum;
	}
	public void setBatchNum(String batchNum) {
		this.batchNum = batchNum;
	}
	public String getOldbatchNum() {
		return oldbatchNum;
	}
	public void setOldbatchNum(String oldbatchNum) {
		this.oldbatchNum = oldbatchNum;
	}
	public String getPosTraceNum() {
		return posTraceNum;
	}
	public void setPosTraceNum(String posTraceNum) {
		this.posTraceNum = posTraceNum;
	}
	public String getOldposTraceNum() {
		return oldposTraceNum;
	}
	public void setOldposTraceNum(String oldposTraceNum) {
		this.oldposTraceNum = oldposTraceNum;
	}
	public String getHostTrace() {
		return hostTrace;
	}
	public void setHostTrace(String hostTrace) {
		this.hostTrace = hostTrace;
	}
	public String getSettleDat() {
		return settleDat;
	}
	public void setSettleDat(String settleDat) {
		this.settleDat = settleDat;
	}
	public String getTransDat() {
		return transDat;
	}
	public void setTransDat(String transDat) {
		this.transDat = transDat;
	}
	public String getTransTim() {
		return transTim;
	}
	public void setTransTim(String transTim) {
		this.transTim = transTim;
	}
	public String getAuthorNum() {
		return authorNum;
	}
	public void setAuthorNum(String authorNum) {
		this.authorNum = authorNum;
	}
	public String getTransAmount() {
		return transAmount;
	}
	public void setTransAmount(String transAmount) {
		this.transAmount = transAmount;
	}
	public String getLoyalty() {
		return loyalty;
	}
	public void setLoyalty(String loyalty) {
		this.loyalty = loyalty;
	}
	public String getCardType() {
		return cardType;
	}
	public void setCardType(String cardType) {
		this.cardType = cardType;
	}
	public String getCardNameCode() {
		return cardNameCode;
	}
	public void setCardNameCode(String cardNameCode) {
		this.cardNameCode = cardNameCode;
	}
	public String getCardName() {
		return cardName;
	}
	public void setCardName(String cardName) {
		this.cardName = cardName;
	}
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getSubmitdate() {
		return submitdate;
	}
	public void setSubmitdate(String submitdate) {
		this.submitdate = submitdate;
	}
	public String getOrid() {
		return orid;
	}
	public void setOrid(String orid) {
		this.orid = orid;
	}
	public String getExt() {
		return ext;
	}
	public void setExt(String ext) {
		this.ext = ext;
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
	public int getPaytype() {
		return paytype;
	}
	public void setPaytype(int paytype) {
		this.paytype = paytype;
	}
	public String getMisTrace() {
		return misTrace;
	}
	public void setMisTrace(String misTrace) {
		this.misTrace = misTrace;
	}

}
