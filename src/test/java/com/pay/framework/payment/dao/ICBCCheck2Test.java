package com.pay.framework.payment.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pay.framework.payment.icbcpay.ICBCPaySubmit;
import com.pay.model.Check;
import com.pay.model.PaymentBean;

public class ICBCCheck2Test {
	public static void main(String[] args) {
		List<Check> resultcheck = new ArrayList<Check>();
		//PaymentBean payment = payTypeService.getPayType(payTypeId);
		PaymentBean payment = new PaymentBean();//15
		payment.setExt7("B2CPAYINF");
		payment.setExt6("020000459999AAA");
		payment.setExt5("102");
		payment.setExt4("test20141223-1.y.0200");
		payment.setExt3("501");
		payment.setExt2("500");
		payment.setExt1("192.168.180.10");
		payment.setMerchantid("0200EC24375827");
		payment.setQuerykey("0200004519000100173");
		// 把请求参数打包成数组
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat sdf2 = new SimpleDateFormat("HHmmss");//TODO 精确到毫秒
		SimpleDateFormat sdf3 = new SimpleDateFormat("yyyyMMddHHmmssSSS");//yyyyMMddHHmmssSSS
		Date datenow = new Date();
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -1);
		Date dateBefor = cal.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		System.out.println("对账日期："+sdf.format(dateBefor));
		
		Map<String, String> sParaTemp = new HashMap<String, String>();
		sParaTemp.put("TransCode", payment.getExt7());
		sParaTemp.put("CIS", payment.getExt6());
		sParaTemp.put("BankCode", payment.getExt5());
		sParaTemp.put("ID", payment.getExt4());
		sParaTemp.put("TranDate", sdf1.format(datenow));
		sParaTemp.put("TranTime", sdf2.format(datenow)+"000000");
		sParaTemp.put("fSeqno", "201504230000005");//ERP系统产生的指令包序列号，一个集团永远不能重复 // TODO
		sParaTemp.put("QryFlag", "0");//0：使用北京时间查询   1：使用工行主机系统时间查询
		sParaTemp.put("ShopType", "2");//1：B2B商城  2：B2C商城 3：C2C商城
		sParaTemp.put("ShopCode", payment.getMerchantid());
		sParaTemp.put("ShopAcct", payment.getQuerykey());
		sParaTemp.put("QrySerialNo", "");
		sParaTemp.put("QryOrderNum", "");
		sParaTemp.put("BeginDate", "20150428");// 对账，前一天的
		sParaTemp.put("EndDate", "20150428");// 对账，前一天的
		sParaTemp.put("BeginTime", "");
		sParaTemp.put("EndTime", "");
		sParaTemp.put("ResultType", "010");// 成功
		sParaTemp.put("NextTag", "");
		sParaTemp.put("ErpOrder", "");
		sParaTemp.put("QueryType", "1");// 订单对账查询
		sParaTemp.put("AcctSeq", "");
		
		try {
			//支付对账
			//List<Check> payCheck = ICBCPaySubmit.checkSocketEBSS(sParaTemp, payment,"1");
			//System.out.println("支付对账："+payCheck.size());
			
			//退款对账
			Map<String, String> sParaRefundTemp = new HashMap<String, String>();
			payment.setExt7("B2CEJEINF");
			sParaRefundTemp.put("TransCode", payment.getExt7());
			sParaRefundTemp.put("CIS",payment.getExt6());
			sParaRefundTemp.put("BankCode", payment.getExt5());
			sParaRefundTemp.put("ID", payment.getExt4());
			sParaRefundTemp.put("TranDate", sdf1.format(datenow));
			sParaRefundTemp.put("TranTime", sdf2.format(datenow)+"000000");
			sParaRefundTemp.put("fSeqno", "201504230000020");
			sParaRefundTemp.put("Ordertype", "0");//0：退货 1：返还 2：转付
			sParaRefundTemp.put("ShopType", "2");///1：B2B商城  2：B2C商城 3：C2C商城
			sParaRefundTemp.put("ShopCode",payment.getMerchantid());
			sParaRefundTemp.put("ShopAcct", payment.getQuerykey());
			sParaRefundTemp.put("QrySerialNo", "");
			sParaRefundTemp.put("QryOrderNum", "");
			sParaRefundTemp.put("BeginDate", "20150430");
			sParaRefundTemp.put("EndDate", "20150430");
			sParaRefundTemp.put("BeginTime", "");
			sParaRefundTemp.put("EndTime", "");
			sParaRefundTemp.put("ResultType", "010");
			sParaRefundTemp.put("PTOrderNo", "");
			sParaRefundTemp.put("NextTag", "1");
			sParaRefundTemp.put("IfSeqno", "");
			sParaRefundTemp.put("AcctSeq", "");
			List<Check> refundCheck = ICBCPaySubmit.checkSocketEBSS(sParaRefundTemp, payment,"2");
			System.out.println("退货对账："+refundCheck.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
