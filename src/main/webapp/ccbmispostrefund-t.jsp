<%@page import="com.pay.model.Refund"%>
<%@ page language="java" contentType="text/html; charset="
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<% 
	String transType = request.getParameter("transType");
	String transAmount = request.getParameter("transAmount");
	String loyalty = request.getParameter("loyalty");
	String MisTrace = request.getParameter("MisTrace");
	String InstallmentTimes = request.getParameter("InstallmentTimes");
	String oldAuthNo = request.getParameter("oldAuthNo");
	String oldPostrace = request.getParameter("oldPostrace");
	String oldHostTrace = request.getParameter("oldHostTrace");
	String oldTransDate = request.getParameter("oldTransDate");
	String cashPcNum = request.getParameter("cashPcNum");
	String cashierNum = request.getParameter("cashierNum");
	
	String sign = request.getParameter("sign");
	String companyid = request.getParameter("companyid");
	String refund_fee = request.getParameter("refund_fee");
	//String newcorderid = request.getParameter("newcorderid");
	String corderid = request.getParameter("corderid");
	String refundnotify = request.getParameter("refundnotify");
	
	
	StringBuilder sb = new StringBuilder();
	sb.append(transType).append(transAmount).append(loyalty).append(MisTrace)
	.append(InstallmentTimes).append(oldAuthNo).append(oldPostrace).append(oldHostTrace)
	.append(oldTransDate).append(cashPcNum).append(cashierNum);

%>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>建行MIS POST 退款处理</title>
<script type="text/javascript" src="<%=request.getContextPath() %>/js/jquery.js"></script>
</head>
<body>
<object name="KeeperClient" classid="clsid:9BB1BFD1-D279-462B-BB7B-74AEF30A6BDA" style="height:18pt;width:120;display:none
	"codebase='http://114.251.247.103/ipay/KeeperClient.CAB'#version=-1,-1,-1,-1">
</object>

	<div id="refund">
		<form name="refundForm" onsubmit="return false;">
			<table>
				<tr>
					<td>用户ID</td>
					<td><input type='text' name='userid' value='17904223' maxlength="11" readonly="readonly"/>
				</tr>
				<tr>
					<td>用户名</td>
					<td><input type='text' name='username' value='shaochangfu' maxlength="30" readonly="readonly"/>
				</tr>
				<tr>
					<td>商户订单号(由商户生成)</td>
					<td><input type='text' name='corderid' id='corderid' value='<%=corderid %>' maxlength="20"/></td>
				</tr>
				<%-- <tr>
				     <td>商户订单号(checkId)</td>
					<td><input type='text' name='newcorderid' id='newcorderid' value='<%=newcorderid%>'  maxlength="20"/></td>
				</tr> --%>
				<tr>
					<td>商品ID</td>
					<td><input type='text' name='productid' value='<%=refund_fee %>' maxlength="20"/>
				</tr>
				<tr>
					<td>商品类型</td>
					<td><input type='text' name='producttype' value='111' maxlength="20"/>
				</tr>
				<tr>
					<td>商品数量</td>
					<td><input type='text' name='productnum' value='1' /></td>
				</tr>
				<tr>
					<td>购买类型</td>
					<td>
					<select name="buyType">
							<option value="0">实物</option>
							<option value="1">虚拟商品</option>
					</select>
					</td>
				</tr>
				<tr>
					<td>支付类型</td>
					<td><select name="chargetype">
							<option value="0">购买商品</option>
							<option value="1">充值</option>
					</select></td>
				</tr>
				<tr>
					<td>商品名称</td>
					<td><input type='text' name='productname' value='test' maxlength="100"/>
				</tr>
				<tr>
					<td>商品描述</td>
					<td><input type='text' name='productdesc' value='test' maxlength="100"/>
				</tr>
				<tr>
					<td>商品金额</td>
					<td><input type='text' name='refund_fee' id='refund_fee' value='<%=refund_fee%>'/></td>
				</tr>
				<tr>
					<td>公司标识</td>
					<td><input type='text' name='companyid' id='companyid' value='350001' maxlength="10"/>
					<td><input type='hidden' name='refundurl' id='refundurl' value='http://www.baidu.com?1=1' maxlength="10"/>
				</tr>
				
				<tr>
					<td>签名</td>
					<td><input type='text' name='sign' id='sign' value='<%=sign %>'
						 width="200px"/></td>
				</tr>
				<tr align="right">
				    <td><input type="submit" id="qrzf" value="确认退款" onclick="javascript:callKeeperClient()" /></td>
					<td><input type="submit" value="取消退款" onclick="javascript:alert(111)" /></td>
				</tr>
			</table>
		</form>
	</div><%--

	var url = "http://114.251.247.103/ipay/pay/refund?ccbmsg="+notifymsg+"&corderid="+corderid+"&sign="+sign+"&companyid="+companyid+"&refund_fee="+refund_fee;

	alert(url);
	$.get(
    	url,
    	{},
    	function(param) {
        	alert(param);
   		},
    "json"
    );
	
	
--%></body>
</html>
<script language="JavaScript" type="text/javascript">


function callKeeperClient()
{
  jQuery("#qrzf").attr("disabled","disabled");
  var input = "<%=sb %>";
  var ret = KeeperClient.misposTrans(input,"1,0,0,1");
  alert(ret);
  var reg=new RegExp("^00");
  if(reg.test(ret)){// 返回00开始，标示交易成功，
	  refundNotify(ret);
  }else{
	  alert("退款失败！失败原因："+ret+"。请及时联系系统支持人员！");
  }
}

// 交易结果使用ajax 通知 接口 
function refundNotify(notifymsg){
    var url = "<%= refundnotify %>";
	jQuery.ajax({
		type: "POST",
		url: url,
		data: 'ccbmsg='+encodeURI(notifymsg)+'&orderNo=<%= MisTrace %>',
		async: false,
		dataType:'text',
		success: function(data){
			alert(data);
			if("success"==data){
				alert("退款成功！");
				//跳转至主页面
			}else if("fail"==data){
				alert("交易失败！请及时联系系统支持人员。");
			}else{
				alert("****"+data)
			}
			
		}
	}); 
	
}


</script>