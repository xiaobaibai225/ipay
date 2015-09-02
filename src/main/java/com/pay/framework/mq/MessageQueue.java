package com.pay.framework.mq;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 消息队列
 * 
 * @author PCCW
 * @version 2013-08-20
 */
@Component
public class MessageQueue {
	private static final Log logger = LogFactory.getLog(MessageQueue.class);

	@Autowired
	private MessageConsumer shareMessageConsumer;
	private final static ConcurrentLinkedQueue<Message> messageQueue = new ConcurrentLinkedQueue<Message>();

	private AtomicLong count = new AtomicLong(0);

	public void putMessage(Message message) {
		shareMessageConsumer.startConsume();
		if (message != null) {
			messageQueue.offer(message);
			long c = count.incrementAndGet();
			if (c > 1000 && c % 5 == 0) {
				logger.warn(String.format("The size of MessageQueue is %d now!", c));
			}
		}
	}

	public Message getMessage() {
		count.decrementAndGet();
		Message message = messageQueue.poll();
		return message;
	}
}
