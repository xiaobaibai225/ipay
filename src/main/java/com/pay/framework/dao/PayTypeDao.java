package com.pay.framework.dao;

import java.util.List;

import org.springframework.stereotype.Component;

import com.pay.model.PaymentBean;

@Component
public interface PayTypeDao {

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
	
	/**
	 * 获取所有的paytype
	 * 在web初始化的时候初始化缓存
	 * @return
	 */
	List<PaymentBean> getPayTypes();
	
	/**
	 * @author wangxin5 
	 * 根据companyId，ext_col2扩展字段取支付方式
	 */
	List<PaymentBean> getPayTypeBycompanyIdExt_col2(int companyId , String ext_col2);

	List getPayStatusList(int companyid, String institution, String platform,
			String available, String payMethod);

	
}
