package com.pay.framework.mq.message;

import com.pay.framework.mq.Message;
import com.pay.framework.mq.MessageType;
import com.pay.model.OrderFormPay;

/**
 * 院线消费通知消息
 * 
 * @author PCCW
 * @version 2013-08-20
 */
public class ServerNotifyMessage implements Message {

	/**
	 * 支付成功订单记录
	 */
	private OrderFormPay form;

	public OrderFormPay getForm() {
		return form;
	}

	public void setForm(OrderFormPay form) {
		this.form = form;
	}

	@Override
	public MessageType getMessageType() {
		return MessageType.SEVER_NOTIFY_MESSAGE;
	}

	public ServerNotifyMessage(OrderFormPay form) {
		this.form = form;
	}
}
