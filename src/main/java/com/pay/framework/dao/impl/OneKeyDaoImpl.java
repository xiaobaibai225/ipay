package com.pay.framework.dao.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pay.framework.dao.OneKeyDao;
import com.pay.framework.db.DBManager;
import com.pay.model.OneKey;

@Component
public class OneKeyDaoImpl implements OneKeyDao {
	@Autowired
	private DBManager db;
	
	@Override
	public OneKey getOneKeyById(int userId) {
		return (OneKey) db.queryForObject("select * from PAY_ALIPAY_CONTRACT where userid = ?", OneKey.class, userId); 
	}
	
	@Override
	public OneKey getOneKeyByIdSuccess(int userId) {
		String sql = "select * from PAY_ALIPAY_CONTRACT where flag =1 and userid = ?";
		Object[] args = new Object[]{userId};
		List<OneKey> result = db.queryForListObject(sql,args, OneKey.class);
		return (result==null || result.size() <1) ? null : result.get(0);
	}
	
	

	@Override
	public int addOnekey(OneKey oneKey) {
		return db.insertObject(oneKey);
	}

	@Override
	public int bindSuccess(String signNo,String buyId) {
		String sql = "update PAY_ALIPAY_CONTRACT set flag=1,BUYER_LOGON_ID=? where EXTERNAL_SIGN_NO=?";
		Object[] args = new Object[]{buyId,signNo};
		return db.update(sql, args);
	}

	@Override
	public int unBind(String signNo) {
		String sql = "delete from  PAY_ALIPAY_CONTRACT where EXTERNAL_SIGN_NO=?";
		Object[] args = new Object[]{signNo};
		return db.update(sql, args);
	}


	@Override
	public List<OneKey> getOneKeyListBySign(String signNo) {
		String sql = "select * from PAY_ALIPAY_CONTRACT where EXTERNAL_SIGN_NO=?";
		Object[] args = new Object[]{signNo};
		List<OneKey> result = db.queryForListObject(sql,args, OneKey.class);
		return result;
	}

	@Override
	public List<OneKey> getOneKeyListByUserId(int userId) {
		String sql = "select * from PAY_ALIPAY_CONTRACT where userid=? and flag=1";
		Object[] args = new Object[]{userId};
		List<OneKey> result = db.queryForListObject(sql,args, OneKey.class);
		return result;
	}

	@Override
	public List<OneKey> findOneKeyList(int userId, int payType) {
		String sql = "select * from PAY_ALIPAY_CONTRACT where flag =1 and userid = ? and paytype = ?";
		Object[] args = new Object[]{userId,payType};
		List<OneKey> result = db.queryForListObject(sql,args, OneKey.class);
		return result;
	}

}
