package com.pay.framework.log;

import org.apache.log4j.Logger;


/**
 * 
 * 工程通用logger，管理log的打印
 * LOG_PATTEN 属性定义log打印的格式 
 * 此类只用来做本地log打印
 * @author PCCW
 */
public class LogManager {
	

	private  Logger logger;
	
	private static final String LOG_PATTERN = "FLAG : %s , MESSAGE: %s";
	//错误日志要打印出类的位置
	private static final String LOG_PATTERN_ERR = "FLAG : %s , LOC : %s , MESSAGE : %s";
	
	private LogManager(Class<? extends Object> clz) {
		logger = Logger.getLogger(clz);
	}
	
	/**
	 * 获取manager 单例
	 * @param name, XXX.getClass().getName();
	 * @return manager实例
	 */
	public static LogManager getLogger(Class<? extends Object> clz){
		return new LogManager(clz);
	}
	
	/**
	 * INFO
	 * @param flag 日志的标识，建议为每一种类型的log定义一个标识，没有可传NULL
	 * @param message 相关逻辑信息或错误信息
	 */
	public void info(String flag,String message){
		print(LogType.INFO,flag,message);
	}
	
	/**
	 * DEBUG
	 * @param flag 日志的标识，建议为每一种类型的log定义一个标识，没有可传NULL
	 * @param message 相关逻辑信息或错误信息
	 */
	public void debug(String flag,String message){ 
		print(LogType.DEBUG,flag,message);
	}
	
	/**
	 * ERROR
	 * @param flag 日志的标识，建议为每一种类型的log定义一个标识，没有可传NULL
	 * @param message 相关逻辑信息或错误信息
	 */
	public void error(String flag,String message){
		print(LogType.ERROR,flag,message);
	}
	/**
	 * 重构ERROR日志输出
	 * @param flag
	 * @param message
	 * @param exception
	 */
	public void error(String flag,String message,Exception exception){
		logger.error(flag+message, exception) ;
	}
	
	private void print(LogType type,String flag,String message){
		 {
			switch (type) {
			case DEBUG:
				if(logger.isDebugEnabled())logger.debug(String.format(LOG_PATTERN, flag == null ? "" : flag ,message));
				break;
			case INFO:
				if(logger.isInfoEnabled())
				logger.info(String.format(LOG_PATTERN, flag == null ? "" : flag ,message));
				break;
			case ERROR:
				logger.error(String.format(LOG_PATTERN_ERR, flag == null ? "" : flag , getBaseInfo() ,message));
				break;
			default:
				break;
			}
		}
	}
	
	/**
	 * 获取类，方法的信息
	 * @return 结合的信息
	 */
	private String getBaseInfo() {
		String classname = "";
		String method = "";
		int linenumber = -1;
		try {
			StackTraceElement stack = Thread.currentThread().getStackTrace()[4];
			linenumber = stack.getLineNumber();
			String classnames[] = stack.getClassName().split("\\.");
			int max = classnames.length - 1;
			classname = classnames[max];
			if(classname != null && classname.indexOf('$')>=0){
			    try {
                    classname = classname.split("$")[0];
                } catch (Exception e) {
                    // TODO: handle exception
                }
			}
			if(classname == null){
				classname = "";
			}
			method = stack.getMethodName();
			
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
		return classname+"::"+method+"("+linenumber+"):";
	}
}
