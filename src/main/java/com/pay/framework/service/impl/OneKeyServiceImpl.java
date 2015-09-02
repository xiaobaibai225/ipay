package com.pay.framework.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pay.framework.dao.OneKeyDao;
import com.pay.framework.service.OneKeyService;
import com.pay.framework.service.PayTypeService;
import com.pay.model.OneKey;

@Component
public class OneKeyServiceImpl implements OneKeyService {

	@Autowired
	private OneKeyDao oneKeyDao;
	
	@Autowired
	private PayTypeService payTypeService;
	
	@Override
	public OneKey getOneKeyById(int userId) {
		OneKey oneKey = oneKeyDao.getOneKeyById(userId);
		return oneKey;
	}
	
	@Override
	public OneKey getOneKeyByIdSuccess(int userId,int companyId) {
		OneKey oneKey = oneKeyDao.getOneKeyByIdSuccess(userId);
		if(oneKey == null){
			return null;
		}
		return oneKey;
//			else{
//			PaymentBean payment = payTypeService.getPayType(companyId,oneKey.getPaytype());
//			String sHtmlText = AlipayUtil.oneKeyContractQuery(payment, oneKey.getSignNo(),payment.getMerchantid(),payment.getSeckey());
//			List<String> confirm = XmlUtil.getContentByKey(sHtmlText, "//alipay/is_success");
//			String mobile = XmlUtil.getContentByKeyOnly(sHtmlText,  "//alipay/response/userSignInfo/mobile");
//			oneKey.setBuyId(mobile);
//			if(confirm == null || confirm.size() <1){
//				return null;
//			}else{
//				String confirmValue = confirm.get(0);
//				if(confirmValue.toLowerCase().equals("t")){
//					return oneKey;
//				}else{
//					return null;
//				}
//			}
//		}
	}

	@Override
	public int addOnekey(OneKey oneKey) {

		return oneKeyDao.addOnekey(oneKey);
	}

	@Override
	public int bindSuccess(String signNo,String buyId) {

		return oneKeyDao.bindSuccess(signNo,buyId);
	}

	@Override
	public int unBind(String signNo) {

		return oneKeyDao.unBind(signNo);
	}

	@Override
	public List<OneKey> getOneKeyListBySign(String signNo) {
		return oneKeyDao.getOneKeyListBySign(signNo);
	}

	@Override
	public List<OneKey> getOneKeyListByUserId(int userId) {
		return oneKeyDao.getOneKeyListByUserId(userId);
	}

	@Override
	public List<OneKey> getOneKeyList(int userId, int payType) {
		return oneKeyDao.findOneKeyList(userId, payType);
	}

}
