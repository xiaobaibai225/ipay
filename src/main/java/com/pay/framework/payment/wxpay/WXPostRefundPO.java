package com.pay.framework.payment.wxpay;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.pay.model.PaymentBean;
import com.pay.model.Refund;

public class WXPostRefundPO {
	
	private String  	appid ="";
	private String  	mch_id ="";
	private String  	nonce_str ="";
	private String  	sign ="";
	private String  	transaction_id ="";
	private String  	out_trade_no ="";
	private String  	 	out_refund_no  ="";
	private String  	total_fee ="";
	private String  	refund_fee ="";
	private String  	op_user_id ="";
	
	public WXPostRefundPO(PaymentBean pay,Refund refund)
	{
		setAppid(pay.getExt());
		setMch_id(pay.getMerchantid());
		setNonce_str(RandomStringGenerator.getRandomStringByLength(32));
		setTransaction_id(refund.getTranseq());
		setOut_trade_no(refund.getOrdernumber());
		setOut_refund_no(refund.getOuterfundno());
		setTotal_fee(refund.getTotalmoney()+"");
		setRefund_fee(refund.getRefundmoney()+"");
		setOp_user_id(pay.getMerchantid());
   	 	String sign = Signature.getSign(toMap(),pay.getSeckey());
   	 	setSign(sign);
	}
	
	public String getAppid() {
		return appid;
	}
	public void setAppid(String appid) {
		this.appid = appid;
	}
	public String getMch_id() {
		return mch_id;
	}
	public void setMch_id(String mch_id) {
		this.mch_id = mch_id;
	}
	public String getNonce_str() {
		return nonce_str;
	}
	public void setNonce_str(String nonce_str) {
		this.nonce_str = nonce_str;
	}
	public String getSign() {
		return sign;
	}
	public void setSign(String sign) {
		this.sign = sign;
	}
	public String getTransaction_id() {
		return transaction_id;
	}
	public void setTransaction_id(String transaction_id) {
		this.transaction_id = transaction_id;
	}
	public String getOut_trade_no() {
		return out_trade_no;
	}
	public void setOut_trade_no(String out_trade_no) {
		this.out_trade_no = out_trade_no;
	}
	public String getOut_refund_no() {
		return out_refund_no;
	}
	public void setOut_refund_no(String out_refund_no) {
		this.out_refund_no = out_refund_no;
	}
	public String getTotal_fee() {
		return total_fee;
	}
	public void setTotal_fee(String total_fee) {
		this.total_fee = total_fee;
	}
	public String getRefund_fee() {
		return refund_fee;
	}
	public void setRefund_fee(String refund_fee) {
		this.refund_fee = refund_fee;
	}
	public String getOp_user_id() {
		return op_user_id;
	}
	public void setOp_user_id(String op_user_id) {
		this.op_user_id = op_user_id;
	}
	
    public Map<String,Object> toMap(){
        Map<String,Object> map = new HashMap<String, Object>();
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            Object obj;
            try {
                obj = field.get(this);
                if(obj!=null){
                    map.put(field.getName(), obj);
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return map;
    }

}
