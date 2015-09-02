package com.pay.framework.payment.wxpay;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.pay.model.OrderFormUnPay;
import com.pay.model.PaymentBean;

public class WXJSAPIPO {
	
    //每个字段具体的意思请查看API文档
	private String appId;//商户注册具有支付权限的公众号成功后即可获得
	private String timeStamp ;//当前的时间 1970到现在的秒
	private String nonceStr ;//随机字符串，不长于32位
	private String packageWeiXin ;//统一下单接口返回的prepay_id参数值，提交格式如：prepay_id=***
	private String signType ;//签名算法，暂支持MD5
	private String sign; // 签名
	
    
    public WXJSAPIPO(PaymentBean pay,String result,String noce_Str)
    {
    	
    	/*payRequest.setAppid(WX_APPID);
		payRequest.setTimeStamp(System.currentTimeMillis() / 1000);
		payRequest.setNonce_str(payInfo.getId().toString());
		payRequest.setPackageInfo("prepay_id=" + getPrepayId(payInfo, WechatJsPay.PAY_TRADE_TYPE));
		payRequest.setSignType(WechatBaseSign.SIGN_TYPE_MD5);
		payRequest.encryptSign(this.WECHAT_PAYSIGNKEY);*/
    	
    	
    	appId = pay.getExt();//商户注册具有支付权限的公众号成功后即可获得
    	timeStamp = (System.currentTimeMillis()/1000)+"";//当前的时间 1970到现在的秒
    	nonceStr = RandomStringGenerator.getRandomStringByLength(32);//随机字符串，不长于32位
    	packageWeiXin = "prepay_id="+result;//统一下单接口返回的prepay_id参数值，提交格式如：prepay_id=***
    	signType = "MD5";//签名算法，暂支持MD5
    	String sign = Signature.getSign(toMap(),pay.getSeckey());
    	setSign(sign);
    }
    

	public String getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	
	public String getNonceStr() {
		return nonceStr;
	}

	public void setNonceStr(String nonceStr) {
		this.nonceStr = nonceStr;
	}

	public String getPackageWeiXin() {
		return packageWeiXin;
	}

	public void setPackageWeiXin(String packageWeiXin) {
		this.packageWeiXin = packageWeiXin;
	}

	public String getSignType() {
		return signType;
	}

	public void setSignType(String signType) {
		this.signType = signType;
	}

	public Map<String,Object> toMap(){
        Map<String,Object> map = new HashMap<String, Object>();
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            Object obj;
            try {
                obj = field.get(this);
                if(obj!=null){
                	if(field.getName().equals("packageWeiXin")){
                		 map.put("package", obj);
                	}else{
                		map.put(field.getName(), obj);
                	}
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
