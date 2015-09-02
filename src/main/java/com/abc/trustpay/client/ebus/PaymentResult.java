package com.abc.trustpay.client.ebus;

import java.io.UnsupportedEncodingException;

import com.abc.trustpay.client.Base64;
import com.abc.trustpay.client.LogWriter;
import com.abc.trustpay.client.MerchantConfig;
import com.abc.trustpay.client.TrxException;
import com.abc.trustpay.client.TrxResponse;
import com.abc.trustpay.client.XMLDocument;

public class PaymentResult extends TrxResponse
{
  public PaymentResult(String aMessage)
    throws TrxException
  {
    super("Notify", aMessage);
    LogWriter tLogWriter = null;
    try
    {
      tLogWriter = new LogWriter();
      tLogWriter.logNewLine("TrustPayClient Java V2.0 交易开始==========================");
      tLogWriter.logNewLine("接收到的支付结果通知：\n[" + aMessage + "]");

      Base64 tBase64 = new Base64();
      String tMessage="";
	try {
		tMessage = new String(tBase64.decode(aMessage),"gb2312");
	} catch (UnsupportedEncodingException e) {
		e.printStackTrace();
	}
      tLogWriter.logNewLine("经过Base64解码后的支付结果通知：\n[" + tMessage + "]");

      tLogWriter.logNewLine("验证支付结果通知的签名：");
      XMLDocument tResult = MerchantConfig.getUniqueInstance().verifySignXML(new XMLDocument(tMessage));
      tLogWriter.logNewLine("验证通过！\n 经过验证的支付结果通知：\n[" + tResult.toString() + "]");

      init(tResult);
    }
    catch (TrxException e) {
      setReturnCode(e.getCode());
      setErrorMessage(e.getMessage() + "-" + e.getDetailMessage());
      tLogWriter.log("验证失败！\n");

      if (tLogWriter != null) {
        tLogWriter.logNewLine("交易结束==================================================");
        try { tLogWriter.closeWriter(MerchantConfig.getTrxLogFile("PayResultLog"));
        }
        catch (Exception localException)
        {
        }
      }
    }
    finally
    {
      if (tLogWriter != null) {
        tLogWriter.logNewLine("交易结束==================================================");
        try { tLogWriter.closeWriter(MerchantConfig.getTrxLogFile("PayResultLog"));
        }
        catch (Exception localException1)
        {
        }
      }
    }
  }
}