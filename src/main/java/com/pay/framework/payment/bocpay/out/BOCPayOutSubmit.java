package com.pay.framework.payment.bocpay.out;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class BOCPayOutSubmit {
	public static String creatHtml(String action, Map<String, String> map) {
		StringBuffer sb = new StringBuffer();
		sb.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"></head><body>");
		sb.append("<form id=\"pay_form_bocpay\" action=\"" + action
				+ "\" method=\"post\">");
		if (map != null && map.size() != 0) {
			Set<Entry<String, String>> set = map.entrySet();
			Iterator<Entry<String, String>> it = set.iterator();
			while (it.hasNext()) {
				Entry<String, String> e = it.next();
				String key = e.getKey();
				String value = e.getValue();
				sb.append("<input type=\"hidden\" id=\"" + key + "\" name=\""
						+ key + "\" value=\"" + value + "\" />");
			}
		}
		sb.append("</form>");
		sb.append("</body>");
		sb.append("<script type=\"text/javascript\">");
		sb.append("document.all.pay_form_bocpay.submit();");
		sb.append("</script>");
		sb.append("</html>");
		return sb.toString();
	}
	
	public static String creatRefundHtml(String action, Map<String, String> map) {
		StringBuffer sb = new StringBuffer();
		sb.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"></head><body>");
		sb.append("<form id=\"pay_form_bocrefund\" action=\"" + action
				+ "\" method=\"post\">");
		if (map != null && map.size() != 0) {
			Set<Entry<String, String>> set = map.entrySet();
			Iterator<Entry<String, String>> it = set.iterator();
			while (it.hasNext()) {
				Entry<String, String> e = it.next();
				String key = e.getKey();
				String value = e.getValue();
				sb.append("<input type=\"hidden\" id=\"" + key + "\" name=\""
						+ key + "\" value=\"" + value + "\" />");
			}
		}
		sb.append("</form>");
		sb.append("</body>");
		sb.append("<script type=\"text/javascript\">");
		sb.append("document.all.pay_form_bocrefund.submit();");
		sb.append("</script>");
		sb.append("</html>");
		return sb.toString();
	}
}
