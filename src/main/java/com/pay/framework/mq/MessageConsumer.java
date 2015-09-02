package com.pay.framework.mq;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 消息消费者
 * @author PCCW
 * @version 2013-08-20
 */
@Component
public class MessageConsumer {
	private static final Log logger = LogFactory.getLog(MessageConsumer.class);

    private static volatile boolean inited = false;

    private static Object initLock = new Object();

    @Autowired
    private ConsumerFactory consumerFactory;

    @Autowired
    private MessageQueue messageQueue;

    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(Runtime.getRuntime()
            .availableProcessors() * 2, new MessageConsumerThreadFactory());

    private static AtomicInteger noMessageCount = new AtomicInteger();

    public void startConsume() {
        //加上这个之后多线程访问时不需要等待，因为初始化只有一次，以后再也不需要等了  
        //如果不加这个那么多个线程就要排队等待这个判断
        if (inited) {
            return;
        }
        synchronized (initLock) {
            if (inited) {
                return;
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("consumer going to init!!!!!!!!!!!!!");
        }

        for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
            scheduler.scheduleAtFixedRate(new Runnable() {

                @Override
                public void run() {
                    while (true) {
                        try {
                            Message message = messageQueue.getMessage();
                            if (message == null) {
                                //超过1000次没有消息进来，就1秒钟一次
                                if (noMessageCount.incrementAndGet() > 1000) {
                                    Thread.sleep(1000);
                                } else {
                                    Thread.sleep(100);
                                }
                                continue;
                            }

                            noMessageCount.set(0);
                            consumerFactory.getMessageType(message.getMessageType())
                                    .consume(message);
                        } catch (Exception e) {
                            logger.error("execute error", e);
                        }
                        //continue;
                    }
                }
            }, 1, 1, TimeUnit.SECONDS);
        }
        inited = true;
    }

    private static class MessageConsumerThreadFactory implements ThreadFactory {

        private final Logger logger = Logger.getLogger(MessageConsumerThreadFactory.class);

        static final AtomicInteger poolNumber = new AtomicInteger(1);

        final ThreadGroup group;

        final AtomicInteger threadNumber = new AtomicInteger(1);

        final String namePrefix;

        MessageConsumerThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = "MessageConsumer-" + poolNumber.getAndIncrement() + "-thread-";
        }

        public Thread newThread(Runnable r) {
            if (threadNumber.get() % 100 == 0) {
                logger.info(String
                        .format("Message consumer executor new Thead now:  Pool Number: %d, Thread Number: %d",
                                poolNumber.get(), threadNumber.get()));
            }

            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }
}
