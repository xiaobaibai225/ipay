package com.pay.framework.payment.unionpay.moto;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import sun.misc.BASE64Decoder;

import com.pay.framework.log.LogManager;
import com.pay.framework.payment.BasePay;
import com.pay.framework.payment.IPay;
import com.pay.framework.payment.unionpay.UnionPay;
import com.pay.framework.payment.unionpay.moto.pay.PayDelegate_PayPort_Client;
import com.pay.framework.payment.unionpay.moto.util.DESUtil;
import com.pay.framework.payment.unionpay.moto.util.PropertiesUtil;
import com.pay.framework.service.OrderFormService;
import com.pay.framework.service.PayService;
import com.pay.framework.util.XmlUtil;
import com.pay.model.Check;
import com.pay.model.OrderFormUnPay;
import com.pay.model.PaymentBean;

/**
 * 外卡支付
 * @author kapstoy
 *
 */

@Component("motopay")
public class MotoPay extends BasePay implements IPay {
	
	LogManager logManager = LogManager.getLogger(MotoPay.class);

	@Autowired
	private OrderFormService orderFormService;

	@Autowired
	private PayService payService;

	@Override
	public String getForwardUrl(HttpServletRequest request,
			HttpServletResponse response, int payTypeId) {
		OrderFormUnPay orderform = beforePay(request, payTypeId);
		if (null == orderform) {
			return null;
		}
		PaymentBean payment = payTypeService.getPayType(orderform.getPaytype());
		String result=PayDelegate_PayPort_Client.callPay(orderform, payment);
		if(!"".equals(result))
		{
			 String returncode = XmlUtil.getContentByKeyOnly(result, "//BJPos/Body/ResCode");
			 if("00".equals(returncode))
			 {
				 String referNo=XmlUtil.getContentByKeyOnly(result, "//BJPos/Body/ReferenceNo");
				 	super.afterServerNotify(orderform,
				 			referNo);
			 }
			 StringBuffer sbf=new StringBuffer();
		        byte[] dr;
				try {
					dr = ( new BASE64Decoder()).decodeBuffer(orderform.getMemo());
				} catch (IOException e) {
					e.printStackTrace();
					 logManager.debug("外币支付解码失败...outer",null);
					return "";
				}
		        byte[] decryResult=null;
				try {
					decryResult = DESUtil.decrypt(dr);
				} catch (Exception e) {
					e.printStackTrace();
					 logManager.debug("外币支付解密失败...outer",null);
					return "";
				}
		        String[] cardInfo=new String(decryResult).split(",");//用此字段存外卡的相关信息
			 return "moto_code::"+returncode+"::"+PropertiesUtil.getInstance().getProValue(returncode+"_"+cardInfo[3])+"##";
		}
		return result;
	}

	@Override
	public int doPaymentServerNotify(HttpServletRequest request,
			HttpServletResponse response) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int doPaymentPageNotify(HttpServletRequest request,
			HttpServletResponse response) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String refund(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int refundNotify(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<Check> check(HttpServletRequest request,
			HttpServletResponse response, int payTypeId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OrderFormUnPay getOrderFormUnPay(HttpServletRequest request,
			HttpServletResponse response) {
		// TODO Auto-generated method stub
		return null;
	}

}
