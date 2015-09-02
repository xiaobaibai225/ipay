package com.pay.framework.mq;

import com.pay.framework.mq.consumer.ServerNotifyConsumer;

/**
 * 消息类型
 * 
 * @author PCCW
 * @version 2013-08-20
 */
public enum MessageType {

	/**
	 * 通知院线开通服务消息
	 */
	SEVER_NOTIFY_MESSAGE(ServerNotifyConsumer.class);
	private Class<?> clazz;

	private MessageType(Class<?> clazz) {
		this.clazz = clazz;
	}

	public Class<?> getConsumerClass() {
		return this.clazz;
	}
}
