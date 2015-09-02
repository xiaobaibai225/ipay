package com.pay.framework.payment.icbcpay;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.dom4j.DocumentHelper;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;

import com.pay.framework.log.LogManager;

public class XmlProcessorUtils {
	
	public static LogManager logger = LogManager.getLogger(XmlProcessorUtils.class);
	
	/**
	  * 方法描述：普通封装xml对象
	  * @param: xml 报文
	  * @param: flag 01:获取xml的全路径  02:只获取
	  * @version: 1.0
	  * @author: fanxq@sinoufc.com
	  * @time: 2013-12-17 下午4:07:04
	  */
	public static HashMap<String, String> getMap(String xml) throws XmlUFCException {
		try {
			//临时Map
			HashMap<String, String> tmpMap = new HashMap<String, String>();
			//去报文头,只留报文体
			Document doc = DocumentHelper.parseText(xml);
			Element rootElm = doc.getRootElement();
			//System.out.println(rootElm.asXML());
			//判断根节点是否有属性
			List attrs = rootElm.attributes();
			for (int i = 0; i < attrs.size(); i++) {
				Attribute item = (Attribute) attrs.get(i);
				tmpMap.put(rootElm.getName().toLowerCase() + "_|_" + item.getName().toLowerCase(), item.getValue());
			}
			//判断根节点
			if (rootElm.elementIterator().hasNext()) {

				tmpMap = getSubMap(rootElm, tmpMap, rootElm.getName().toLowerCase());
			} else {
				//节点的值封装到Map中
				tmpMap.put(rootElm.getName(), rootElm.getTextTrim() != null && rootElm.getTextTrim().trim().equals("") ? null : rootElm.getTextTrim().trim());
			}
			//返回Map结果集
			return tmpMap;
		} catch (DocumentException e) {
			logger.debug("工行银企互联","Xml解析,普通封装xml对象异常:" + e.getMessage());
			throw new XmlUFCException("普通封装xml对象异常", e);
		}
	}
	/**
	  * 方法描述：递归封装xml对象
	  * @version: 1.0
	  * @author: www.sinoufc.com
	  * @time: 2013-12-17 下午4:08:05
	  */
	private static HashMap<String, String> getSubMap(Element rootElm, HashMap<String, String> tmpMap, String eleName) throws XmlUFCException {
		eleName = eleName + "/";
		//拆分xml
		try {
			for (Iterator it = rootElm.elementIterator(); it.hasNext();) {
				Element element = (Element) it.next();
				//判断节点中是否有属性,属性格式为: 节点名-||-属性名
				List attrs = element.attributes();
				for (int i = 0; i < attrs.size(); i++) {
					Attribute item = (Attribute) attrs.get(i);
					tmpMap.put(eleName + element.getName().toLowerCase() + "_|_" + item.getName(), item.getValue());
				}
				//判断节点中是否有子节点,ture时,递归遍历
				if (element.elementIterator().hasNext()) {
					tmpMap = getSubMap(element, tmpMap, eleName + element.getName().toLowerCase());
				} else {
					//节点的值封装到Map中
					tmpMap.put(eleName + element.getName().toLowerCase(), element.getTextTrim() != null && element.getTextTrim().trim().equals("") ? null : element.getTextTrim().trim());
				}
			}
			return tmpMap;
		} catch (Exception e) {
			logger.debug("工行银企互联","Xml解析,递归封装xml对象异常:" + e.getMessage());
			throw new XmlUFCException("递归封装xml对象异常", e);
		}
	}
	/**
	  * 方法描述：加载响应数据,封装到Map中
	  * @version: 1.0
	  * @author: www.sinoufc.com
	 * @throws XmlUFCException 
	  * @time: 2014-2-4 下午12:44:04
	  */
	public static HashMap<String, Object> packResMap(String id, String resContent, String bankFlag, String typeFlag) throws XmlUFCException {
		try {
			//解响应报文
			HashMap<String, String> resMap = XmlProcessorUtils.getMap(resContent);
			logger.debug("工行银企互联",id + "响应报文封装到Map中: " + resMap.toString());
			//判断处理结果
			HashMap<String, String> mapRelation = XmlProcessorUtils.getMapRelation(bankFlag, typeFlag);
			if (mapRelation == null) {
				//throw new XmlUFCException("交易类型不存在");
			}
			//取报文数据封装Map
			HashMap<String, Object> dbMap = new HashMap<String, Object>();
			for (String key : mapRelation.keySet()) {
				dbMap.put(key, (resMap.get(mapRelation.get(key)) == null) ? "" : resMap.get(mapRelation.get(key)));
			}
			return dbMap;
		} catch (XmlUFCException e) {
			logger.debug("工行银企互联",id + "解析响应报文时出错:");
			throw new XmlUFCException("解析响应报文时出错:" + e.getMessage());
		}
	}
	
