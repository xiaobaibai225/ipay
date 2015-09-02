package com.pay.framework.payment.icbcpay;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class IOUtils {
	/**
	  * 方法描述：把交互报文写入日志报文文件中
	  * @version: 1.0
	  * @author: www.sinoufc.com
	  * @time: 2014-3-12 上午9:35:52
	  */
	public static void writeLogXml(String seqno, String xmlContent, String bankFlag, String orderType, String xmlType) throws IOException {
		try {
			String dateName = ComUtils.getCurrentDate2("yyyyMMdd");
			String xmlDateDir = ComUtils.removePathLastStr(BusinessConstant.PROJECT_LOG_PATH) + File.separator + "xml_log/" + bankFlag + "/" + xmlType + "/" + dateName;
			File dateDir = new File(xmlDateDir);
			if (!dateDir.isDirectory()) {
				dateDir.mkdirs();
			}
			String xmlFile = xmlDateDir + "/" + seqno + "-" + orderType + ".xml.log";
			FileOutputStream fos = new FileOutputStream(xmlFile);
			OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
			PrintWriter out = new PrintWriter(osw);
			out.println(xmlContent);
			out.close();
		} catch (Exception e) {
			throw new IOException("交互报文写入日志报文文件中出错--" + e.getMessage());
		}
	}
}
