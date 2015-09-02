package com.pay.framework.util;

/**
 * ip���
 * @author zhangyouxing
 *
 */
public class IPCheck {

	private static long[] mask_array = { 0x0, 0x80000000, 0xc0000000,
			0xe0000000, 0xf0000000, 0xf8000000, 0xfc000000, 0xfe000000,
			0xff000000, 0xff800000, 0xffc00000, 0xffe00000, 0xfff00000,
			0xfff80000, 0xfffc0000, 0xfffe0000, 0xffff0000, 0xffff8000,
			0xffffc000, 0xffffe000, 0xfffff000, 0xfffff800, 0xfffffc00,
			0xfffffe00, 0xffffff00, 0xffffff80, 0xffffffc0, 0xffffffe0,
			0xfffffff0, 0xfffffff8, 0xfffffffc, 0xfffffffe, 0xffffffff };

	/**
	 * �ж�ip��ip_range�Ƿ�����ͬһ�����
	 * 
	 * @param ip
	 *            ���ʽ��ʾ����210.0.217.179
	 * @param ip_range
	 *            ����α�ʾ������210.0.128.0/17
	 * @return �Ƿ�ͬ���
	 */
	public static boolean isSameNetWork(String ip, String ip_range) {
		String[] ip_range2Array = ip_range.split("/");
		if (ip_range2Array.length != 2) {
			return false;
		}
		int mask = Integer.parseInt(ip_range2Array[1]);
		if (mask < 0 || mask > 32) {
			return false;
		}
		if (mask == 32) {
			return ip.equals(ip_range2Array[0]);
		}
		return (convert2Long(ip) & mask_array[mask]) == (convert2Long(ip_range2Array[0]));
	}

	public static long convert2Long(String ip) {
		long nIP = 0;
		String[] seg = ip.split("\\.");
		if (seg.length != 4)
			return -1;
		nIP = (Long.valueOf(seg[0].trim()).longValue() << 24)
				| (Long.valueOf(seg[1].trim()).longValue() << 16)
				| (Long.valueOf(seg[2].trim()).longValue() << 8)
				| (Long.valueOf(seg[3].trim()).longValue() << 0);
		return nIP;
	}

}