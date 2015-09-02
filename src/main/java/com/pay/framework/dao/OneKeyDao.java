package com.pay.framework.dao;

import java.util.List;

import org.springframework.stereotype.Component;

import com.pay.model.OneKey;

@Component
public interface OneKeyDao {
	
	/**
	 * 获取用户绑定信息
	 * @param userId
	 * @return
	 */
	public OneKey getOneKeyById(int userId);
	
	/**
	 * 获取用户一键支付的绑定信息(需要绑定成功)
	 * @param userId
	 * @return
	 */
	public OneKey getOneKeyByIdSuccess(int userId);
	
	/**
	 * 获取用户绑定信息
	 * @param userId
	 * @return
	 */
	public List<OneKey> getOneKeyListBySign(String signNo);
	
	/**
	 * 通过用户id获取真正绑定的
	 * @param userId
	 * @return
	 */
	public List<OneKey> getOneKeyListByUserId(int userId);
	
	/**
	 * 增加一个绑定，这个时候是预处理
	 * @param oneKey
	 * @return
	 */
	public int addOnekey(OneKey oneKey);
	
	/**
	 * 绑定成功
	 * @param signNo
	 * @return
	 */
	public int bindSuccess(String signNo,String buyId);
	
	/**
	 * 解除绑定
	 * @param signNo
	 * @return
	 */
	public int unBind(String signNo);
	
	
	/**
	 * 获取用户签约成功的关联信息
	 * @param userId
	 * @param payType
	 * @return
	 */
	public List<OneKey> findOneKeyList(int userId,int payType);
}
