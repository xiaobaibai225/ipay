package com.pay.framework.service.impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pay.framework.dao.PayTypeDao;
import com.pay.framework.service.PayTypeService;
import com.pay.model.PaymentBean;

@Service
public class PayTypeServiceImpl implements PayTypeService{
	
	@Autowired
	private PayTypeDao dao;
	
	
	@Override
	public PaymentBean getPayType(int typeId) {
		return dao.getPayType(typeId);
	}

	@Override
	public String getBeanName(int typeId) {
		PaymentBean bean = this.getPayType(typeId);
		return bean.getBean();
	}
}
