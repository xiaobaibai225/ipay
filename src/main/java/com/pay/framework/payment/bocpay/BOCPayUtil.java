package com.pay.framework.payment.bocpay;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.RandomStringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.pay.framework.log.LogManager;
import com.pay.framework.payment.unionpay.util.DateStyle;
import com.pay.model.Check;

public class BOCPayUtil {
	private static LogManager logger = LogManager.getLogger(BOCPayUtil.class);
	
	public static Map<String, String> getAllRequestParam(HttpServletRequest request)
	{
		Map res = new HashMap();
		Enumeration temp = request.getParameterNames();
		if (null != temp) {
			while (temp.hasMoreElements()) {
				String en = (String)temp.nextElement();
				String value = request.getParameter(en);
				res.put(en, value);
				if ((res.get(en) == null) || (res.get(en) == ""))
				{
					res.remove(en);
				}
			}
		}
		return res;
	}
	/**
	 * 把 xml 转化成 map
	 * @param xml
	 * @return
	 */
	public static Map readStringXmlOut(String xml) {
		Map map = new HashMap();
		Document doc = null;
		try {
			doc = DocumentHelper.parseText(xml); // 将字符串转为XML
			Element rootElt = doc.getRootElement(); // 获取根节点
			 for (Iterator ie = rootElt.elementIterator(); ie.hasNext();) {  
		            Element element = (Element) ie.next(); 
		            map.put(element.getName(), element.getData());
		            readElements(map,element);
			 }
		} catch (DocumentException e) {
			e.printStackTrace();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}
	public static Map readElements(Map map, Element element ){
		 for (Iterator ieson = element.elementIterator(); ieson.hasNext();) {  
	            Element elementSon = (Element) ieson.next();  
	            map.put(elementSon.getName(),  elementSon.getText());
	            if( elementSon.elementIterator().hasNext()){
	            	readElements(map,elementSon);
	            }
         }
		 return map;
	}
	
	// 商户退款交易流水号 30
	public static String getRefundSeqNo(){
		String str = "";
    	Date date = new Date();
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");//17
    	str = sdf.format(date);
    	str+=RandomStringUtils.randomNumeric(13);
    	return str;
	}
	
	// 商户退款交易流水号 30
		public static String getRefundSeqNoB2B(){
			String str = "";
	    	Date date = new Date();
	    	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");//8
	    	str = sdf.format(date);
	    	str+=RandomStringUtils.randomNumeric(2);
	    	return str;
		}
	/**
	 * 获取对账文件
	 * @param urlStr
	 * @param savePath
	 * @return
	 */
	public static File getCheckFile(String urlStr,String savePath){
		try {
			URL url = new URL(urlStr);
			URLConnection uc = url.openConnection(); 
			String fileName = uc.getHeaderField(3); 
			logger.debug("中行支付", "中行支付获取文件名:"+fileName+"-----test6---"+uc.getHeaderField(6));
			logger.debug("中行支付", "中行支付获取文件名:"+fileName+"-----test5---"+uc.getHeaderField(5));
			logger.debug("中行支付", "中行支付获取文件名:"+fileName+"-----test4---"+uc.getHeaderField(4));
			logger.debug("中行支付", "中行支付获取文件名:"+fileName+"-----test3---"+uc.getHeaderField(3));
			logger.debug("中行支付", "中行支付获取文件名:"+fileName+"-----test2---"+uc.getHeaderField(2));
			logger.debug("中行支付", "中行支付获取文件名:"+fileName+"-----test1---"+uc.getHeaderField(1));
			logger.debug("中行支付", "中行支付获取文件名:"+fileName+"-----test0---"+uc.getHeaderField(0));
			fileName = URLDecoder.decode(fileName.substring(fileName.indexOf("filename=")+9),"UTF-8"); 
			//fileName = "111.zip";
			logger.debug("获取中行对账文件","中行对账文件:"+fileName); 
			String path = savePath+File.separator+fileName; 
			FileOutputStream os = new FileOutputStream(path); 
			InputStream is = uc.getInputStream(); 
			byte[] b = new byte[1024]; 
			int len = 0; 
			while((len=is.read(b))!=-1){ 
			os.write(b,0,len); 
			} 
			os.close(); 
			is.close(); 
			logger.debug("获取中行对账文件成功：","中行对账文件保存路径："+path); 
			File returnfile = new File(path);
			return returnfile;
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return null;
	}
	/**
	 * 解压对账包
	 * @param zipFile
	 * @param descDir
	 * @return
	 * @throws IOException
	 */
	public static File unZipFiles(File zipFile,String descDir)throws IOException{  
	    File pathFile = new File(descDir);  
	    if(!pathFile.exists()){  
	        pathFile.mkdirs();  
	    }  
	    ZipFile zip = new ZipFile(zipFile);  
	    for(Enumeration entries = zip.entries();entries.hasMoreElements();){  
	        ZipEntry entry = (ZipEntry)entries.nextElement();  
	        String zipEntryName = entry.getName();  
	        InputStream in = zip.getInputStream(entry);  
	        String outPath = (descDir+zipEntryName).replaceAll("\\*", "/");;  
	        //判断路径是否存在,不存在则创建文件路径  
	        File file = new File(outPath.substring(0, outPath.lastIndexOf('/')));  
	        if(!file.exists()){  
	            file.mkdirs();  
	        }  
	        //判断文件全路径是否为文件夹,如果是上面已经上传,不需要解压  
	        if(new File(outPath).isDirectory()){  
	            continue;  
	        }  
	        //输出文件路径信息  
	        System.out.println(outPath);  
	          
	        OutputStream out = new FileOutputStream(outPath);  
	        byte[] buf1 = new byte[1024];  
	        int len;  
	        while((len=in.read(buf1))>0){  
	            out.write(buf1,0,len);  
	        }  
	        in.close();  
	        out.close();  
	        return new File(outPath);
	        }  
	    return null;
	}
	// B2B 对账文件解析
	public static Map<String,List> checkFileParseB2B(File file){

		Map<String,List> map = new HashMap<String,List>();
		List payList = new ArrayList();
		List refundList = new ArrayList();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));//构造一个BufferedReader类来读取文件
			String s = null;
			s = br.readLine();
			while((s = br.readLine())!=null){//使用readLine方法，一次读一行
				String strarr [] = s.split("\\|");
				Check check = new Check();
				check.setAccountname(strarr[6]);
				check.setAccountno(strarr[7]);
				check.setBank("BOC");
				check.setOrdernumber(strarr[0]);
				String td=com.pay.framework.payment.unionpay.util.DateUtil.DateToString(com.pay.framework.payment.unionpay.util.DateUtil.StringToDate(strarr[18], DateStyle.YYYYMMDDHHMMSSCN),DateStyle.YYYY_MM_DD_HH_MM_SS);
				check.setTransdate(td);
				check.setTranseq(strarr[1]);
				check.setFee("0.00");
				if("1".equals(strarr[3])||"2".equals(strarr[3])){
					// 支付成功
					check.setPrice(strarr[5].trim());
					check.setNetvalue(strarr[5].trim());
					payList.add(check);
				}else if("3".equals(strarr[3])||"4".equals(strarr[3])){
					// 退货成功
					check.setPrice("-"+strarr[5].trim());
					check.setNetvalue("-"+strarr[5].trim());
					refundList.add(check);
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.debug("中行支付", "支付和退货size"+payList.size()+"---"+refundList.size());
		map.put("payList", payList);
		map.put("refundList", refundList);
		return map;
	
	} 
	// B2C 对账文件解析
	public static Map<String,List> checkFileParse(File file){
		Map<String,List> map = new HashMap<String,List>();
		List payList = new ArrayList();
		List refundList = new ArrayList();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));//构造一个BufferedReader类来读取文件
			String s = null;
			s = br.readLine();
			while((s = br.readLine())!=null){//使用readLine方法，一次读一行
				String strarr [] = s.split("\\|");
				Check check = new Check();
				check.setAccountname("");
				check.setAccountno("");
				check.setBank("BOC");
				check.setOrdernumber(strarr[1]);
				String td=com.pay.framework.payment.unionpay.util.DateUtil.DateToString(com.pay.framework.payment.unionpay.util.DateUtil.StringToDate(strarr[4], DateStyle.YYYYMMDDHHMMSSCN),DateStyle.YYYY_MM_DD_HH_MM_SS);
				check.setTransdate(td);
				check.setTranseq(strarr[2]);
				check.setFee("0.00");
				if("PCEP".equals(strarr[8])){
					// 支付成功
					check.setNetvalue(strarr[6]);
					check.setPrice(strarr[6]);
					payList.add(check);
				}else if("REFP".equals(strarr[8])){
					// 退货成功
					check.setNetvalue("-"+strarr[6]);
					check.setPrice("-"+strarr[6]);
					refundList.add(check);
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.debug("中行支付", "支付和退货size"+payList.size()+"---"+refundList.size());
		map.put("payList", payList);
		map.put("refundList", refundList);
		return map;
	}
	
}
