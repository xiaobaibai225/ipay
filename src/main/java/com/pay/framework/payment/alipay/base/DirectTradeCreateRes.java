/**
 * Alipay.com Inc.
 * Copyright (c) 2005-2008 All Rights Reserved.
 */
package com.pay.framework.payment.alipay.base;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;

/**
 * 
 * 
 * @author feng.chenf
 * @version $Id: CreateDirectTrade.java, v 0.1 2008-11-17 ����09:49:02 feng.chenf Exp $
 */
@XObject("direct_trade_create_res")
public class DirectTradeCreateRes {

    /**
     * ��õĴ������׵�RequestToken
     */
    @XNode("request_token")
    private String requestToken;

    public String getRequestToken() {
        return requestToken;
    }

}
