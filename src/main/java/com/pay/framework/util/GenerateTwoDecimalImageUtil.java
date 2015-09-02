package com.pay.framework.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;

import javax.servlet.http.HttpServletResponse;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;


public class GenerateTwoDecimalImageUtil {
	/**
	 * 
	 * @param content 二维码图片的内容
	 * @param width   二维码图片的宽度
	 * @param height  二维码图片的高度
	 * @param fomat   二维码图片的格式
	 * @param response
	 */
 @SuppressWarnings("all")
public static void generateTwoDecialImage(String content,int width,int height,String format,HttpServletResponse response){
	/* String text = "weixin://wxpay/bizpayurl?appid=wx932e01b841abbb54&timestamp=1414144542&noncestr=79734228&productid=20141024175542729052&sign=3da36a6960ff3ff06b9a7a5e1e7f5e56263adfa5"; 
     int width = 300; 
     int height = 300; 
     //二维码的图片格式 
     String format = "png"; */
	 Hashtable hints = new Hashtable(); 
     //内容所使用编码 
     hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
     //设置边距
     hints.put(EncodeHintType.MARGIN, 0);
     BitMatrix bitMatrix=null;
		try {
			bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
		} catch (WriterException e) {
			e.printStackTrace();
		} 
     //生成二维码 
     OutputStream out;
	try {
		out = response.getOutputStream();
		MatrixToImageWriter.writeToStream(bitMatrix, format, out);
	    out.flush();
	    out.close();
	} catch (IOException e) {
		e.printStackTrace();
	}
     
 }
	
	public static void main(String[] args) {

	}

}
