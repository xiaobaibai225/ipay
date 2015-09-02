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
	String notifyUrl = request.getParameter("notifyUrl");
	System.out.println(notifyUrl);
	
	StringBuilder sb = new StringBuilder();
	sb.append(transType).append(transAmount).append(loyalty).append(MisTrace)
	.append(InstallmentTimes).append(oldAuthNo).append(oldPostrace).append(oldHostTrace)
	.append(oldTransDate).append(cashPcNum).append(cashierNum);


%>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>建行MIS POST 处理</title>
<script type="text/javascript" src="<%=request.getContextPath() %>/js/jquery.js"></script>
</head>
<body>
<object name="KeeperClient" classid="clsid:9BB1BFD1-D279-462B-BB7B-74AEF30A6BDA" style="height:18pt;width:120;display:none
	"codebase='http://114.251.247.103/ipay/KeeperClient.CAB'#version=-1,-1,-1,-1">
</object>
	<div id="topay">
		<form name="testForm" onsubmit="return false;">
			<table>
				<tr>
					<td>交易金额</td>
					<td><input type='text' name='account' value='<%=transAmount %>' maxlength="11" readonly />
				</tr>
				<tr>
					<td>交易结果</td>
					<td>
						<textarea name="outputdata" cols="90" rows="10"></textarea>
					</td>
				</tr>
				<tr align="right">
					<td><input type="submit" id="qrzf" value="确认支付" onclick="javascript:callKeeperClient()" /></td>
					<td><input type="submit" value="取消交易" onclick="javascript:alert(111)" /></td>
				</tr>
			</table>
		</form>
	</div>
</body>
</html>
<script language="JavaScript" type="text/javascript">


function callKeeperClient()
{
  jQuery("#qrzf").attr("disabled","disabled");
  var input = "<%=sb %>";
  var ret = KeeperClient.misposTrans(input,"1,0,0,1");
  var reg=new RegExp("^00");
  if(reg.test(ret)){// 返回00开始，标示交易成功，
	  paynotify(ret);
  }else{
	  alert("交易失败！失败原因："+ret+"。请及时联系系统支持人员！");
  }
}

// 交易结果使用ajax 通知 接口 
function paynotify(notifymsg){
	var url = "<%= notifyUrl %>";
	jQuery.ajax({
		type: "POST",
		url: url,
		data: 'ccbmsg='+encodeURI(notifymsg)+'&orderNo=<%= MisTrace %>',
		async: false,
		dataType:'text',
		success: function(data){
			if("success"==data){
				alert("支付成功！");
			}else if("noorder"==data){
				alert("没有该订单，请确认该订单是否已经发起过支付！");
			}else if("fail"==data){
				alert("交易失败！请及时联系系统支持人员。");
			}
			
		}
	});
}


</script>