package com.pay.framework.util;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.pay.model.Check;

public class XmlUtil {

	/**
	 * 获取相应的xml获取相应元素的值，比如
	 * <code><?xml version="1.0" encoding="utf-8"?><alipay><is_success>F</is_success><error>ILLEGAL_PARTNER</error></alipay></code>
	 * <br>
	 * 获取is_success的值
	 * 
	 * @param xmlStr
	 * @param level
	 *            需要传类似//alipay/is_success
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static List<String> getContentByKey(String xmlStr, String level) {
		Document document = null;
		SAXReader reader = new SAXReader();
		List<String> valueList = new ArrayList<String>();
		try {
			document = reader.read(new ByteArrayInputStream(xmlStr.trim().getBytes()));
			List list = document.selectNodes(level);
			Iterator iter = list.iterator();

			while (iter.hasNext()) {
				Element element = (Element) iter.next();
				String value = element.getText();
				valueList.add(value);
			}

		} catch (DocumentException e) {
			
		}
		return valueList;
	}
	
	/**
	 * 获取相应的xml获取相应元素的值，比如
	 * <code><?xml version="1.0" encoding="utf-8"?><alipay><is_success>F</is_success><error>ILLEGAL_PARTNER</error></alipay></code>
	 * <br>
	 * 获取is_success的值
	 * 
	 * @param xmlStr
	 * @param level
	 *            需要传类似//alipay/is_success
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static String getContentByKeyOnly(String xmlStr, String level) {
		Document document = null;
		SAXReader reader = new SAXReader();
		List<String> valueList = new ArrayList<String>();
		try {
			document = reader.read(new ByteArrayInputStream(xmlStr.trim().getBytes()));
			List list = document.selectNodes(level);
			Iterator iter = list.iterator();

			while (iter.hasNext()) {
				Element element = (Element) iter.next();
				String value = element.getText();
				valueList.add(value);
			}

		} catch (DocumentException e) {
			
		}
		if(valueList == null || valueList.size() <1){
			return null;
		}else{
			String confirmValue = valueList.get(0);
			return confirmValue;
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static List<Check> getCheckResult(String xmlStr, String level) {
		List<Check> retCheck = new ArrayList<Check>();
		Document document = null;
		SAXReader reader = new SAXReader();
		try {
			document = reader.read(new ByteArrayInputStream(xmlStr.trim().getBytes()));
			List list = document.selectNodes(level);
			Iterator iter = list.iterator();
			Map map=new HashMap();

			while (iter.hasNext()) {
				Element element = (Element) iter.next();
				String sr=element.elementText("trans_code_msg");
				String ordernumber = element.elementText("merchant_out_order_no");
				String price = element.elementText("income");
				price=price.equals("0.00")?"-"+element.elementText("outcome"):price;
				String transdate = element.elementText("trans_date");
				String transeq= element.elementText("trade_no");
				String bank=element.elementText("deposit_bank_no");
				String accountno=element.elementText("buyer_account");
				if(map.containsKey(ordernumber))
				{
					Check ctemp=(Check)map.get(ordernumber);
					if("收费".equals(sr))
					{
						ctemp.setFee(price);
					}
					else
					{
						if(price.startsWith("-"))//如果是当天退款，则需要新生成一笔check对象
						{
							Check check = new Check();
							check.setOrdernumber(ordernumber);
							check.setAccountname("");//支付宝无法取得对应的账号名称
							check.setPrice(price);
							check.setTranseq(transeq);
							check.setTransdate(transdate);
							check.setBank(bank);
							check.setAccountno(accountno);
							check.setFee("0.00");
							check.setNetvalue((new BigDecimal(check.getPrice())).add((new BigDecimal(check.getFee()))).toString());
							retCheck.add(check);
							continue;
						}
						ctemp.setPrice(price);
						ctemp.setTranseq(transeq);
						ctemp.setTransdate(transdate);
						ctemp.setBank(bank);
						ctemp.setAccountno(accountno);
					}
					ctemp.setNetvalue((new BigDecimal(ctemp.getPrice())).add((new BigDecimal(ctemp.getFee()))).toString());
				}
				else
				{	

					Check check = new Check();
					check.setOrdernumber(ordernumber);
					check.setAccountname("");//支付宝无法取得对应的账号名称
					check.setPrice("0.00");
					check.setFee("0.00");
					check.setTransdate(transdate);
					if("收费".equals(sr))
					{
						check.setFee(price);
					}
					else
					{
						check.setPrice(price);
						check.setTranseq(transeq);
						//check.setTransdate(transdate);
						check.setBank(bank);
						check.setAccountno(accountno);
					}
					check.setNetvalue((new BigDecimal(check.getPrice())).add((new BigDecimal(check.getFee()))).toString());
					retCheck.add(check);
					map.put(ordernumber, check);
				}


			}

		} catch (DocumentException e) {
			
		}
		return retCheck;
	}


}
