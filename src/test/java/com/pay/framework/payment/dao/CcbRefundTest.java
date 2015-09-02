package com.pay.framework.payment.dao;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.pay.framework.util.XmlUtil;

public class CcbRefundTest {

	private InetSocketAddress inetSocketAddress;
	
	public CcbRefundTest(String hostname, int port) {
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
		sbf.append("<?xml version='1.0' encoding='GB2312' standalone='yes' ?>");
		sbf.append("<TX>");
		sbf.append("<REQUEST_SN>11211099789249</REQUEST_SN>");
		sbf.append("<CUST_ID>105110073992025</CUST_ID>");
		sbf.append("<USER_ID>110000410166533-001</USER_ID>");
		sbf.append("<PASSWORD>111111</PASSWORD>");
		sbf.append("<TX_CODE>5W1004</TX_CODE>");
		sbf.append("<LANGUAGE>01</LANGUAGE>");
	    sbf.append("<TX_INFO>");
		sbf.append("<MONEY>0.01</MONEY>");
		sbf.append("<ORDER>20150108104050789049</ORDER>");
		sbf.append("</TX_INFO>");
		sbf.append("<SIGN_INFO></SIGN_INFO>");
		sbf.append("<SIGNCERT></SIGNCERT>");
		sbf.append("</TX>");
		int port = 12345;
		new CcbRefundTest(hostname, port).send(sbf.toString());
	}
	
	
}