	/**
	  * 方法描述：获取映射关系
	  * @version: 1.0
	  * @author: www.sinoufc.com
	  * @time: 2013-12-21 上午10:33:30
	  */
	public static HashMap<String, String> getMapRelation(String bankFlag, String tranType) throws XmlUFCException {
		HashMap<String, HashMap<String, String>> relationMap = null;
		//验证映射关系是否存在
		if (BusinessConstant.getRelationMap(bankFlag) == null) {
			//获取映射关系报文
			String relationContent = XmlProcessorUtils.getMapRelationContent(bankFlag);
			//获取映射关系Map
			relationMap = XmlProcessorUtils.getMapRaletion(relationContent);
			logger.info("工行银企互联","新加载" + bankFlag + "映射关系" + relationMap.toString());
			//把映射关系放入内存中
			BusinessConstant.setRelationMap(bankFlag, relationMap);
		} else {
			relationMap = BusinessConstant.getRelationMap(bankFlag);
		}
		return relationMap.get(tranType);
	}
	/**
	  * 方法描述：得到映射关系字符串
	  * @version: 1.0
	  * @author: www.sinoufc.com
	  * @time: 2013-12-21 上午10:07:22
	  */
	public static String getMapRelationContent(String bankFlag) throws XmlUFCException {
		//String pathFile =  BusinessConstant.PROJECT_MODEL_PATH + bankCode+ "_map.xml";
		String pathFile = ComUtils.removePathLastStr(BusinessConstant.PROJECT_CONFIG_FILE_PATH) + File.separator + "resmap/" + bankFlag + "_map.xml";
		logger.info("工行银企互联",bankFlag + "映射文件路径:" + pathFile);
		try {
			//读取银行的响应报文
			File file = new File(pathFile);
			if(!file.exists()){
				file.createNewFile();
			}
			InputStream in = new FileInputStream(file);
			byte[] buf = new byte[in.available()];
			in.read(buf);
			//转换成字符串类型
			String source = new String(buf, "GBK");
			if (source != null || !source.trim().equals("")) {
				//去空格
				source = ComUtils.removeXMLWhiteSpace(source);
			}
			return source;
		} catch (Exception e) {
			logger.error("工行银企互联","得到映射关系字符串异常:" + e.getMessage());
			throw new XmlUFCException("获得映射关系异常", e);
		}
	}
	/**
	  * 方法描述：映射关系封装xml对象
	  * @param: xml 报文
	  * @version: 1.0
	  * @author: fanxq@sinoufc.com
	  * @time: 2013-12-17 下午4:07:04
	  */
	public static HashMap<String, HashMap<String, String>> getMapRaletion(String xml) throws XmlUFCException {
		try {
			//临时Map
			HashMap<String, HashMap<String, String>> tmpMap = new HashMap<String, HashMap<String, String>>();

			//去报文头,只留报文体
			Document doc = DocumentHelper.parseText(xml);
			Element rootElm = doc.getRootElement();
			//判断根节点
			for (Iterator it = rootElm.elementIterator(); it.hasNext();) {
				Element element = (Element) it.next();
				tmpMap.put(element.getName().toLowerCase(), getSubMapRaletion(element));
			}
			//返回Map结果集
			return tmpMap;
		} catch (DocumentException e) {
			logger.error("工行银企互联","Xml解析,映射关系封装xml对象异常:" + e.getMessage());
			throw new XmlUFCException("映射关系封装xml对象异常", e);
		}
	}
	
	/**
	  * 方法描述：映射关系递归封装xml对象
	  * @version: 1.0
	  * @author: www.sinoufc.com
	  * @time: 2013-12-17 下午4:08:05
	  */
	private static HashMap<String, String> getSubMapRaletion(Element element) throws XmlUFCException {
		HashMap<String, String> tmpMap = new HashMap<String, String>();
		//拆分xml
		try {
			for (Iterator it = element.elementIterator(); it.hasNext();) {
				Element ele = (Element) it.next();
				//节点的值封装到Map中
				tmpMap.put(ele.getName().toLowerCase(), ele.getTextTrim());
			}
			return tmpMap;
		} catch (Exception e) {
			logger.error("工行银企互联","Xml解析,映射关系递归封装xml对象异常:" + e.getMessage());
			throw new XmlUFCException("映射关系递归封装xml对象", e);
		}
	}
}
