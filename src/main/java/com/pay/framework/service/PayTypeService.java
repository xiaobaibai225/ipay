package com.pay.framework.service;

import com.pay.model.PaymentBean;

public interface PayTypeService {

	/**
	 * 获取支付类型信息
	 * @return
	 */
	PaymentBean getPayType(int typeId);
	
	/**
	 * 根据类型id获取对应bean对象的名称
	 * @param typeId
	 * @return bean 对象名称
	 */
	String getBeanName(int typeId);
	
}
