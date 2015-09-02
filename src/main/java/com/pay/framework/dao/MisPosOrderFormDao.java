package com.pay.framework.dao;

import java.util.List;

import com.pay.model.MisPosOrderFormPay;
import com.pay.model.MisPosOrderFormUnPay;

/**
 * 创建mispos已经支付的dao
 * 
 * @author PCCW
 * 
 */
public interface MisPosOrderFormDao {

	/**
	 * 添加mispos账单
	 * 
	 * @param misPosOrderFormPay
	 * @return
	 */
	int addPay(MisPosOrderFormPay misPosOrderFormPay);
	
	int addUnPay(MisPosOrderFormUnPay misPosOrderFormUnPay);
	
	/**
	 * 通过ordernumber获取已经支付的订单，
	 * 
	 * @param ordernumber
	 * @return
	 */
	 List<MisPosOrderFormPay> queryMisPosOrderFormPayByOrderNumber(String ordernumber);
	
	 
	/**
	 * 通过corderid获取已经支付的订单，
	 * 
	 * @param corderid
	 * @return
	 */
	 List<MisPosOrderFormPay> queryMisPosOrderFormPayByCorderid(String corderid, int typeId);
		
	 List<MisPosOrderFormPay> queryMisPosOrderFormPayRefundByCorderid(String corderid, int typeId);
	 
	/**
	 * 获取mispos支付记录
	 * @param hostTrace 交易系统检索号 
	 * @param terminalNum 终端编号
	 * @param authorNum 授权号
	 * @return
	 */
	 MisPosOrderFormPay getMisPosOrderFormByOth(String hostTrace,String terminalNum, String authorNum);

	 /**
	  * 根据主键id查找支付记录
	  * @param id
	  * @return
	  */
	 MisPosOrderFormUnPay findFormUnPayById(String id);


	 int delUnPayById(String id);

	 MisPosOrderFormPay findFormPayById(String id);

	MisPosOrderFormPay getMisPosOrderFormByExt(String ext);

	void updateByCorderId(String ordernumber, String corderid);


}
