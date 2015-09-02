package com.pay.framework.mq;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.pay.framework.util.spring.SpringContextManager;

/**
 * 消费工厂
 * @author PCCW
 * @version 2013-08-20
 */
@Component
public class ConsumerFactory {

	private static Map<MessageType, Consumer<Message>> consumers = new ConcurrentHashMap<MessageType, Consumer<Message>>();
    @SuppressWarnings("unchecked")
    public Consumer<Message> getMessageType(MessageType type) {
        if (!consumers.containsKey(type)) {
            synchronized (type) {
                if (!consumers.containsKey(type)) {
                	ApplicationContext context = SpringContextManager.getApplicationContext();
                    consumers.put(type, (Consumer<Message>) context.getBean(type.getConsumerClass()));
                }
            }
        }
        return consumers.get(type);
    }

}
