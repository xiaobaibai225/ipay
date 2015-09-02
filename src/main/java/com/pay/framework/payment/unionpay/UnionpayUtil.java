package com.pay.framework.payment.unionpay;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import com.pay.framework.log.LogManager;
import com.pay.framework.payment.unionpay.util.DateStyle;
import com.pay.framework.payment.unionpay.util.DateUtil;
import com.pay.model.Check;
import com.unionpay.acp.sdk.HttpClient;
import com.unionpay.acp.sdk.SDKConstants;
import com.unionpay.acp.sdk.SDKUtil;
import com.unionpay.acp.sdk.SecureUtil;

/**
 * 银联支付工具类
 * @author kapstoy
 *
 */
public class UnionpayUtil {
	
	private static LogManager logger = LogManager.getLogger(UnionpayUtil.class);
	
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
	  
	  public static Map<String, String> signData(Map<String, ?> contentData,String encoding) {
			Entry<String, String> obj = null;
			Map<String, String> submitFromData = new HashMap<String, String>();
			for (Iterator<?> it = contentData.entrySet().iterator(); it.hasNext();) {
				obj = (Entry<String, String>) it.next();

				if (null != obj.getValue()
						&& StringUtils.isNotEmpty(String.valueOf(obj.getValue()))) {
					submitFromData
							.put(obj.getKey(), String.valueOf(obj.getValue()));
					logger.debug(obj.getKey() + "-->"+ String.valueOf(obj.getValue()),null);
				}
			}

			/**
			 * 签名
			 */
			SDKUtil.sign(submitFromData, encoding);

			return submitFromData;
		}
		
		public static    Map<String, String> submitDate(Map<String, ?> contentData,String requestUrl) {

			Map<String, String> submitFromData = (Map<String, String>)signData(contentData,"UTF-8");

			return submitUrl(submitFromData,requestUrl);
		}
		
