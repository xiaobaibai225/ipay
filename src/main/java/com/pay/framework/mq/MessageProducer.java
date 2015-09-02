package com.pay.framework.mq;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.pay.framework.mq.message.ServerNotifyMessage;
import com.pay.framework.util.JsonUtil;
import com.pay.model.OrderFormPay;

/**
 * 消息生产者
 * @author PCCW
 * @version 2013-08-20
 */
public class MessageProducer {
	private static final Log logger = LogFactory.getLog(MessageProducer.class);
	
	/**
     * 产生评论同步消息
     * 
     * @param gid
     * @param size
     * @return
	 * @throws IOException 
     */
    public static ServerNotifyMessage productServerNotifyMessage(OrderFormPay formPay) throws IOException {
        if (logger.isInfoEnabled()) {
            logger.info(String.format("productServerNotifyMessage params[%s]", JsonUtil.beanToJson(formPay)));
        }
        return new ServerNotifyMessage(formPay);
    }
}
