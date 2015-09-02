package com.pay.framework.payment.wxpay;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.pay.model.OrderFormUnPay;
import com.pay.model.PaymentBean;

public class WXPostPO {
	
    //每个字段具体的意思请查看API文档
    private String appid = "";
    private String mch_id = "";
    private String nonce_str = "";
    private String sign = "";
    private String body = "";
    private String out_trade_no = "";
    private long total_fee = 0;
    private String spbill_create_ip = "";
    private String notify_url;
    private String trade_type;
    private String product_id;
    
    public WXPostPO(PaymentBean pay,OrderFormUnPay orderform)
    {
    	setAppid(pay.getExt());
    	setMch_id(pay.getMerchantid());
    	setNonce_str(RandomStringGenerator.getRandomStringByLength(32));
    	setBody(orderform.getProductdesc());
    	setOut_trade_no(orderform.getOrdernumber());
    	setTotal_fee(orderform.getPrice());
    	setSpbill_create_ip(orderform.getIp());
    	setNotify_url(pay.getBgurl());
    	setTrade_type("NATIVE");
    	setProduct_id(orderform.getProductid());
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
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String getOut_trade_no() {
		return out_trade_no;
	}
	public void setOut_trade_no(String out_trade_no) {
		this.out_trade_no = out_trade_no;
	}
	public long getTotal_fee() {
		return total_fee;
	}
	public void setTotal_fee(long total_fee) {
		this.total_fee = total_fee;
	}
	public String getSpbill_create_ip() {
		return spbill_create_ip;
	}
	public void setSpbill_create_ip(String spbill_create_ip) {
		this.spbill_create_ip = spbill_create_ip;
	}
	public String getNotify_url() {
		return notify_url;
	}
	public void setNotify_url(String notify_url) {
		this.notify_url = notify_url;
	}
	public String getTrade_type() {
		return trade_type;
	}
	public void setTrade_type(String trade_type) {
		this.trade_type = trade_type;
	}
	public String getProduct_id() {
		return product_id;
	}
	public void setProduct_id(String product_id) {
		this.product_id = product_id;
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
