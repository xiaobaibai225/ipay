package com.pay.framework.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pay.framework.dao.MisPosOrderFormDao;
import com.pay.framework.service.MisPosOrderFormService;
import com.pay.model.MisPosOrderFormPay;
import com.pay.model.MisPosOrderFormUnPay;

@Component
public class MisPosOrderFormServiceImpl implements MisPosOrderFormService {

	@Autowired
	private MisPosOrderFormDao misPosOrderFormDao;

	@Override
	public MisPosOrderFormPay getMisPosOrderFormByOth(String hostTrace,
			String terminalNum, String authorNum) {
		
		return	misPosOrderFormDao.getMisPosOrderFormByOth(hostTrace, terminalNum, authorNum);
		
	}

	@Override
	public List<MisPosOrderFormPay> getMisPosOrderFormPayByOrdernumber(
			String ordernumber) {
		
		return	misPosOrderFormDao.queryMisPosOrderFormPayByOrderNumber(ordernumber);
	}

	@Override
	public List<MisPosOrderFormPay> getMisPosOrderFormPayByCorderid(
			String corderid,int pay_type_id) {
		
		return	misPosOrderFormDao.queryMisPosOrderFormPayByCorderid(corderid,pay_type_id);
	}
	@Override
	public List<MisPosOrderFormPay> getMisPosOrderFormPayRefundByCorderid(
			String corderid,int pay_type_id) {
		
		return	misPosOrderFormDao.queryMisPosOrderFormPayRefundByCorderid(corderid,pay_type_id);
	}
	
	@Override
	public int addPay(MisPosOrderFormPay misPosOrderFormPay) {
		
		return	misPosOrderFormDao.addPay(misPosOrderFormPay);
	}
	
	@Override
	public int addUnPay(MisPosOrderFormUnPay misPosOrderFormUnPay) {

		return	misPosOrderFormDao.addUnPay(misPosOrderFormUnPay);
	}

	@Override
	public MisPosOrderFormUnPay findFormUnPayById(String id) {
		
		return 	misPosOrderFormDao.findFormUnPayById(id);
	}

	@Override
	public int delUnPayById(String id) {
		
		return misPosOrderFormDao.delUnPayById(id);
	}

	@Override
	public MisPosOrderFormPay findFormPayById(String id) {

		return 	misPosOrderFormDao.findFormPayById(id);
	}

	@Override
	public MisPosOrderFormPay getMisPosOrderFormPayByExt(String ext) {

		return 	misPosOrderFormDao.getMisPosOrderFormByExt(ext);
	}

	@Override
	public void updateByCorderId(String ordernumber, String corderid) {

		misPosOrderFormDao.updateByCorderId(ordernumber,corderid);
		
	}


}
