package com.pay.framework.payment.ccbpay;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.servlet.http.HttpServletRequest;

import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

import com.pay.framework.log.LogManager;
import com.pay.framework.util.XmlUtil;

/**
 * 建行支付工具类
 * @author zidanezhang
 *
 */
public class CCBPayUtil {
	
	private static LogManager  logger = LogManager.getLogger(CCBPay.class);
	public static final int BUFFER = 2048; 
	public static final String EXT = ".gz";
	
	
	
	  public static Map<String, String> getAllRequestParam(HttpServletRequest request){
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
     * 使用js的escape()方法对“客户注册信息”和“商品信息”进行转码，
     * 数字字母信息不需转码。例：escape(小飞侠)= %u5C0F%u98DE%u4FA0
	 * @param src
	 * @return
	 */
	public static String escape(String src) {
		try {
			if(src==null || "".equals(src)){
				return "";
			}
			int i;
			char j;
			StringBuffer tmp = new StringBuffer();
			tmp.ensureCapacity(src.length() * 6);
			for (i = 0; i < src.length(); i++) {
				j = src.charAt(i);
				if (Character.isDigit(j) || Character.isLowerCase(j)
						|| Character.isUpperCase(j))
					tmp.append(j);
				else if (j < 256) {
					tmp.append("%");
					if (j < 16)
						tmp.append("0");
					tmp.append(Integer.toString(j, 16));
				} else {
					tmp.append("%u");
					tmp.append(String.valueOf(Integer.toString(j, 16))
							.toUpperCase());
				}
			}
			return tmp.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
	/**
	 * 获取查询（支付或退款流水）的xml
	 * 为了获取兑账文件名
	 * @param paramsMap
	 * @return
	 */
	public static String getQueryXmlStrByParams(Map paramsMap){
		StringBuffer sbf = new StringBuffer("");
		sbf.append("<?xml version='1.0' encoding='GB2312' standalone='yes' ?>");
		sbf.append("<TX>");
		sbf.append("<REQUEST_SN>"+paramsMap.get("REQUEST_SN")+"</REQUEST_SN>");
		sbf.append("<CUST_ID>"+paramsMap.get("CUST_ID")+"</CUST_ID>");
		sbf.append("<USER_ID>"+paramsMap.get("USER_ID")+"</USER_ID>");
		sbf.append("<PASSWORD>"+paramsMap.get("PASSWORD")+"</PASSWORD>");
		sbf.append("<TX_CODE>5W1005</TX_CODE>");
		sbf.append("<LANGUAGE>"+paramsMap.get("LANGUAGE")+"</LANGUAGE>");
	    sbf.append("<TX_INFO>");
		sbf.append("<DATE>"+paramsMap.get("DATE")+"</DATE>");
		sbf.append("<KIND>"+paramsMap.get("KIND")+"</KIND>");
		sbf.append("<FILETYPE>"+paramsMap.get("FILETYPE")+"</FILETYPE>");
		sbf.append("<TYPE>"+paramsMap.get("TYPE")+"</TYPE>");
		sbf.append("<NORDERBY>"+paramsMap.get("NORDERBY")+"</NORDERBY>");
		sbf.append("<POS_CODE>"+paramsMap.get("POS_CODE")+"</POS_CODE>");
		sbf.append("<ORDER>"+paramsMap.get("ORDER")+"</ORDER>");
		sbf.append("<STATUS>"+paramsMap.get("STATUS")+"</STATUS>");
		sbf.append("</TX_INFO>");
		sbf.append("</TX>");
		logger.debug("info--兑账 xml===", sbf.toString());
		return sbf.toString();
	}
	/**
	 * 下载 支付或退款流水 的 兑账文件到本地某路径下
	 * @param paramsMap
	 * @return
	 */
	public static String getDownloadXmlStrByParams(Map paramsMap){
		StringBuffer sbf = new StringBuffer("");
		sbf.append("<?xml version='1.0' encoding='GB2312' standalone='yes' ?>");
		sbf.append("<TX>");
		sbf.append("<REQUEST_SN>"+paramsMap.get("REQUEST_SN")+"</REQUEST_SN>");
		sbf.append("<CUST_ID>"+paramsMap.get("CUST_ID")+"</CUST_ID>");
		sbf.append("<USER_ID>"+paramsMap.get("USER_ID")+"</USER_ID>");
		sbf.append("<PASSWORD>"+paramsMap.get("PASSWORD")+"</PASSWORD>");
		sbf.append("<TX_CODE>6W0111</TX_CODE>");
		sbf.append("<LANGUAGE>"+paramsMap.get("LANGUAGE")+"</LANGUAGE>");
		sbf.append("<TX_INFO>");
		sbf.append("<SOURCE>"+paramsMap.get("FILENAME")+"</SOURCE>");
		sbf.append("<FILEPATH>merchant/shls</FILEPATH>");
		sbf.append("<LOCAL_REMOTE>0</LOCAL_REMOTE>");
		sbf.append("</TX_INFO>");
		sbf.append("</TX>");
		logger.debug("info--兑账 xml===", sbf.toString());
		return sbf.toString();
	}
	
	
	
	/**
	 * 获取退款的xml
	 * @param paramsMap
	 * @return
	 */
	public static String getBackXmlStrByParams(Map paramsMap){
		StringBuffer sbf = new StringBuffer("");
		sbf.append("<?xml version='1.0' encoding='GB2312' standalone='yes' ?>");
		sbf.append("<TX>");
		sbf.append("<REQUEST_SN>"+paramsMap.get("REQUEST_SN")+"</REQUEST_SN>");
		sbf.append("<CUST_ID>"+paramsMap.get("CUST_ID")+"</CUST_ID>");
		sbf.append("<USER_ID>"+paramsMap.get("USER_ID")+"</USER_ID>");
		sbf.append("<PASSWORD>"+paramsMap.get("PASSWORD")+"</PASSWORD>");
		sbf.append("<TX_CODE>5W1004</TX_CODE>");
		sbf.append("<LANGUAGE>"+paramsMap.get("LANGUAGE")+"</LANGUAGE>");
	    sbf.append("<TX_INFO>");
		sbf.append("<MONEY>"+paramsMap.get("MONEY")+"</MONEY>");
		sbf.append("<ORDER>"+paramsMap.get("ORDER")+"</ORDER>");
		sbf.append("</TX_INFO>");
		sbf.append("<SIGN_INFO>"+paramsMap.get("SIGN_INFO")+"</SIGN_INFO>");
		sbf.append("<SIGNCERT>"+paramsMap.get("SIGNCERT")+"</SIGNCERT>");
		sbf.append("</TX>");
		logger.debug("info--退款 xml===", sbf.toString());
		return sbf.toString();
	} 

	/**
	 * （5W1002）商户支付流水查询  请求xml
	 * @param paramsMap
	 * @return
	 */
	public static String getCheckOrderPayXmlStr(Map paramsMap){
		StringBuffer sbf = new StringBuffer("");
		sbf.append("<?xml version='1.0' encoding='GB2312' standalone='yes' ?>");
		sbf.append("<TX>");
		sbf.append("<REQUEST_SN>"+paramsMap.get("REQUEST_SN")+"</REQUEST_SN>");
		sbf.append("<CUST_ID>"+paramsMap.get("CUST_ID")+"</CUST_ID>");
		sbf.append("<USER_ID>"+paramsMap.get("USER_ID")+"</USER_ID>");
		sbf.append("<PASSWORD>"+paramsMap.get("PASSWORD")+"</PASSWORD>");
		sbf.append("<TX_CODE>5W1002</TX_CODE>");
		sbf.append("<LANGUAGE>"+paramsMap.get("LANGUAGE")+"</LANGUAGE>");
	    sbf.append("<TX_INFO>");
		sbf.append("<START></START>");
		sbf.append("<STARTHOUR></STARTHOUR>");
		sbf.append("<STARTMIN></STARTMIN>");
		sbf.append("<END></END>");
		sbf.append("<ENDHOUR></ENDHOUR>");
		sbf.append("<ENDMIN></ENDMIN>");
		sbf.append("<KIND>"+paramsMap.get("KIND")+"</KIND>");
		sbf.append("<ORDER>"+paramsMap.get("ORDER")+"</ORDER>");
		sbf.append("<ACCOUNT></ACCOUNT>");
		sbf.append("<DEXCEL>"+paramsMap.get("DEXCEL")+"</DEXCEL>");
		sbf.append("<MONEY></MONEY>");
		sbf.append("<NORDERBY>"+paramsMap.get("NORDERBY")+"</NORDERBY>");
		sbf.append("<PAGE>"+paramsMap.get("PAGE")+"</PAGE>");
		sbf.append("<POS_CODE>"+paramsMap.get("POS_CODE")+"</POS_CODE>");
		sbf.append("<STATUS>"+paramsMap.get("STATUS")+"</STATUS>");
		sbf.append("</TX_INFO>");
		sbf.append("</TX>");
		logger.debug("info--查询订单支付状态 xml===", sbf.toString());
		return sbf.toString();
	}
	
	/**
	 * 
	 * @param hostname
	 * @param port
	 * @param xmlStr
	 * @return
	 */
	public static String socketCcbEBSS(String hostname, int port,String xmlStr,boolean flag){
		String result="";
		try {
			logger.debug("info","--socket begin--");
			InetSocketAddress inetSocketAddress = new InetSocketAddress(hostname, port);
			SocketChannel socketChannel = SocketChannel.open(inetSocketAddress);
			socketChannel.configureBlocking(flag);
			ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER);
			socketChannel.write(ByteBuffer.wrap(xmlStr.getBytes()));
			while (true) {
				byteBuffer.clear();
				int readBytes = socketChannel.read(byteBuffer);
				if (readBytes > 0) {
					byteBuffer.flip();
					result= new String(byteBuffer.array(), 0, readBytes);
					socketChannel.close();
					break;
				}
			}
			logger.debug("info--socket result===", result);
			logger.debug("info","return code is:"+XmlUtil.getContentByKeyOnly(result, "//TX/RETURN_CODE"));

		} catch (IOException e) {
			e.printStackTrace();
			logger.error("socket exception==", e.toString());
		}
		return result;

	}

	/**
	 * 文件解压缩 ,在原路径下
	 * @param url    "E:/fileName/"
	 * @param filename   "SHOP.105110073992025.20150105.02.success.txt.gz"
	 * @param delete   是否删除原始文件
	 * @throws Exception
	 */
    public static String decompress(String url,String filename, boolean delete) throws Exception {
    	logger.debug("info--decompress--", "--对账文件下载---文件解压缩开始-----");
    	File file = new File(url+filename);
        FileInputStream fis = new FileInputStream(file); 
        FileOutputStream fos = new FileOutputStream(file.getPath().replace(EXT, "")); 
        GZIPInputStream gis = new GZIPInputStream(fis); 
        logger.debug("info--decompress--", "--对账文件下载---文件解压缩中----BUFFER="+BUFFER);
        int count; 
        byte data[] = new byte[BUFFER]; 
        while ((count = gis.read(data, 0, BUFFER)) != -1) { 
        	fos.write(data, 0, count); 
        } 

        gis.close(); 
        fis.close(); 
        fos.flush(); 
        fos.close(); 

        if (delete) { 
            file.delete(); 
        } 
        logger.debug("info--decompress--", "--对账文件下载---文件解压缩结束---解压后文件全路径名"+file.getPath().replace(EXT,""));
        return file.getPath().replace(EXT,"");
    }  


    /**
     * 从smbMachine读取文件并存储到localpath指定的路径  
     * @param smbMachineurl  共享机器的文件,如smb://soho:soho123@192.168.163.4/download/,soho:soho123是共享机器的用户名密码  
     * @param filename   文件名字  SHOP.105110073992025.20150105.02.success.txt.gz
     * @param localpath  本地路径  
     * @return
     */
    public static File readFromSmb(String smbMachineurl,String filename,String localpath){   
    	logger.debug("info--readFromSmb--", "--cmb远程读取文件保存到本地-开始--");
        File localfile=null;   
        InputStream bis=null;   
        OutputStream bos=null;   
        try{   
            SmbFile rmifile = new SmbFile(smbMachineurl+filename);   
//            String filename=rmifile.getName();   
            bis=new BufferedInputStream(new SmbFileInputStream(rmifile));   
            localfile=new File(localpath+File.separator+filename);  
            logger.debug("info--readFromSmb--", "--localfile=="+localfile);
            bos=new BufferedOutputStream(new FileOutputStream(localfile));   
            int length=rmifile.getContentLength();  
            logger.debug("info--readFromSmb--", "--length=="+length);
            byte[] buffer=new byte[length];   
            bis.read(buffer);  
            bos.write(buffer); 
        } catch (Exception e){    
        	e.printStackTrace();
            logger.debug("info--readFromSmb--", "--exception=="+e.toString());
        }finally{   
            try {   
                bos.close();   
                bis.close();   
            } catch (IOException e) {   
                e.printStackTrace();   
                logger.debug("info--IOException--", "--exception=="+e.toString());
            }               
        }   
        logger.debug("info--readFromSmb--", "--cmb远程读取文件保存到本地-结束--filepath="+localfile.getPath());
        return localfile;   
    }   


}
