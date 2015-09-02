package com.pay.framework.payment;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.pay.framework.service.PayTypeService;
import com.pay.framework.util.spring.SpringContextManager;

@Component
public class PayFactory {
	
	public static AutoPay getAutoPayInstance(int typeId){
		AutoPay pay = null;
		//获取applicationContext
		ApplicationContext context = SpringContextManager.getApplicationContext();
		PayTypeService payTypeService = context.getBean(PayTypeService.class);
		//获取对应的pay实例
		String beanName = payTypeService.getBeanName(typeId);
		pay = (AutoPay)context.getBean(beanName);
		return pay;
	}
	
	public static IPay getPayInstance(int typeId){
		IPay pay = null;
		//获取applicationContext
		ApplicationContext context = SpringContextManager.getApplicationContext();
		PayTypeService payTypeService = context.getBean(PayTypeService.class);
		//获取对应的pay实例
		String beanName = payTypeService.getBeanName(typeId);
		pay = (IPay)context.getBean(beanName);
		return pay;
	}
	
	public static IMobilePay getMobilePayInstance(int typeId){
		IMobilePay pay = null;
		//获取applicationContext
		ApplicationContext context = SpringContextManager.getApplicationContext();
		PayTypeService payTypeService = context.getBean(PayTypeService.class);
		//获取对应的pay实例
		String beanName = payTypeService.getBeanName(typeId);
		pay = (IMobilePay)context.getBean(beanName);
		return pay;
	}
	
	public static ICodePay getCodePayInstance(int typeId){
		ICodePay pay = null;
		//获取applicationContext
		ApplicationContext context = SpringContextManager.getApplicationContext();
		PayTypeService payTypeService = context.getBean(PayTypeService.class);
		//获取对应的pay实例
		String beanName = payTypeService.getBeanName(typeId);
		pay = (ICodePay)context.getBean(beanName);
		return pay;
	}
	
	public static IMisPosPay getMisPosPayInstance(int typeId){
		IMisPosPay pay = null;
		//获取applicationContext
		ApplicationContext context = SpringContextManager.getApplicationContext();
		PayTypeService payTypeService = context.getBean(PayTypeService.class);
		//获取对应的pay实例
		String beanName = payTypeService.getBeanName(typeId);
		pay = (IMisPosPay)context.getBean(beanName);
		return pay;
	}
	
}
