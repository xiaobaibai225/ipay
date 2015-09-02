package com.pay.framework.mq;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 消费接口
 * @author PCCW
 *
 * @param <M>
 */
public interface Consumer<M> {

    Log logger = LogFactory.getLog(Consumer.class);

    public void consume(M m);
}