		public static Map<String, String> submitUrl(
				Map<String, String> submitFromData,String requestUrl) {
			String resultString = "";
			logger.debug("requestUrl====" , requestUrl);
			logger.debug("submitFromData====" , submitFromData.toString());
			/**
			 * 发送
			 */
			HttpClient hc = new HttpClient(requestUrl, 30000, 30000);
			try {
				int status = hc.send(submitFromData, "UTF-8");
				logger.debug("status====" , status+"");
				if (200 == status) {
					resultString = hc.getResult();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			Map<String, String> resData = new HashMap<String, String>();
			/**
			 * 验证签名
			 */
			if (null != resultString && !"".equals(resultString)) {
				// 将返回结果转换为map
				resData = SDKUtil.convertResultStringToMap(resultString);
				if (SDKUtil.validate(resData,  "UTF-8")) {
					logger.debug("验证签名成功",null);
				} else {
					logger.debug("验证签名失败",null);
				}
				// 打印返回报文
				logger.debug("打印返回报文：" ,resultString);
			}
			return resData;
		}
		/**
		 * 解析返回文件
		 */
		public static File deCodeFileContent(Map<String, String> resData,String root) {
			// 解析返回文件
			File file=null;
			String fileContent = resData.get(SDKConstants.param_fileContent);
			if (null != fileContent && !"".equals(fileContent)) {
				try {
					byte[] fileArray = SecureUtil.inflater(SecureUtil
							.base64Decode(fileContent.getBytes("UTF-8")));
					String filePath = null;
						filePath = root + File.separator + resData.get("fileName");
						logger.debug("下载后的银联文件名为",filePath);
					file = new File(filePath);
					if (file.exists()) {
						file.delete();
					}
					file.createNewFile();
					FileOutputStream out = new FileOutputStream(file);
					out.write(fileArray, 0, fileArray.length);
					out.flush();
					out.close();

				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return file;
		}
		
		public static File deCompressFile(File zipFile) throws Exception
		{
			 ZipInputStream Zin=new ZipInputStream(new FileInputStream(  
		             zipFile));//输入源zip路径  
		     BufferedInputStream Bin=new BufferedInputStream(Zin);  
		     String Parent=zipFile.getPath().replace(".zip", ""); 
		     File Fout=null;  
		     ZipEntry entry;  
		         while((entry = Zin.getNextEntry())!=null && !entry.isDirectory()){  
		             Fout=new File(Parent,entry.getName());  
		             if(!Fout.exists()){  
		                 (new File(Fout.getParent())).mkdirs();  
		             }  
		             FileOutputStream out=new FileOutputStream(Fout);  
		             BufferedOutputStream Bout=new BufferedOutputStream(out);  
		             int b;  
		             while((b=Bin.read())!=-1){  
		                 Bout.write(b);  
		             }  
		             Bout.close();  
		             out.close();  
		             System.out.println(Fout+"解压成功");      
		         }  
		         Bin.close();  
		         Zin.close();  
		         
					File[] files=(new File(Parent)).listFiles();
					File toRead=null;
					for(int i=0;i<files.length;i++)
					{
						if(files[i].getName().contains("INN")) 
						{
							toRead=files[i];
							break;
						}
					}
					return toRead;
		}
		
		 public static List<Check>  readBillFile(File  file,String year){
			 List<Check> retCheck = new ArrayList<Check>();
		        try {
		        			BufferedReader br = new BufferedReader(new FileReader(file));//构造一个BufferedReader类来读取文件
		        			String lineTxt = null;
		        			while((lineTxt = br.readLine())!=null){//使用readLine方法，一次读一行
		        				String type=lineTxt.substring(215,217);
		        				String prestr=type.equals("04")?"-":"";
		        				String price = prestr+lineTxt.substring(66,78);//交易金额
		        				String yp=BigDecimal.valueOf(Long.valueOf(price)).divide(new BigDecimal(100)).toString();
		        				String ordernumber = lineTxt.substring(112,144).trim();//订单号
		        				String transdate =  lineTxt.substring(35,45);//交易日期
		        				String transeq=  lineTxt.substring(87,108);//交易流水号
		        				String accountno= lineTxt.substring(46,65);//付款账号
		        				logger.debug("读出来的行数据为===type",type+":price:"+yp+":ordernumber:"+ordernumber+":transdat:"+transdate+":transeq:"+transeq+":accno:"+accountno);
		        				Check check = new Check();
		        				check.setOrdernumber(ordernumber);
		        				check.setPrice(yp);
		        				check.setTranseq(transeq);
		        				String td=DateUtil.DateToString(DateUtil.StringToDate(year+transdate, DateStyle.YYYYMMDDHHMMSSCN),DateStyle.YYYY_MM_DD_HH_MM_SS);
		        				check.setTransdate(td);
		        				check.setBank("Union");
		        				check.setAccountno(accountno);
		        				check.setAccountname("");//银联对账单没有提供付款方名称
		        				if("-".equals(prestr))//如果是退款交易，则手续费为收入增加，清算金额为支出
		        				{
		        					check.setFee(BigDecimal.valueOf(Long.valueOf(lineTxt.substring(167,179))).divide(new BigDecimal(100)).toString());
		        					String aaa=lineTxt.substring(180,193);
		        					if(aaa.startsWith("C"))//如果为C开头，则表明是公司收银行的钱
		        					{
		        					check.setNetvalue(BigDecimal.valueOf(Long.valueOf(lineTxt.substring(181,193))).divide(new BigDecimal(100)).toString());
		        					}
		        					else
		        					{
		        					check.setNetvalue(BigDecimal.valueOf(Long.valueOf("-"+lineTxt.substring(181,193))).divide(new BigDecimal(100)).toString());	
		        					}
		        				}
		        				else//如果是收入交易，则手续费为支出，清算金额为收入
		        				{
			        				check.setFee(BigDecimal.valueOf(Long.valueOf("-"+lineTxt.substring(167,179))).divide(new BigDecimal(100)).toString());
			        				String aaa=lineTxt.substring(180,193);
			        				if(aaa.startsWith("D"))//如果为D开头，则表明是公司欠银行的钱
			        				{
			        				check.setNetvalue(BigDecimal.valueOf(Long.valueOf("-"+lineTxt.substring(181,193))).divide(new BigDecimal(100)).toString());	
			        				}
			        				else
			        				{
			        				check.setNetvalue(BigDecimal.valueOf(Long.valueOf(lineTxt.substring(181,193))).divide(new BigDecimal(100)).toString());	
			        				}
		        				}
		        				retCheck.add(check);
		        			}
		        			br.close();  
		        } catch (Exception e) {
		        	logger.debug("读取union文件内容出错",null);
		            e.printStackTrace();
		        }
		        return retCheck;
		     
		    }

}
