package com.pay.framework.payment.icbcpay;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.com.infosec.icbc.ReturnValue;

import com.pay.framework.log.LogManager;
import com.pay.framework.payment.BasePay;
import com.pay.framework.payment.IPay;
import com.pay.framework.payment.unionpay.UnionpayUtil;
import com.pay.framework.service.OrderFormService;
import com.pay.framework.service.PayService;
import com.pay.model.Check;
import com.pay.model.OrderFormPay;
import com.pay.model.OrderFormUnPay;
import com.pay.model.PayStatus;
import com.pay.model.PaymentBean;
import com.pay.model.Refund;
@Component("icbcpayb2b")
public class ICBCPayB2B extends BasePay implements IPay {
	LogManager logger = LogManager.getLogger(ICBCPayB2B.class);
	
	@Autowired
	private OrderFormService orderFormService;

	@Autowired
	private PayService payService;
	public static final String yyyyMMddHHmmss = "yyyyMMddHHmmss";
	
	@Override
	public String getForwardUrl(HttpServletRequest request,
			HttpServletResponse response, int payTypeId) {
		logger.debug("进入工行支付", "进入工行B2B支付。。。。。。payTypeId："+payTypeId);
		OrderFormUnPay orderform = beforePay(request, payTypeId);
		if (null == orderform) {
			return null;
		}
		PaymentBean payment = payTypeService.getPayType(orderform.getPaytype());
		// 把请求参数打包成数组
		Date tranTime = new Date();
		DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		
		Map<String, String> m = new HashMap<String, String>();
		m.put("APIName", payment.getExt());//接口名称 
		m.put("APIVersion", payment.getExtCol1());//接口版本号
		m.put("Shop_code", payment.getMerchantid());// 商城代码
		m.put("MerchantURL",payment.getBgurl());
		m.put("ContractNo", orderform.getOrdernumber());
		m.put("ContractAmt", orderform.getPrice()+"");//单位分
		m.put("Account_cur", "001");// 支付的币种,人民币
		m.put("JoinFlag", "2");
		m.put("SendType","1");//成功通知
		if( payment.getExt9() == null || "".endsWith(payment.getExt9())){
			m.put("TranTime", df.format(tranTime));
		}else{
			m.put("TranTime", payment.getExt9());
		}
		String productid = orderform.getProductid() == null ? "":orderform.getProductid().trim();
		if(productid.length() >= 30){
			productid = productid.substring(0, 30);
		}
		m.put("Shop_acc_num", payment.getExt8());//商城账号
		m.put("PayeeAcct", payment.getQuerykey());//收款单位账号
		m.put("GoodsCode", productid);
		m.put("GoodsName", "SOHO3Q");//商品名称
		m.put("Amount", orderform.getProductnum()+"");
		m.put("TransFee", orderform.getPrice()+"");
		m.put("ShopRemark", "");
		m.put("ShopRem", "");
		m.put("PayeeName", "");
		// APIName=B2B&APIVersion=001.001.001.001&Shop_code=商户代码&MerchantURL=支付结果信息通知程序地址
		//&ContractNo=订单号&ContractAmt=订单金额&Account_cur=001&JoinFlag=2&SendType=结果发送类型&TranTime=接收交易日期时间
		//&Shop_acc_num=商城账号&PayeeAcct=收款单位账号
		StringBuilder signStr = new StringBuilder();
		signStr.append("APIName="+m.get("APIName"));
		signStr.append("&APIVersion="+m.get("APIVersion"));
		signStr.append("&Shop_code="+m.get("Shop_code"));
		signStr.append("&MerchantURL="+m.get("MerchantURL"));
		signStr.append("&ContractNo="+m.get("ContractNo"));
		signStr.append("&ContractAmt="+m.get("ContractAmt"));
		signStr.append("&Account_cur=001");
		signStr.append("&JoinFlag=2");
		signStr.append("&SendType=1");
		signStr.append("&TranTime="+m.get("TranTime"));
		signStr.append("&Shop_acc_num="+m.get("Shop_acc_num"));
		signStr.append("&PayeeAcct="+m.get("PayeeAcct"));
		logger.debug("工行b2b签名", "工行b2b签名元数据"+signStr.toString());
		byte [] bsign = null;
		try{
			byte [] signStrB = signStr.toString().getBytes("GBK");
			String password = payment.getMerchantpwd();// 商户密码 
			FileInputStream in2 = new FileInputStream(payment.getPrikey());//user.key
			byte[] bkey = new byte[in2.available()];
			in2.read(bkey);
			in2.close();
			char[] keyPass = password.toCharArray();
			bsign = ReturnValue.sign(signStrB,signStr.toString().length(),bkey,keyPass);
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		if(bsign == null){
			logger.debug("工行B2B签名","工行B2B签名失败" );
			return null;
		}else{
			logger.debug("工行B2B签名", "工行B2B签名成功");
			byte[] EncSign = ReturnValue.base64enc(bsign);
			String SignMsgBase64=new String(EncSign).toString();
			m.put("Mer_Icbc20_signstr",SignMsgBase64);//TODO 生成签名数据
		}
		
		try{
			FileInputStream in1 = new FileInputStream(payment.getPubkey());//user.crt
			byte[] bcert = new byte[in1.available()];
			in1.read(bcert);
			in1.close();
			byte[] EncCert=ReturnValue.base64enc(bcert);
			m.put("Cert", new String(EncCert).toString());//TODO 商户端读取本地商户证书文件后，再使用工行提供的API进行Base64编码后产生的商户证书数据字串。
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		String sHtmlText = ICBCPaySubmit.createHtml(payment.getPosturl(), m);
		logger.debug("工行建立请求", "工行B2B建立请求:"+sHtmlText);
		return sHtmlText;
	}

	@Override
	public int doPaymentServerNotify(HttpServletRequest request,
			HttpServletResponse response) {
		logger.debug("工行接收后台通知", "工行B2B后台通知开始...");
	
		try {
			request.setCharacterEncoding("UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		
		String str11 = request.getParameter("PayeeName");
		logger.debug("工行支付str11", str11);
		
		String PayeeNameAttr = (String)request.getAttribute("PayeeNameAttr");
		logger.debug("工行支付PayeeNameAttr", PayeeNameAttr);
		
		String PayeeName  = "";
		try {
			PayeeName = new String(str11.getBytes("iso-8859-1"),"GBK");
			logger.debug("工行支付PayeeName", PayeeName);
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
		}
		
		Map reqParam = ICBCPayUtil.getAllRequestParam(request);// TODO 处理银行通知返回结果
		logger.debug("工行支付", "工行后台通知返回的参数:"+reqParam);
		//
		String APIName = (String)reqParam.get("APIName") == null ? "" : (String)reqParam.get("APIName");
		String APIVersion = (String)reqParam.get("APIVersion") == null ? "" : (String)reqParam.get("APIVersion");
		String Shop_code = (String)reqParam.get("Shop_code") == null ? "" : (String)reqParam.get("Shop_code");
		String MerchantURL = (String)reqParam.get("MerchantURL") == null ? "" : (String)reqParam.get("MerchantURL");
		String Serial_no = (String)reqParam.get("Serial_no") == null ? "" : (String)reqParam.get("Serial_no");
		String PayStatusZHCN = (String)reqParam.get("PayStatusZHCN") == null ? "" : (String)reqParam.get("PayStatusZHCN");
		String TranErrorCode = (String)reqParam.get("TranErrorCode") == null ? "" : (String)reqParam.get("TranErrorCode");
		String TranErrorMsg = (String)reqParam.get("TranErrorMsg") == null ? "" : (String)reqParam.get("TranErrorMsg");
		String ContractNo = (String)reqParam.get("ContractNo") == null ? "" : (String)reqParam.get("ContractNo");
		String ContractAmt = (String)reqParam.get("ContractAmt") == null ? "" : (String)reqParam.get("ContractAmt");
		String Account_cur = (String)reqParam.get("Account_cur") == null ? "" : (String)reqParam.get("Account_cur");
		String JoinFlag = (String)reqParam.get("JoinFlag") == null ? "" : (String)reqParam.get("JoinFlag");
		String ShopJoinFlag = (String)reqParam.get("ShopJoinFlag") == null ? "" : (String)reqParam.get("ShopJoinFlag");
		String CustJoinFlag = (String)reqParam.get("CustJoinFlag") == null ? "" :(String)reqParam.get("CustJoinFlag");
		String CustJoinNumber = (String)reqParam.get("CustJoinNumber") == null ? "" :(String)reqParam.get("CustJoinNumber");
		String NotifySign = (String)reqParam.get("NotifySign") == null ? "" :(String)reqParam.get("NotifySign"); // 通知的签名信息
		String SendType = (String)reqParam.get("SendType") == null ? "" :(String)reqParam.get("SendType");
		String TranTime = (String)reqParam.get("TranTime")== null ? "" :(String)reqParam.get("TranTime");
		String NotifyTime = (String)reqParam.get("NotifyTime")== null ? "" :(String)reqParam.get("NotifyTime");
		String Shop_acc_num = (String)reqParam.get("Shop_acc_num")== null ? "" :(String)reqParam.get("Shop_acc_num");
		String PayeeAcct  = (String)reqParam.get("PayeeAcct")== null ? "" :(String)reqParam.get("PayeeAcct");
		//String PayeeName  = (String)reqParam.get("PayeeName")== null ? "" :(String)reqParam.get("PayeeName");
		String ShopRem = (String)reqParam.get("ShopRem")== null ? "" :(String)reqParam.get("ShopRem");
		
		// 验签,
		//取出数字签名信息，先使用工行提供的API对数字签名进行Base64解码，然后使用工行提供的API验证签名
		byte[] NotifySignMW = ReturnValue.base64dec(NotifySign.getBytes());
		logger.debug("工行支付", "返回签名数据64 解码1："+ new String(NotifySignMW));
		try {
			NotifySignMW = ReturnValue.base64dec(NotifySign.getBytes("GBK"));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		logger.debug("工行支付", "返回签名数据64 解码2："+ new String(NotifySignMW));
		OrderFormUnPay orderFormUnpay = orderFormService.queryOrderFormUnPaymentByOrderNumber(ContractNo);
		
		if (orderFormUnpay == null) {
			try {
				PrintWriter out = response.getWriter();
				out.println("success");
				out.flush();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			logger.debug("info","支付后台-----工行支付接收后台通知结束......,状态：【"+PayStatus.PAY_NO_ORDER+"】");
			return PayStatus.PAY_NO_ORDER;
		}

		PaymentBean payment = payTypeService.getPayType(orderFormUnpay.getPaytype());
		int a = 1;
		try{
			FileInputStream in1 = new FileInputStream(payment.getSeckey());
			byte[] bcert = new byte[in1.available()];
			in1.read(bcert);
			in1.close();
			
			byte[] EncCert=ReturnValue.base64enc(bcert);
			byte[] DecCert = ReturnValue.base64dec(EncCert);
//			byte[] sign = NotifySign.getBytes("GBK");//获得银行签名数据
//			byte[] EncSign = ReturnValue.base64enc(sign);
//			byte[] DecSign = ReturnValue.base64dec(EncSign);
			//APIName=接口名称&APIVersion=接口版本号&Shop_code=商户代码&MerchantURL=支付结果信息通知程序地址&Serial_no=指令序号&PayStatusZHCN=订单处理状态&TranErrorCode=错误代码&TranErrorMsg=错误描述&ContractNo=订单号&ContractAmt=订单金额&Account_cur=支付币种&JoinFlag=检验联名标志&ShopJoinFlag=商城联名标志&CustJoinFlag=客户联名标志&CustJoinNumber=联名会员号&SendType=结果发送类型&TranTime=接收交易日期时间&NotifyTime=返回通知日期时间&Shop_acc_num=商城账号&PayeeAcct=收款单位账号&PayeeName=收款单位名称
			String src = "APIName="+APIName+"&APIVersion="+APIVersion+"&Shop_code="+Shop_code+"&MerchantURL="+MerchantURL+"&Serial_no="+Serial_no+"&PayStatusZHCN="+PayStatusZHCN+"&TranErrorCode="+TranErrorCode+"&TranErrorMsg="+TranErrorMsg+"&ContractNo="+ContractNo+"&ContractAmt="+ContractAmt+"&Account_cur="+Account_cur+"&JoinFlag="+JoinFlag+"&ShopJoinFlag="+ShopJoinFlag+"&CustJoinFlag="+CustJoinFlag+"&CustJoinNumber="+CustJoinNumber+"&SendType="+SendType+"&TranTime="+TranTime+"&NotifyTime="+NotifyTime+"&Shop_acc_num="+Shop_acc_num+"&PayeeAcct="+PayeeAcct+"&PayeeName="+PayeeName;
			logger.debug("工行支付", "工行支付验签原数据"+src);
			byte srcbyte [] = src.getBytes("GBK");
			// 对银行返回的数据进行验签，
			a = ReturnValue.verifySign(srcbyte,srcbyte.length,DecCert,NotifySignMW);
			logger.debug("工行验签", "验签结果："+a);
		}catch(Exception ex){
			ex.printStackTrace();
			logger.debug("工行验签", "验签失败！");
			try {
				PrintWriter out = response.getWriter();
				out.println("error");
				out.flush();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return PayStatus.PAY_FAIL;
		}
		
		if(a==0){
			// 验签成功
			if("0".equals(PayStatusZHCN) || "3".equals(PayStatusZHCN)){
				logger.debug("工行验签", "工行交易成功，清算开始。。。。");
				// 交易成功
				// 判断金额
				String total_fee = ContractAmt;
				if ((new java.math.BigDecimal(total_fee).multiply(new java.math.BigDecimal(100)).intValue()) < (new java.math.BigDecimal(orderFormUnpay.getPrice()).intValue())) {
					
					try {
						PrintWriter out = response.getWriter();
						out.println("error");
						out.flush();
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					logger.debug("info","支付后台-----工行支付接收后台通知结束......,状态：【"+PayStatus.PAY_NOT_MATCH+"】");
					//return PayStatus.PAY_NOT_MATCH;
				}
				//TODO  要放开后台通知
				logger.debug("工行支付", "开始转移表...");
				super.afterServerNotify(orderFormUnpay,Serial_no);// 交易流水账号
				logger.debug("工行支付", "转移表结束。。。");
				// 工行后台通知方式是 ： 工行实时通知给商户，商户返回供货连接或者中断，银行会把返回的供货连接发送给客户。
				//构造取货连接
				String backurl = orderFormUnpay.getFronturl();
				logger.debug("工行支付", "工行跳转路径11："+backurl);
				
				
				try {
					//PrintWriter out = response.getWriter();
//					response.addHeader("HTTP/1.1 200", "OK");
//					response.addHeader("Server", "Apache/1.39");
//					response.addHeader("Content-Length",backurl.length()+"");
//					response.addHeader("Content-type", "text/html");
//					out.write(backurl);
//					out.flush();
//					out.close();
					
					//backurl=backurl+"&delayFlag=N";
					PrintWriter out = response.getWriter();
					response.setHeader("Cache-Control", "no-cache");
					out.println("<HTML>");
					out.println("<HEAD>");
					out.println("<meta http-equiv=\"refresh\" content=\"0; url='"+backurl+"'\">");
					out.println("</HEAD>");
					out.println("</HTML>");
					out.flush();
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				logger.debug("info","支付后台-----工行支付接收后台通知结束......,状态：【"+PayStatus.PAY_SUCCESS+"】");
				return PayStatus.PAY_SUCCESS;
			}else if("1".equals(PayStatusZHCN)){
				logger.debug("工行验签", "工行交易失败");
				try {
					PrintWriter out = response.getWriter();
					out.println("error");
					out.flush();
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return PayStatus.PAY_FAIL;
			}else if("2".equals(PayStatusZHCN)){
				logger.debug("工行验签", "工行交易可疑");
				try {
					PrintWriter out = response.getWriter();
					out.println("error");
					out.flush();
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return PayStatus.PAY_FAIL;
			}else if("3".equals(PayStatusZHCN)){
				logger.debug("工行验签", "工行等待授权");
				try {
					PrintWriter out = response.getWriter();
					out.println("error");
					out.flush();
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return PayStatus.PAY_FAIL;
			}else{
				logger.debug("工行验签", "等待电话核实");
				try {
					PrintWriter out = response.getWriter();
					out.println("error");
					out.flush();
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return PayStatus.PAY_FAIL;
			}
		}else{
			//验签失败
			logger.debug("info","支付后台-----工行支付接收后台通知，签名验证失败......");
			try {
				PrintWriter out = response.getWriter();
				out.println("error");
				out.flush();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			logger.debug("info","支付后台-----工行支付接收后台通知结束......,状态：【"+PayStatus.PAY_VALIDATE_FAIL+"】");
			return PayStatus.PAY_VALIDATE_FAIL;
		}
	}
	
	public static String toUtf8String(String s) {  
		   StringBuffer sb = new StringBuffer();  
		   for (int i = 0; i < s.length(); i++) {  
		       char c = s.charAt(i);  
		       if (c >= 0 && c <= 255) {  
		           sb.append(c);  
		       } else {  
		           byte[] b;  
		           try {  
		               b = Character.toString(c).getBytes("utf-8");  
		           } catch (Exception ex) {  
		               System.out.println(ex);  
		               b = new byte[0];  
		           }  
		           for (int j = 0; j < b.length; j++) {  
		               int k = b[j];  
		               if (k < 0) {  
		                   k += 256;  
		               }  
		               sb.append("%" + Integer.toHexString(k).  
		                       toUpperCase());  
		           }  
		       }  
		   }  
		   return sb.toString();  
		}  
	
	public static String getBodyString(java.io.BufferedReader br) {
		  String inputLine;
		       String str = "";
		     try {
		       while ((inputLine = br.readLine()) != null) {
		        str += inputLine;
		       }
		       br.close();
		     } catch (IOException e) {
		       System.out.println("IOException: " + e);
		     }
		     return str;
		 }
	
	public static String getUTF8StringFromGBKString(String gbkStr) {   
        try {   
            return new String(getUTF8BytesFromGBKString(gbkStr), "UTF-8");   
       } catch (UnsupportedEncodingException e) {   
           throw new InternalError();   
       }   
    }   
       
   public static byte[] getUTF8BytesFromGBKString(String gbkStr) {   
       int n = gbkStr.length();   
       byte[] utfBytes = new byte[3 * n];   
        int k = 0;   
        for (int i = 0; i < n; i++) {   
           int m = gbkStr.charAt(i);   
          if (m < 128 && m >= 0) {   
                utfBytes[k++] = (byte) m;   
              continue;   
           }   
           utfBytes[k++] = (byte) (0xe0 | (m >> 12));   
       utfBytes[k++] = (byte) (0x80 | ((m >> 6) & 0x3f));   
            utfBytes[k++] = (byte) (0x80 | (m & 0x3f));   
        }   
   if (k < utfBytes.length) {   
           byte[] tmp = new byte[k];   
           System.arraycopy(utfBytes, 0, tmp, 0, k);   
            return tmp;   
        }   
        return utfBytes;   
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
		logger.debug("工行B2B退货","工行B2B退货开始.....");
		Refund refund = super.beforeRefund(request);
		JSONObject json = new JSONObject();
		int statu = PayStatus.REFUND_FAIL;
		String errorMsg = "";
		if (refund == null) {
			json.put("status", statu);
			json.put("errormsg", "already refund all");
			return json.toString();
		}
		PaymentBean payment = payTypeService.getPayType(refund.getPaytype());
		OrderFormPay orderForPay = orderFormService.getOrderByOrderNum(refund.getOrdernumber());
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat sdf2 = new SimpleDateFormat("HHmmssSSS");//TODO 精确到毫秒
		SimpleDateFormat sdf3 = new SimpleDateFormat("yyyyMMddHHmmssSSS");//yyyyMMddHHmmssSSS
		SimpleDateFormat sdf4 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date datenow = new Date();
		
		// 把请求参数打包成数组
		Map<String, String> sParaTemp = new HashMap<String, String>();
		sParaTemp.put("TransCode", "EBUSCOM");
		sParaTemp.put("CIS",payment.getExt6());
		sParaTemp.put("BankCode", payment.getExt5());
		sParaTemp.put("ID", payment.getExt4());
		sParaTemp.put("TranDate", sdf1.format(datenow));
		sParaTemp.put("TranTime", sdf2.format(datenow)+"000");
		sParaTemp.put("fSeqno",ICBCPayUtil.getFSeqno());//fSeqnoERP系统产生的指令包序列号，一个集团永远不能重复
		sParaTemp.put("TranType", "0");
		sParaTemp.put("ShopType", "1");//1：B2B商城  2：B2C商城
		sParaTemp.put("ShopCode", payment.getMerchantid());
		sParaTemp.put("ShopAcct", payment.getQuerykey());
		sParaTemp.put("OrderNum", refund.getOrdernumber());//订单号
		sParaTemp.put("PayType", "0");//支付速度
		sParaTemp.put("PayDate", sdf1.format(sdf4.parse(orderForPay.getSubmitdate())));// 原订购支付日期
		sParaTemp.put("TransferName", "");
		sParaTemp.put("TransferAccNo", "");
		sParaTemp.put("PayAmt", refund.getRefundmoney()+"");
		sParaTemp.put("SignTime", sdf3.format(datenow));
		sParaTemp.put("ReqReserved1", "");
		sParaTemp.put("ReqReserved2", "");
		sParaTemp.put("AcctSeq", "");
		errorMsg = ICBCPaySubmit.refundSocketEBSS(sParaTemp, payment);
		
		if("success".equals(errorMsg)){
			afterRefund(refund.getOrdernumber());
			orderFormService.updatePayStatus(refund.getOrdernumber(), PayStatus.PAY_SUCCESS_REFUND);
			statu = PayStatus.PAY_SUCCESS_REFUND;
		}
		json.put("status", statu);
		json.put("errormsg", errorMsg);
		json.put("corderid", refund.getCorderid());
		json.put("money", refund.getRefundmoney());
	    return json.toString();
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
		if (!beforeCheck(request)) {
			return null;
		}
		List<Check> resultcheck = new ArrayList<Check>();
		logger.debug("工行B2B对账","查询对账信息开始......");
		PaymentBean payment = payTypeService.getPayType(payTypeId);
		// 把请求参数打包成数组
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat sdf2 = new SimpleDateFormat("HHmmss");//TODO 精确到毫秒
		SimpleDateFormat sdf3 = new SimpleDateFormat("yyyyMMddHHmmssSSS");//yyyyMMddHHmmssSSS
		Date datenow = new Date();
		String dateFile = (String)request.getAttribute("starttime");
		dateFile = dateFile.replace("-", "");
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -1);
		Date dateBefor = cal.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		logger.debug("工行对账", "工行对账日期："+sdf.format(dateBefor));
		System.out.println("对账日期："+sdf.format(dateBefor));
		// 支付
		Map<String, String> sParaTemp = new HashMap<String, String>();
		sParaTemp.put("TransCode", payment.getExt7());
		sParaTemp.put("CIS", payment.getExt6());
		sParaTemp.put("BankCode", payment.getExt5());
		sParaTemp.put("ID", payment.getExt4());
		sParaTemp.put("TranDate", sdf1.format(datenow));
		sParaTemp.put("TranTime", sdf2.format(datenow)+"000000");
		sParaTemp.put("fSeqno",ICBCPayUtil.getFSeqno());//ERP系统产生的指令包序列号，一个集团永远不能重复 // TODO
		sParaTemp.put("ShopType", "1");//1：B2B商城  2：B2C商城 3：C2C商城
		sParaTemp.put("ShopCode", payment.getMerchantid());
		sParaTemp.put("ShopAcct", payment.getQuerykey());
		sParaTemp.put("QrySerialNo", "");
		sParaTemp.put("QryOrderNum", "");
		sParaTemp.put("BeginDate", dateFile);// 对账，前一天的
		sParaTemp.put("EndDate", dateFile);// 对账，前一天的
		sParaTemp.put("BeginTime", "");
		sParaTemp.put("EndTime", "");
		sParaTemp.put("ResultType", "010");// 成功
		sParaTemp.put("NextTag", "");
		sParaTemp.put("ReqReserved1", "");
		sParaTemp.put("ReqReserved2", "");
		
		List<Check> payCheck = ICBCPaySubmit.checkSocketEBSSB2B(sParaTemp, payment,"1");
		
		// 退货查询
		Map<String, String> sParaRefundTemp = new HashMap<String, String>();
		payment.setExt7("B2BEJEINF");
		sParaRefundTemp.put("TransCode", payment.getExt7());
		sParaRefundTemp.put("CIS",payment.getExt6());
		sParaRefundTemp.put("BankCode", payment.getExt5());
		sParaRefundTemp.put("ID", payment.getExt4());
		sParaRefundTemp.put("TranDate", sdf1.format(datenow));
		sParaRefundTemp.put("TranTime", sdf2.format(datenow)+"000000");
		sParaRefundTemp.put("fSeqno", ICBCPayUtil.getFSeqno());
		sParaRefundTemp.put("Ordertype", "0");//0：退货 1：返还 2：转付
		sParaRefundTemp.put("ShopType", "1");///1：B2B商城  2：B2C商城 3：C2C商城
		sParaRefundTemp.put("ShopCode",payment.getMerchantid());
		sParaRefundTemp.put("ShopAcct", payment.getQuerykey());
		sParaRefundTemp.put("QrySerialNo", "");
		sParaRefundTemp.put("QryOrderNum", "");
		sParaRefundTemp.put("BeginDate", dateFile);
		sParaRefundTemp.put("EndDate", dateFile);
		sParaRefundTemp.put("BeginTime", "");
		sParaRefundTemp.put("EndTime", "");
		sParaRefundTemp.put("ResultType", "010");
		sParaRefundTemp.put("NextTag", "");
		sParaRefundTemp.put("ReqReserved1", "");
		sParaRefundTemp.put("ReqReserved2", "");
		
		//退货查询
		List<Check> backCheck = ICBCPaySubmit.checkSocketEBSSB2B(sParaRefundTemp, payment,"2");
		
		try {
			
			if(payCheck!=null&&payCheck.size()>0){
				for(int i =0;i<payCheck.size();i++){
					Check check = payCheck.get(i);
					OrderFormPay orderFormPay = payService.getOrderFormByOrderNumPay(check.getOrdernumber());
					if (orderFormPay != null) {
						if(check.getPrice().contains("-")) check.setCorderid(orderFormPay.getMemo());//如果是退款金额，则取退款的应收单
						else 	check.setCorderid(orderFormPay.getCorderid());//如果取得数据为付款金额，则取原账单
					}
					check.setPaytype(payTypeId+"");
					resultcheck.add(check);
				}
			}
			if(backCheck!=null&&backCheck.size()>0){
				for(int i =0;i<backCheck.size();i++){
					Check check = backCheck.get(i);
					OrderFormPay orderFormPay = payService.getOrderFormByOrderNumPay(check.getOrdernumber());
					if (orderFormPay != null) {
						if(check.getPrice().contains("-")) check.setCorderid(orderFormPay.getMemo());//如果是退款金额，则取退款的应收单
						else 	check.setCorderid(orderFormPay.getCorderid());//如果取得数据为付款金额，则取原账单
					}
					check.setPaytype(payTypeId+"");
					resultcheck.add(check);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
		
		return resultcheck;
	}

	@Override
	public OrderFormUnPay getOrderFormUnPay(HttpServletRequest request,
			HttpServletResponse response) {
		// TODO Auto-generated method stub
		return null;
	}

}
