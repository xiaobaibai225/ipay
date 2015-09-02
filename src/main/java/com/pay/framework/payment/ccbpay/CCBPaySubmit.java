package com.pay.framework.payment.ccbpay;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pay.framework.log.LogManager;
import com.pay.framework.util.XmlUtil;
import com.pay.model.Check;
import com.pay.model.PaymentBean;

/**
 * 建行支付
 * @author zidanezhang
 *
 */
public class CCBPaySubmit {
	
//	private static final String CCBPAY_GATEWAY= "https://ibsbjstar.ccb.com.cn/app/ccbMain?";
	LogManager logManager = LogManager.getLogger(CCBPay.class);
	
	
	public static List<Check> checkSocketEBSS(Map<String, String> sParaTemp,PaymentBean payment,String TYPE) throws Exception{
		List<Check> list = new ArrayList<Check>();
		sParaTemp.put("TYPE", TYPE);//0支付流水1退款流水
		String  extparams = payment.getExt();//ip,port,operatecode,pwd
		String[] ebssparams = extparams.split(",");
//		获取兑账请求socket xml字符串
		String sendXml = CCBPayUtil.getQueryXmlStrByParams(sParaTemp);
		String result = CCBPayUtil.socketCcbEBSS(ebssparams[0], Integer.parseInt(ebssparams[1]), sendXml,true);
		String returncode = XmlUtil.getContentByKeyOnly(result, "//TX/RETURN_CODE");
		if("000000".equals(returncode)){
			String filename = XmlUtil.getContentByKeyOnly(result, "//TX/TX_INFO/FILE_NAME");
			sParaTemp.put("FILENAME", filename);
			String downloadXml = CCBPayUtil.getDownloadXmlStrByParams(sParaTemp);
			String downloadresult = CCBPayUtil.socketCcbEBSS(ebssparams[0], Integer.parseInt(ebssparams[1]), downloadXml,true);
			String downloadreturncode = XmlUtil.getContentByKeyOnly(downloadresult, "//TX/RETURN_CODE");
			if("000000".equals(downloadreturncode)){
				CCBPayUtil.readFromSmb(payment.getExtCol1(), filename, payment.getQuerykey());
				String ungzUrl = CCBPayUtil.decompress(payment.getQuerykey(), filename, false);
				File file = new File(ungzUrl);
				BufferedReader br = new BufferedReader(new FileReader(file));//构造一个BufferedReader类来读取文件
				String s = null;
				while((s = br.readLine())!=null){//使用readLine方法，一次读一行
					if(s.length()>5&&s.substring(0, 4).matches("^[0-9]{4}$")){
						String[] array = s.split("	");//tab
//支付[2015-01-05 11:43:30, 20150105110850353495, ***************4575, 0.01, 0.00, 755183625, --, --, 本地, 成功, 20150105]
//退款[2015-01-05 13:48:32	20150104191915069726	0.01	0.01	755183625	本地	成功	20150105]	
						String postid = TYPE=="0"?array[5].trim():array[4].trim();
						if(!postid.equals(sParaTemp.get("POS_CODE")))continue;
						String price =TYPE=="0"?array[3].trim():"-"+array[3].trim(); 
						String accountNum =TYPE=="0"?array[2].trim():""; 
						Check check = new Check();
						check.setAccountname("");
						check.setAccountno(accountNum);
						check.setBank("CCB");
						check.setOrdernumber(array[1].trim());
						check.setPrice(price);
						check.setTransdate(array[0].trim());
						check.setTranseq("");
						check.setFee("0.00");
						check.setNetvalue(price);
						list.add(check);
					}
				}
				br.close();  
			}
		}
		
		return list;
	}

	public static String refundSocketEBSS(Map<String, String> sParaTemp,PaymentBean payment){
		String errorMsg = "";
		String  extparams = payment.getExt();//ip,port,operatecode,pwd
		String[] ebssparams = extparams.split(",");
		String refundXml = CCBPayUtil.getBackXmlStrByParams(sParaTemp);
		String result = CCBPayUtil.socketCcbEBSS(ebssparams[0], Integer.parseInt(ebssparams[1]), refundXml,false);
		String returncode = XmlUtil.getContentByKeyOnly(result, "//TX/RETURN_CODE");
		if("000000".equals(returncode)){
			
		}else{
			errorMsg = XmlUtil.getContentByKeyOnly(result, "//TX/RETURN_MSG");
			
		}
		return errorMsg;
	}
	
	public static Map checkOrderPay(Map sParaTemp){
		Map map = new HashMap();
		String orderXml = CCBPayUtil.getCheckOrderPayXmlStr(sParaTemp);
		String result = CCBPayUtil.socketCcbEBSS(sParaTemp.get("ip")+"", Integer.parseInt(sParaTemp.get("port")+""), orderXml,false);
		String returncode = XmlUtil.getContentByKeyOnly(result, "//TX/RETURN_CODE");
		if("000000".equals(returncode)){
			String orderStatu = XmlUtil.getContentByKeyOnly(result, "//TX/TX_INFO/LIST/ORDER_STATUS");
			String money = XmlUtil.getContentByKeyOnly(result, "//TX/TX_INFO/LIST/PAYMENT_MONEY");
			map.put("orderStatu", orderStatu);
			map.put("money", money);
			map.put("success", "true");
		}else{
			map.put("success", "false");
		}
		return map;
	}
	

	
	
}
