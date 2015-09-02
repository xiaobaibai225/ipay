package com.pay.framework.payment.ccbmispostpay;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.RandomStringUtils;

import com.pay.framework.log.LogManager;
import com.pay.model.Check;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;

public class CCBMisPostPayUtil {
	
private static int xls = 1;
private static LogManager logger = LogManager.getLogger(CCBMisPostPayUtil.class);
	
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
	
	// 获取MIS流水号  6 年月日 + 3位序列数。 2015年开始。
	public static String MisTrace(){
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		String ymd = sdf.format(date);
		StringBuilder result  = new StringBuilder();
		String [] dic = {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z","0","1","2","3","4","5","6","7","8","9"};//"abcdefghigklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
		String year = ymd.split("-")[0];
		String month = ymd.split("-")[1];
		String day = ymd.split("-")[2];
		String hh = ymd.split("-")[3];
		String mm = ymd.split("-")[4];
		String ss = ymd.split("-")[5];
		
		//result.append(dic[(Integer.parseInt(year)-2015)%dic.length]);
		result.append(dic[Integer.parseInt(month)]);
		result.append(dic[Integer.parseInt(day)]);
		result.append(dic[Integer.parseInt(hh)]);
		result.append(dic[Integer.parseInt(mm)]);
		result.append(dic[Integer.parseInt(ss)]);
		//result.append(dic[(xls++)%dic.length]);
		result.append(dic[(Integer.parseInt(RandomStringUtils.randomNumeric(4)))%dic.length]);
		
		return result.toString();
	}
	
	//转换字符串
	public static String string_real(String value ,String str) {
		StringBuffer stringBuffer = new StringBuffer();
		String chinese = "[\u4e00-\u9fa5]";
		for (int i = 0; i < value.length(); i++) {
			String temp = value.substring(i, i + 1);
			if (temp.matches(chinese)) {
				stringBuffer.append(temp+str);			
			} else {
				stringBuffer.append(temp);
			}
		}
		return stringBuffer.toString();
	}
	
	/**
	 * 解析excel个文件
	 */
	
	/*public static List<Check>  getCheck (File file){
		
		List<Check> list = new ArrayList<Check>();
		jxl.Workbook readwb = null;   
		  
        try    
  
        {   
  
            //构建Workbook对象, 只读Workbook对象   
  
            //直接从本地文件创建Workbook   
  
            InputStream instream = new FileInputStream(file);
  
            readwb = Workbook.getWorkbook(instream);   
  
            //Sheet的下标是从0开始   
  
            //获取第一张Sheet表   
  
            Sheet readsheet = readwb.getSheet(0);   
  
            //获取Sheet表中所包含的总列数   
  
            int rsColumns = readsheet.getColumns();   
  
            //获取Sheet表中所包含的总行数   
  
            int rsRows = readsheet.getRows();   
  
            //获取指定单元格的对象引用   
            
            for (int r = 1; r < rsRows; r++) {
            	Check check = new Check();
    			Cell[] row = readsheet.getRow(r);
    			if (row == null) {
    				continue;// 如果这行为空，跳过这行，继续遍历余下的行
    		    }
    			
    			
    			
    			
            }
            for (int i = 0; i < rsRows; i++)   
  
            {   
  
                for (int j = 0; j < rsColumns; j++)   
  
                {   
  
                    Cell cell = readsheet.getCell(j, i);   
  
                    System.out.print(cell.getContents() + " ");   
  
                }   
  
                System.out.println();   
  
            }   
  
  
        } catch (Exception e) {   
  
            e.printStackTrace();   
  
        } finally {   
  
            readwb.close();   
  
        }   
		
		return null;
	}
	
	*/
	
	/**
	 * 解析txt文件  最初版本
	 */
	public static Map<String,List> getChecks1(File file){
		
		Map<String,List> map = new HashMap<String,List>();
		List<Check> payList = new ArrayList<Check>();
		List<Check> refundList = new ArrayList<Check>();
		try {
			InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "GBK");
			BufferedReader br = new BufferedReader(isr);
			//BufferedReader br = new BufferedReader(new FileReader(file));//构造一个BufferedReader类来读取文件
			String s = null;
			//s = br.readLine();
		//终端号 | 发卡行 | 卡种 | 卡号-序列号 | 交易日期 | 交易时间 | 交易类型 | 授权号 | 交易金额 | 小费 | 分期期数 | 银行手续费 | ----11
		//	DCC返还手续费 | 划帐金额 | 凭证号 | 批次号 | POS交易序号 | 结算账号 | 订单号 | 柜台编号 | POS编号 | 系统参考号 |----21
			while((s = br.readLine())!=null){//使用readLine方法，一次读一行
				logger.debug("读取行信息","s==="+s);
				if(s.length()<=0) continue;
				String strarr [] = s.split("\\|");
				Check check = new Check();
				check.setAccountname(strarr[1]);//交易账号所属银行
				check.setAccountno(strarr[17]);//交易账号
				check.setBank("ccbMispos");
				check.setOrdernumber(strarr[18]);//该值为空（）
    			//String td = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new SimpleDateFormat("yyyy-MM-ddHH:mm:ss").parse(strarr[4]+strarr[5]));
				//check.setTransdate(td);//交易日期
				check.setTransdate(strarr[4]+" "+strarr[5]);
				check.setTranseq(strarr[21]);//系统参考号
				check.setFee(Double.toString(Double.parseDouble(strarr[11])-Double.parseDouble(strarr[12])));//手续费-DCC返还手续费
				check.setExt((strarr[21]+strarr[4]+strarr[0]).replace(" ", "").replace("-", ""));
				logger.debug("是否是消费", "strarr[6]=="+strarr[6]);
				if("消费".equals(strarr[6])){
					// 支付成功
					check.setNetvalue(strarr[13].trim());//划账金额
					check.setPrice(strarr[8].trim());//交易金额
					payList.add(check);
				}else if("退款".equals(strarr[6])){
					// 退货成功
					check.setNetvalue("-"+strarr[13].trim());//划账金额
					check.setPrice("-"+strarr[8].trim());//退款金额
					refundList.add(check);
				}
			}
		} catch (FileNotFoundException e) {
			logger.debug("文件未找到", e.getMessage());
		}
		catch (Exception e) {
			logger.debug("文件处理异常", e.getMessage());
		}
		logger.debug("建行mispos", "支付和退货size"+payList.size()+"---"+refundList.size());
		map.put("payList", payList);
		map.put("refundList", refundList);
		return map;
	}
	
