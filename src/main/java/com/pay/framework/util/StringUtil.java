package com.pay.framework.util;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

/**
 * 继承自 {@link StringUtils} 并且添加了常用的 String 、 String数组的操作
 * 
 * @author houzhaowei
 * 
 */
public class StringUtil extends StringUtils {

	/**
	 * 数组中是否包含target
	 * 
	 * @param array
	 * @param target
	 * @return 包含返回true，不包含返回false
	 */
	public static boolean contains(String[] array, String target) {
		boolean contains = false;
		for (String one : array) {
			if (one.equals(target)) {
				contains = true;
				break;
			}
		}
		return contains;
	}

	/**
	 * 数组中是否有某个元素包含 target
	 * 
	 * @param array
	 * @param target
	 * @return 数组中某元素包含target则返回true，否则返回false
	 */
	public static boolean containsItemContains(String[] array, String target) {
		boolean contains = false;
		for (String one : array) {
			if (target.indexOf(one) != -1) {
				contains = true;
			}
		}
		return contains;
	}

	public static boolean isDate(String value, String format) {

		SimpleDateFormat sdf = null;
		ParsePosition pos = new ParsePosition(0);// 指定从所传字符串的首位开始解析

		if (value == null || isEmpty(format)) {
			return false;
		}
		try {
			sdf = new SimpleDateFormat(format);
			sdf.setLenient(false);
			Date date = sdf.parse(value, pos);
			if (date == null) {
				return false;
			} else {
				if (pos.getIndex() > format.trim().length()) {
					return false;
				}
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static void main(String[] args) {
		System.out.println(isDate("2011-03-24", "yyyy-MM-dd"));
	}
}
