package com.pay.framework.xml;

/**
 * 明确一个正确的xml 类型，一个xml类型必须是一个 {@link XmlObject}
 * @author houzhaowei
 *
 */
public interface Xml {
	/**
	 * 是否为空
	 * @return 是否为空
	 */
	boolean isEmpty();
	/**
	 * 长度
	 * @return 长度
	 */
	int size();
}
