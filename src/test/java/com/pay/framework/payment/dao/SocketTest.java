package com.pay.framework.payment.dao;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.pay.framework.util.XmlUtil;

public class SocketTest {
	
private InetSocketAddress inetSocketAddress;
	
	public SocketTest(String hostname, int port) {
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
			System.out.println("return code is:"+XmlUtil.getContentByKeyOnly(result, "//TX/RETURN_CODE"));

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public static void main(String[] args) {
		String hostname = "192.168.163.4";
		StringBuffer sbf = new StringBuffer("");
		sbf.append("<?xml version='1.0' encoding='GB2312' standalone='yes' ?>");
		sbf.append("<TX>");
		sbf.append("<REQUEST_SN>0108104050789049</REQUEST_SN>");
		sbf.append("<CUST_ID>105110073992025</CUST_ID>");
		sbf.append("<USER_ID>110000410166533-001</USER_ID>");
		sbf.append("<PASSWORD>111111</PASSWORD>");
		sbf.append("<TX_CODE>5W1002</TX_CODE>");
		sbf.append("<LANGUAGE>01</LANGUAGE>");
	    sbf.append("<TX_INFO>");
//		sbf.append("<START></START>");
//		sbf.append("<STARTHOUR></STARTHOUR>");
//		sbf.append("<STARTMIN></STARTMIN>");
//		sbf.append("<END></END>");
//		sbf.append("<ENDHOUR></ENDHOUR>");
//		sbf.append("<ENDMIN></ENDMIN>");
		sbf.append("<KIND>1</KIND>");
		sbf.append("<ORDER>20150108104050789049</ORDER>");
//		sbf.append("<ACCOUNT></ACCOUNT>");
		sbf.append("<DEXCEL>1</DEXCEL>");
//		sbf.append("<MONEY></MONEY>");
		sbf.append("<NORDERBY>1</NORDERBY>");
		sbf.append("<PAGE>1</PAGE>");
		sbf.append("<POS_CODE>755183625</POS_CODE>");
		sbf.append("<STATUS>1</STATUS>");
		sbf.append("</TX_INFO>");
		sbf.append("</TX>");
		int port = 12345;
		new SocketTest(hostname, port).send(sbf.toString());
	}

}
