package com.pay.framework.util;

/*
 * 用于针对oracle的分页语句的处理
 * User:Gezehao	
 * Date:2013.07.30
 * 
 */
public class SqlHelper {

	private static final int PAGE_DEFAULT = 1; // 默认展示第一页

	private static final int ROWS_DEFAULT = 10; // 默认展示10行

	/**
	 * 
	 * 注意这个方法一定是要在sql语句拼接的最后使用
	 * 
	 */
	public static String createPageOracleSql(String sql, int page, int rows) {
		sql = "SELECT * FROM ( SELECT B.* , ROWNUM RN FROM (" + sql + ") B WHERE ROWNUM <= " + ((page - 1) * rows + rows) + " ) WHERE RN > "
				+ (page - 1) * rows + "";

		return sql;
	}

	
	public static String createPageSql(String sql, PageHelper helper) {
		int page = helper.getPage();
		int rows = helper.getRows();
		// 默认展示10行，一页
		if (rows <= 0) {
			rows = ROWS_DEFAULT;
		}
		if (page <= 0) {
			page = PAGE_DEFAULT;
		}
		if (rows > 0 && page > -1) {
			sql = createPageOracleSql(sql, page, rows);
		}
		return sql;
	}

}
