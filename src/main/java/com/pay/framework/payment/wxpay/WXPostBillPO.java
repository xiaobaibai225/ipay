package com.pay.framework.payment.wxpay;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.pay.model.PaymentBean;

public class WXPostBillPO {
	
	private String appid="";
	private String mch_id="";
	private String nonce_str="";
	private String sign="";
	private String bill_date="";
	private String  	bill_type ="";
	


	public WXPostBillPO(PaymentBean pay,String billDate)
	{
		setAppid(pay.getExt());
		setMch_id(pay.getMerchantid());
		setNonce_str(RandomStringGenerator.getRandomStringByLength(32));
		setBill_date(billDate);
		setBill_type("ALL");
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
	public String getBill_date() {
		return bill_date;
	}
	public void setBill_date(String bill_date) {
		this.bill_date = bill_date;
	}
	
	public String getBill_type() {
		return bill_type;
	}

	public void setBill_type(String bill_type) {
		this.bill_type = bill_type;
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
