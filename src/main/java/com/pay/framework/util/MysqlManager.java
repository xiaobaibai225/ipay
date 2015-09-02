package com.pay.framework.util;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 由于Spring 有 JdbcTemplete，所以这个类暂时没有用
 * @author houzhaowei
 */
public class MysqlManager {

	private static MysqlManager mysqlManager;
	
	@Autowired
	private static BasicDataSource dataSource;
	

	private MysqlManager(){
		
	}
	
	public static MysqlManager getInstance(){
		if (null == mysqlManager ){
			return new MysqlManager();
		}
		return mysqlManager;
	}
	
	public List<Map<Integer,Object>> executeQuery(String sql){
		List<Map<Integer,Object>> resultList = new ArrayList<Map<Integer,Object>>();
		
		try {
			Connection conn = dataSource.getConnection();
			Statement stmt = conn.createStatement();
			ResultSet res =  stmt.executeQuery(sql);
			int index = 0;
			while(res.next()){
				index ++;
				Map<Integer,Object> map = new HashMap<Integer,Object>();
				map.put(index, res.getObject(index));
				resultList.add(map);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return resultList;
	}
	
	public int executeUpdate(String sql){
		try {
			Connection conn = dataSource.getConnection();
			Statement stmt = conn.createStatement();
			return stmt.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
}
