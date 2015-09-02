package com.pay.framework.util;

import org.codehaus.jackson.map.ObjectMapper;

/**
 * JSON与BEAN转换相关
 * @author PCCW
 * @version 2013-07-22
 */
public class Common4JsonUtil {
	private static ObjectMapper mapper;

	/**
	 * 一个破ObjectMapper而已，你为什么不直接new 还搞的那么复杂。接下来的几篇文章我将和你一起研究这个令人蛋疼的问题
	 * 
	 * @param createNew
	 *            是否创建一个新的Mapper
	 * @return
	 */
	public static synchronized ObjectMapper getMapperInstance(boolean createNew) {
		if (createNew) {
			return new ObjectMapper();
		} else if (mapper == null) {
			mapper = new ObjectMapper();
		}
		return mapper;
	}
}
