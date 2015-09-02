package com.pay.framework.mq.consumer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pay.framework.mq.Consumer;
import com.pay.framework.mq.message.ServerNotifyMessage;
import com.pay.framework.service.PayService;

@Component
public class ServerNotifyConsumer implements Consumer<ServerNotifyMessage> {
	private Log logger = LogFactory.getLog(this.getClass());
	@Autowired
	protected PayService payService;
	@Override
	public void consume(ServerNotifyMessage m) {
		try {
			payService.doServerNotify(m.getForm()) ;
		} catch (Exception e) {
			logger.error(e.getMessage()) ;
		}
	}

}
