package com.pay.framework.service;

import java.util.List;

import com.pay.model.MisPosOrderFormPay;
import com.pay.model.MisPosOrderFormUnPay;

/**
 * 订单service
 * @author PCCW
 *
 */
public interface MisPosOrderFormService {
	
	
	/**
	 * 获取mispos支付记录
	 * @param hostTrace 交易系统检索号 
	 * @param terminalNum 终端编号
	 * @param authorNum 授权号
	 * @return
	 */
	MisPosOrderFormPay getMisPosOrderFormByOth(String hostTrace,String terminalNum ,String authorNum);
	
	/**
	 * 根据订单编号获取mispos支付记录
	 * @param ordernumber 
	 * 
	 * @return
	 */
	List<MisPosOrderFormPay> getMisPosOrderFormPayByOrdernumber(String ordernumber);
	
	

	/**
	 * 根据订单号获取mispos支付记录
	 * @param ordernumber 
	 * 
	 * @return
	 */
	List<MisPosOrderFormPay> getMisPosOrderFormPayByCorderid(String corderid ,int pay_type_id);
	
	List<MisPosOrderFormPay> getMisPosOrderFormPayRefundByCorderid(String corderid,int pay_type_id);
	/**
	 * 增加一条mispos支付记录
	 * 
	 * @param misPosOrderFormPay
	 * @return
	 */
	public int addPay(MisPosOrderFormPay misPosOrderFormPay);

	
	/**
	 * 根据主键Id查找未支付记录
	 * 
	 * @param id
	 * @return
	 */
	MisPosOrderFormUnPay findFormUnPayById(String id);

	
	MisPosOrderFormPay findFormPayById(String id);
	/**
	 * 根据主键Id删除支付记录
	 * 
	 * @param id
	 * @return
	 */
	int delUnPayById(String id);
	/**
	 * 增加一条mispos支付訂單
	 * 
	 * @param misPosOrderFormPay
	 * @return
	 */
	int addUnPay(MisPosOrderFormUnPay misPosOrderFormUnPay);

	MisPosOrderFormPay getMisPosOrderFormPayByExt(String ext);

	void updateByCorderId(String ordernumber, String corderid);



	
}
