package com.pay.framework.payment.dao;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.pay.framework.dao.OrderFormPayDao;
import com.pay.framework.util.DateUtil;
import com.pay.model.OrderFormPay;
import com.pay.model.OrderFormUnPay;
import com.pay.model.PayStatus;

public class PageQueryTest {
	ApplicationContext context;

	@Before
	public void setUp() {
		context = new ClassPathXmlApplicationContext("classpath:*.xml");
	}

	@Test
	public void testUnPay2Pay() {
		OrderFormPayDao formPayDao = context.getBean(OrderFormPayDao.class);
		OrderFormUnPay orderFormUnpay = formPayDao
				.queryOrderFormUnPaymentByOrderNumber("20141128221317605010");
		OrderFormPay formpay = new OrderFormPay();
		try {
			BeanUtils.copyProperties(formpay, orderFormUnpay);
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		}
		formpay.setStatus(String.valueOf(PayStatus.PAY_SUCCESS_NOT_XIAOFEI));
		formpay.setPaymentdate(DateUtil.format(new Date(),
				DateUtil.C_TIME_PATTON_DEFAULT));
		formpay.setTranseq("2341234234234234");
		// 增加到pay记录
		formPayDao.addPay(formpay);
		// 删除unpay
		formPayDao.delUnpay(orderFormUnpay.getOrdernumber());
	}

}
