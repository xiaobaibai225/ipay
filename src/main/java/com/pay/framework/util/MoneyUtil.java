package com.pay.framework.util;

import java.math.BigDecimal;

import com.pay.framework.log.LogManager;

/**
 * 将money 乘以一定的比率，并精确到小数点后N位 将money传给银行接口时候使用
 * 
 * @author PCCW
 * 
 */
public class MoneyUtil {

	private static final int DEFAULT_RATE = 100;// 默认乘以100

	private static final int DEFAULT_DIGIT = 0;// 默认精确到小数点后0位

	private static LogManager log = LogManager.getLogger(MoneyUtil.class);

	public static java.text.DecimalFormat df = new java.text.DecimalFormat("#.#");

	/**
	 * 转换钱成指定形式
	 * 
	 * @param moneyBefore
	 *            转换前的钱
	 * @param rate
	 *            比率 (moneyBefore*rate=result)
	 * @param type
	 *            转换形式（默认为BigDecimal.ROUND_HALF_UP 即四舍五入）
	 * @param digit
	 *            精确到小数点后几位
	 * @return
	 */
	public static String getPayMoney(String moneyBefore, int rate, int type, int digit) {
		if (null != moneyBefore && !moneyBefore.equals("")) {
			return String.valueOf((new BigDecimal(moneyBefore).multiply(BigDecimal.valueOf(rate))).setScale(digit, type));
		}
		return null;
	}

	/**
	 * 转换钱成指定形式
	 * 
	 * @param moneyBefore
	 *            转换前的钱
	 * @param rate
	 *            比率 (moneyBefore*rate=result)
	 * @param type
	 *            转换形式（默认为BigDecimal.ROUND_HALF_UP 即四舍五入）
	 * @return
	 */
	public static String getPayMoney(String moneyBefore, int rate, int type) {
		return getPayMoney(moneyBefore, rate, type, DEFAULT_DIGIT);
	}

	/**
	 * 转换钱成指定形式
	 * 
	 * @param moneyBefore
	 *            转换前的钱
	 * @param rate
	 *            比率 (moneyBefore*rate=result)
	 * @return
	 */
	public static String getPayMoney(String moneyBefore, int rate) {
		return getPayMoney(moneyBefore, rate, BigDecimal.ROUND_HALF_UP);
	}

	/**
	 * 转换钱成指定形式
	 * 
	 * @param moneyBefore
	 *            转换前的钱
	 * @return
	 */
	public static String getPayMoney(String moneyBefore) {
		return getPayMoney(moneyBefore, DEFAULT_RATE);
	}

}