	/**
	 * 解析txt文件 最终版本
	 */
	public static List<Check> getChecks(File file){
		List<Check> list = new ArrayList<Check>();
		try {
			InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "GBK");
			BufferedReader br = new BufferedReader(isr);
			//BufferedReader br = new BufferedReader(new FileReader(file));//构造一个BufferedReader类来读取文件
			String s = null;
			//s = br.readLine();
			//终端号|交易卡号|批次号|流水号|交易日期|摘要|交易金额|手续费|分期期数|
			while((s = br.readLine())!=null){//使用readLine方法，一次读一行
				logger.debug("读取行信息","s==="+s);
				if(s.length()<=0) continue;
				String strarr [] = s.split("\\|");
				Check check = new Check();
				check.setAccountname("");//交易账号所属银行
				check.setAccountno(strarr[1]);//交易账号
				check.setBank("ccbMispos");
				check.setOrdernumber("");//该值为空
    			//String td = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new SimpleDateFormat("yyyy-MM-ddHH:mm:ss").parse(strarr[4]+strarr[5]));
				//check.setTransdate(td);//交易日期
				check.setTransdate(strarr[4]);
				check.setTranseq(strarr[3]);//系统参考号
				//check.setFee(Double.toString(Double.parseDouble(strarr[11])-Double.parseDouble(strarr[12])));//手续费-DCC返还手续费
				check.setFee(strarr[7]);
				check.setExt((strarr[3]+strarr[4]+strarr[0]).replace(" ", "").replace("-", ""));
				logger.debug("是否是消费", "strarr[5]=="+strarr[5]);
				if("210".equals(strarr[5])){
					// 支付成功
					//check.setNetvalue(strarr[13].trim());//划账金额
					check.setPrice(strarr[6].trim());//交易金额
					list.add(check);
				}else if("230".equals(strarr[5])){
					// 退货成功
					//check.setNetvalue("-"+strarr[13].trim());//划账金额
					check.setPrice("-"+strarr[6].trim());//退款金额
					list.add(check);
				}
			}
		} catch (FileNotFoundException e) {
			logger.debug("文件未找到", e.getMessage());
		}
		catch (Exception e) {
			logger.debug("文件处理异常", e.getMessage());
		}
		return list;
	}
	
	
	/**
	 * 将文件移动到另一目录下
	 */
	public static void moveFile(File oldFile , String des){
		   File fnewpath = new File(des);
		   if(!fnewpath.exists())
		     fnewpath.mkdirs();
		   File fnew = new File(des+oldFile.getName());
		   oldFile.renameTo(fnew);
	}
	
	/**
	 * 读取某个目录下的所有文件
	 */
	
	public static List<File> getFileList(String src){
		
		List<File> list = new ArrayList<File>();
		try{
			File f=new File(src);                 //新建文件实例
			File[] tempList=f.listFiles();        /* 此处获取文件夹下的所有文件 */
			for(int i=0;i<tempList.length;i++){
				if (tempList[i].isFile()) {
					list.add(tempList[i]);
				}
			}
		}catch (NullPointerException e) {
		    return list;
		}
		return list ;
	}
	
}
