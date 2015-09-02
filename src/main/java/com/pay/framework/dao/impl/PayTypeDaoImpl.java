package com.pay.framework.dao.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pay.framework.dao.PayTypeDao;
import com.pay.framework.db.DBManager;
import com.pay.model.PaymentBean;

@Component
public class PayTypeDaoImpl implements PayTypeDao {

	@Autowired
	private DBManager db;

	@Override
	public PaymentBean getPayType(int typeId) {
		String sql = "select * from pay_type where typeid = ?";
		Object[] args = new Object[] { typeId };
		return db.queryForObject(sql, PaymentBean.class, args);
	}

	@Override
	public String getBeanName(int typeId) {
		String sql = "select pt.bean from pay_type pt where typeid = ?";
		Object[] args = { typeId };
		List<PaymentBean> payments = db.queryForListObject(sql, PaymentBean.class, args);
		if ( null == payments ){
			return null;
		}
		return payments.get(0).getBean();
	}

	@Override
	public List<PaymentBean> getPayTypes() {
		String sql = "select * from pay_type where rownum <1000";
		List<PaymentBean> result = db
				.queryForListObject(sql, PaymentBean.class);
		return result;
	}

	@Override
	public List<PaymentBean> getPayTypeBycompanyIdExt_col2(int companyId, String ext_col2) {
		String sql = "select * from pay_type pt where ext_col2 = ? and companyid = ?";
		Object[] args = { ext_col2,companyId };
		List<PaymentBean> payments = db.queryForListObject(sql, PaymentBean.class, args);
		if ( null == payments ){
			return null;
		}
		return payments;
	}

	@Override
	public List<PaymentBean> getPayStatusList(int companyid, String institution,String platform, String available,String payMethod) {
		StringBuffer sql = new StringBuffer();
	    sql.append(" select * from pay_type where companyid = ?");
	    if("".equals(available)){
			available="Y";
		}
	    if(!"".equals(institution)){
			sql.append(" and institution ='"+institution+"' ");
		}
		if(!"".equals(platform)){
			sql.append(" and platform ='"+platform+"' ");
		}
		if(!"ALL".equals(available)){
			sql.append(" and available ='"+available+"' ");
		}
		if(!"".equals(payMethod)){
			sql.append(" and channel ='"+payMethod+"' ");
		}
		Object[] args = new Object[] { companyid };
		List<PaymentBean> payments=  db.queryForListObject(sql.toString(), PaymentBean.class, args);
		if ( null == payments ){
			return new ArrayList<PaymentBean>();
		}
		return payments;
	}

}
