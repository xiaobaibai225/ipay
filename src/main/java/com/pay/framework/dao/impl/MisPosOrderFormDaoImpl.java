package com.pay.framework.dao.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.pay.framework.dao.MisPosOrderFormDao;
import com.pay.framework.db.DBManager;
import com.pay.model.MisPosOrderFormPay;
import com.pay.model.MisPosOrderFormUnPay;

@Component
public class MisPosOrderFormDaoImpl implements  MisPosOrderFormDao {

	@Autowired
	private DBManager db;

	@Override
	public int addPay(MisPosOrderFormPay formPay) {
		int result = db.insertObject(formPay);
		return result;
	}

	@Override
	public List<MisPosOrderFormPay> queryMisPosOrderFormPayByOrderNumber(String ordernumber) {
		//return db.queryForListObject("select * from mispos_orderform where ordernumber = ?", MisPosOrderFormPay.class, ordernumber);
		String sql = "select * from mispos_orderform where ordernumber = ? and transType = ?";
		Object[] args = new Object[] { ordernumber,"S1"};
		return db.queryForListObject(sql, args, MisPosOrderFormPay.class);
	
	}
	@Override
	public List<MisPosOrderFormPay> queryMisPosOrderFormPayByCorderid(String corderid,int typeId) {
		String sql = "select * from mispos_orderform where corderid = ? and transType = ? and paytype = ?";
		Object[] args = new Object[] { corderid,"S1",typeId};
		return db.queryForListObject(sql, args, MisPosOrderFormPay.class);
		//return db.queryForListObject("select * from mispos_orderform where ordernumber = ?", MisPosOrderFormPay.class, corderid);
	}
	
	@Override
	public List<MisPosOrderFormPay> queryMisPosOrderFormPayRefundByCorderid(
			String corderid,int typeId) {
		
		String sql = "select * from mispos_orderform where corderid = ? and transType = ? and paytype = ?";
		Object[] args = new Object[] { corderid,"S3",typeId};
		return db.queryForListObject(sql, args, MisPosOrderFormPay.class);
	}
	@Override
	public MisPosOrderFormPay getMisPosOrderFormByOth(String hostTrace,String terminalNum, String authorNum) {
		String sql = "select * from mispos_orderform where hostTrace=? and terminalNum=? and terminalNum=?";
		Object[] args = new Object[] { hostTrace, terminalNum, authorNum};
		List<MisPosOrderFormPay> retList = db.queryForListObject(sql, args, MisPosOrderFormPay.class);
		if (retList != null && retList.size() > 0) {
			return retList.get(0);
		} else {
			return null;
		}
	}

	@Override
	public MisPosOrderFormUnPay findFormUnPayById(String id) {
		String sql = "select * from mispos_orderform_unpay where id=?";
		Object[] args = new Object[] { id };
		return db.queryForObject(sql, MisPosOrderFormUnPay.class, args);
	}

	@Override
	public int delUnPayById(String id) {
		String sql = "delete from mispos_orderform_unpay where id=? ";
		Object[] args = new Object[] { id };
		int result = db.update(sql, args);
		return result;
		
	}

	@Override
	public int addUnPay(MisPosOrderFormUnPay misPosOrderFormUnPay) {
		int result = db.insertObject(misPosOrderFormUnPay);
		return result;
	}

	@Override
	public MisPosOrderFormPay findFormPayById(String id) {
		String sql = "select * from mispos_orderform where id=?";
		Object[] args = new Object[] { id };
		return db.queryForObject(sql, MisPosOrderFormPay.class, args);
	}

	@Override
	public MisPosOrderFormPay getMisPosOrderFormByExt(String ext) {

		String sql = "select * from mispos_orderform where ext = ?";
		Object[] args = new Object[] { ext};
		//return db.queryForListObject(sql, args, MisPosOrderFormPay.class);
		List<MisPosOrderFormPay> retList = db.queryForListObject(sql, args, MisPosOrderFormPay.class);
		if (retList != null && retList.size() > 0) {
			return retList.get(0);
		} else {
			return null;
		}
	}

	@Override
	public void updateByCorderId(String ordernumber, String corderid) {
		String sql1 = "update mispos_orderform set ordernumber =? where corderid=? ";
		String sql2 = "update mispos_orderform_unpay set ordernumber =? where corderid=? ";
		Object[] args = new Object[] { ordernumber, corderid };
		db.update(sql1, args);
		db.update(sql2, args);
	}

	

}
