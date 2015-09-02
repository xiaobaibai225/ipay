package com.pay.framework.payment.dao;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.pay.framework.util.XmlUtil;

public class CcbCheckTest {

private InetSocketAddress inetSocketAddress;
	
	public CcbCheckTest(String hostname, int port) {
		inetSocketAddress = new InetSocketAddress(hostname, port);
	}
	public void send(String requestData){
		try {
			SocketChannel socketChannel = SocketChannel.open(inetSocketAddress);
			socketChannel.configureBlocking(false);
			ByteBuffer byteBuffer = ByteBuffer.allocate(2048);
			socketChannel.write(ByteBuffer.wrap(requestData.getBytes()));
			String result;
			while (true) {
				byteBuffer.clear();
				int readBytes = socketChannel.read(byteBuffer);
				if (readBytes > 0) {
					byteBuffer.flip();
					result= new String(byteBuffer.array(), 0, readBytes);
					System.out.println(result);
					socketChannel.close();
					break;
				}
			}
			System.out.println(result);
			
			System.out.println("return code is:"+XmlUtil.getContentByKeyOnly(result, "//TX/RETURN_CODE"));

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public static void main(String[] args) {
		String hostname = "192.168.163.4";
		StringBuffer sbf = new StringBuffer("");

//		sbf.append("<?xml version='1.0' encoding='GB2312' standalone='yes' ?>");
//		sbf.append("<TX>");
//		sbf.append("<REQUEST_SN>0110381149</REQUEST_SN>");
//		sbf.append("<CUST_ID>105110073992025</CUST_ID>");
//		sbf.append("<USER_ID>110000410166533-001</USER_ID>");
//		sbf.append("<PASSWORD>111111</PASSWORD>");
//		sbf.append("<TX_CODE>5W1005</TX_CODE>");
//		sbf.append("<LANGUAGE>01</LANGUAGE>");
//	    sbf.append("<TX_INFO>");
//		sbf.append("<DATE>20150105</DATE>");
//		sbf.append("<KIND>1</KIND>");
//		sbf.append("<FILETYPE>1</FILETYPE>");
//		sbf.append("<TYPE>1</TYPE>");//1 退款0支付
//		sbf.append("<NORDERBY>1</NORDERBY>");
//		sbf.append("<POS_CODE></POS_CODE>");
//		sbf.append("<ORDER></ORDER>");
//		sbf.append("<STATUS>1</STATUS>");
//		sbf.append("</TX_INFO>");
//		sbf.append("</TX>");
		int port = 12345;
//		new CcbCheckTest(hostname, port).send(sbf.toString());
		StringBuffer dbf2 = new StringBuffer();
		dbf2.append("<?xml version='1.0' encoding='GB2312' standalone='yes' ?>");
		dbf2.append("<TX>");
		dbf2.append("<REQUEST_SN>0108125149</REQUEST_SN>");
		dbf2.append("<CUST_ID>105110073992025</CUST_ID>");
		dbf2.append("<USER_ID>110000410166533-001</USER_ID>");
		dbf2.append("<PASSWORD>111111</PASSWORD>");
		dbf2.append("<TX_CODE>6W0111</TX_CODE>");
		dbf2.append("<LANGUAGE>01</LANGUAGE>");
		dbf2.append("<TX_INFO>");
		dbf2.append("<SOURCE>SHOP.105110073992025.20150105.04.success.txt.gz</SOURCE>");
		dbf2.append("<FILEPATH>merchant/shls</FILEPATH>");
		dbf2.append("<LOCAL_REMOTE>0</LOCAL_REMOTE>");
		dbf2.append("</TX_INFO>");
		dbf2.append("</TX>");
		new CcbCheckTest(hostname, port).send(dbf2.toString());

	}
	
}
