<%@ page language="java" contentType="text/html; charset="
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<%
    String orderNum = request.getParameter("orderNum");
	String corderid = request.getParameter("corderid");
	String refund_fee = request.getParameter("refund_fee");
	String outerfundno = request.getParameter("outerfundno");
	String typeId = request.getParameter("typeId");
%>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>建行MIS POS退款 处理</title>
<script type="text/javascript" src="<%=request.getContextPath() %>/js/jquery.js"></script>
<script type="text/javascript" src="<%=request.getContextPath() %>/js/jquery-1.3.2.js"></script>

</head>
<body >
	<div id="ocx"></div>
	<div align="center" width="800" >
		<div id="header" style="margin:20px;background-color:#99bbbb">
				<h1>MIS POS退款信息</h1>
		</div>
		<table width="800">
			<tr>
				<th style="width: 30%"><h2>应退金额</h2></th>
				<th id ="account" style="width: 30%"><%=refund_fee %></th>
				<th colspan='2'></th>
			</tr>
			<tr>
				<th style="width: 30%"><h2>已退金额</h2></th>
				<th id ="refunded" style="width: 30%">0</th>
				<th colspan='2'></th>
			</tr>
		</table>
		<br/><br/><br/>
		<table id="refundTable" width="800" >
            <tr>
              	<th style="width: 20%">卡行</th>
             	<th style="width: 20%">卡号</th>
            	<th style="width: 10%">支付金额</th>
            	<th style="width: 10%">可退金额</th>
            	<th style="width: 20%">实际退款金额</th>
            	<th style="width: 20%">操作</th>
            </tr>
		</table>
	</div>
	<div>
	 	<input type="button" name="close" id = "close" value="退款完成" onclick="refundNotify()"  style="display:none" />
	</div>
</body>
</html>
<script language="JavaScript" type="text/javascript">

jQuery(document).ready(function() {
	jQuery(document).keydown(function (e) {
	    var doPrevent;
	    if (e.keyCode == 8) {
	        var d = e.srcElement || e.target;
	        if (d.tagName.toUpperCase() == 'INPUT' || d.tagName.toUpperCase() == 'TEXTAREA') {
	            doPrevent = d.readOnly || d.disabled;
	        }
	        else
	            doPrevent = true;
	    }
	    else
	        doPrevent = false;
	
	    if (doPrevent)
	        e.preventDefault();
	}); 
});

if(navigator.platform.indexOf("64")>0){
	$("#ocx").html("<object  name=\"KeeperClient_64\" classid=\"clsid:7458EE69-37A2-4BD9-B13C-C70D1CC772E2\" style=\"height:18pt;width:120;display:none\"codebase='.\\ccbmispos\\KeeperClient_64.CAB'#version=-1,-1,-1,-1\"></object>");
}else{
	$("#ocx").html("<object  name=\"KeeperClient\" classid=\"clsid:9BB1BFD1-D279-462B-BB7B-74AEF30A6BDA\" style=\"height:18pt;width:120;display:none\"codebase='.\\ccbmispos\\KeeperClient.CAB'#version=-1,-1,-1,-1\"></object>");
}
function misposTrans(msg){
	if(navigator.platform.indexOf("64")>0){
		return KeeperClient_64.misposTrans(msg,"1,0,0,1");
	}else{
		return KeeperClient.misposTrans(msg,"1,0,0,1");
	}
}

function saveMisposMsg(msg,len,id){
		//var url = "http://114.251.247.103/ipay/pay/saveMisposMsg/40001";
		var url = '<%=request.getContextPath() %>/pay/saveMisposMsg/'+<%=typeId%>;
		var flag = false;
		jQuery.ajax({
			type: "POST",
			url: url,
			data: 'msg='+encodeURI(msg)+'&id='+id,
			async: false,
			dataType:'text',
			success: function(data){
				if("success"==data){
					//alert("退款成功！");
					flag = true;
				}else {
					flag = false;
				}
			}
		});
		return flag;
 }


function refund(len){
		var account = $("#account").text();
		var price = $("#realRefund"+len).val();
		var allowRefund = $("#allowRefund"+len).text();
	    var hasRefund = $("#refunded").text();
		if(hasRefund==null||""==hasRefund){
			hasRefund = 0;
		}
	    if(!isNum(price)){
		   alert("请输入正确的金额！！！");
		   return ;
	    }
	    if(parseFloat(price)>parseFloat(allowRefund)){
	    	 alert("退款金额 大于该银行卡的可退金额！！！");
			 return ;
	    }
	    if((parseFloat(hasRefund)+parseFloat(price))>parseFloat(account)){
	    	alert("退款总金额大于应退金额！！！！");
	    	return ;
	    }
	    var outerfundno = '<%=outerfundno%>';
		//var url = "http://114.251.247.103/ipay/pay/getMisposRefundParam/40001";
		var url = '<%=request.getContextPath() %>/pay/getMisposRefundParam/'+<%=typeId%>;
		var id = jQuery("#id"+len).val();
		jQuery.ajax({
			type: "POST",
			url: url,
			data: {price:price,id:id,outerfundno:outerfundno},
			async: false,
			dataType:'text',
			success: function(data){
				var json = eval("("+data+")"); 
				if(json.flag){
					// var ret = KeeperClient.misposTrans(json.msg,"1,0,0,1");
					 var ret = misposTrans(json.msg);
					 var reg=new RegExp("^00");
					 if(ret==null||ret==""||ret=="null"){//pos返回信息为空
						 alert("网络异常，信息接收失败，请点击页面打印最近交易按钮，来确认本次交易是否成功！");
						 jQuery("#id"+len).val(json.id);
						 $("#price"+len).attr("disabled",true);
						 $("#price"+len).attr("readOnly","readOnly");//将input元素设置为readonly
						 $("#refund"+len).attr("disabled",true);
						 jQuery("#print"+len).show();//显示打印按钮
						 return ;
					 }
					 if(reg.test(ret)){// 返回00开始，标示交易成功，
					    	if(saveMisposMsg(ret,len,json.refundId)){
					    		jQuery("#id"+len).val(json.refundId);
					    		jQuery("#msg"+len).val(ret);
						    	var rec = parseFloat(hasRefund)+parseFloat(price);
								jQuery("#refunded").text(rec);
								$("#realRefund"+len).attr("disabled",true);
								$("#realRefund"+len).attr("readOnly","readOnly")//将input元素设置为readonly
								jQuery("#refund"+len).attr("disabled",true);
								jQuery("#refund"+len).attr("value","退款成功");
								jQuery("#reprint"+len).show();//显示重打印按钮
								if(parseFloat(account)==rec){
									//refundNotify();
									jQuery("#close").show();
								}
					    	}else{
					    		alert("退款成功，后台数据保存失败，请点击页面手动保存小票信息按钮！");
					    		jQuery("#id"+len).val(json.refundId);
					    		jQuery("#msg"+len).val(ret);
					    		$("#realRefund"+len).attr("disabled",true);
								$("#realRefund"+len).attr("readOnly","readOnly")//将input元素设置为readonly
								jQuery("#refund"+len).attr("disabled",true);
								jQuery("#reSave"+len).show();//显示保存小票信息按钮
					    	}
				    }else{
				    	alert("交易失败！失败原因："+ret+"。请及时联系系统支持人员！");
				    }
				}else{
					alert(json.msg);
				}
			}
		});
}

//交易结果使用ajax 通知 接口 
function refundNotify(){
    //var url = "http://114.251.247.103/ipay/pay/refundnotify/40001";
    var url = '<%=request.getContextPath() %>/pay/refundnotify/'+<%=typeId%>;
	jQuery.ajax({
		type: "POST",
		url: url,
		data: 'orderNum=<%=orderNum %>',
		async: false,
		dataType:'text',
		success: function(data){
			if("success"==data){
				jQuery("#close").attr("disabled",true);
				alert("退款成功！");
				//跳转至主页面
			}else if("fail"==data){
				alert("交易失败！请重新点击页面退款完成按钮！");
			}
		}
	}); 
}

//打印最近交易
function print(len){
	var id = jQuery("#id"+len).val();
	//var url = "http://114.251.247.103/ipay/pay/getMisposReprintParam/40001";
	 var url = '<%=request.getContextPath() %>/pay/getMisposReprintParam/'+<%=typeId%>;
	jQuery.ajax({
		type: "POST",
		url: url,
		data: 'id='+id,
		async: false,
		dataType:'text',
		success: function(data){
			var json = eval("("+data+")");
			if(json.flag){
				// var ret = KeeperClient.misposTrans(json.msg,"1,0,0,1");
				 var ret = misposTrans(json.msg);
				 var reg=new RegExp("^00");
				 if(reg.test(ret)){// 返回00开始，标示交易成功，
					if(confirm("请核对小票，确认是否是当前交易？")){
						 jQuery("#print"+len).hide();
						 jQuery("#msg"+len).val(ret);
						 jQuery("#reSave"+len).show();//显示重新保存按钮
						 reSave(len);
					}else{
						 jQuery("#print"+len).hide();
						 $("#refund"+len).attr("disabled",false);
						 $("#price"+len).attr("disabled",false);
						 $("#price"+len).attr("readOnly","");
				   }
			   }else{
				   alert("打印最近交易失败！失败信息："+ret);
			   }
		  }else{
			 alert(json.msg);
		  }
	   }
	});
}


//重打印小票
function reprint(len){
	var id = jQuery("#id"+len).val();
	//var url = "http://114.251.247.103/ipay/pay/getMisposReprintParam/40001";
	var url = '<%=request.getContextPath() %>/pay/getMisposReprintParam/'+<%=typeId%>;
	jQuery.ajax({
		type: "POST",
		url: url,
		data: 'id='+id,
		async: false,
		dataType:'text',
		success: function(data){
			var json = eval("("+data+")");
			if(json.flag){
				 //var ret = KeeperClient.misposTrans(json.msg,"1,0,0,1");
				 var ret = misposTrans(json.msg);
				 var reg=new RegExp("^00");
				 if(reg.test(ret)){// 返回00开始，标示交易成功，
				    alert("重打印成功！");
				 }else{
					 alert("重打印失败！失败信息："+ret);
				 }
			}else{
				alert(json.msg);
			}
		}
	});
}

//重新保存小票信息
function reSave(len){
		var account = $("#account").text();
		var price = $("#realRefund"+len).val();
	    var hasRefund = $("#refunded").text();
		if(hasRefund==null||""==hasRefund){
			hasRefund = 0;
		}
		var msg = $("#msg"+len).val();
		var id = $("#id"+len).val();
	
		if(saveMisposMsg(msg,len,id)){
			 jQuery("#reSave"+len).hide();//隐藏重新保存按钮
			 jQuery("#refund"+len).attr("value","退款成功");
			 jQuery("#reprint"+len).show();
			 var rec = parseFloat(hasRefund)+parseFloat(price);
			 jQuery("#refunded").text(rec);
			 if(parseFloat(account)==rec){
				 jQuery("#close").show();
			 }
		}else{
			alert("保存小票信息失败，请重新保存！");
		}
 }

function isNum(price){
	if(price==null||price==""){
		return false;
	}
	var reg=/^[0-9]+(.[0-9]{0,})?$/;
	return reg.test(price);
}

$(function(){
	//var url = "http://114.251.247.103/ipay/pay/getMisposPayJson/40001";
	var url = '<%=request.getContextPath() %>/pay/getMisposPayJson/'+<%=typeId%>;
	
	jQuery.ajax({
		type: "POST",
		url: url,
		data: 'corderid=<%=corderid %>',
		async: false,
		dataType:'text',
		success: function(data){
			if(data!=null&&data!=""&&"null"!=data){
				var jsons = eval("("+data+")"); 
				var currentRefund = 0;
			    for(var len=0;len<jsons.length;len++){
			       currentRefund =parseFloat(currentRefund)+parseFloat(jsons[len].currentRefund);
				   var tr = "<tr id=\"tr"+len+"\" name = \"payRow\"><td>"+jsons[len].cardName+"</td><td>"+jsons[len].cardNameCode+"</td><td>"+jsons[len].transAmount+"</td><td id=\"allowRefund"+len+"\" name=\"allowRefund\">"+jsons[len].allowRefund+"</td><td><input type=\"hidden\" id=\"id"+len+"\" name=\"id\" value="+jsons[len].id+" ><input type=\"hidden\" id=\"msg"+len+"\" name=\"msg\" /><input type=\"text\" id=\"realRefund"+len+"\" name=\"realRefund\" /></td><td><input type=\"button\" name=\"refund\" id=\"refund"+len+"\" value=\"退款\" onclick=\"refund("+len+")\" style=\"display:block\"/><input type=\"button\" name=\"print\" id=\"print"+len+"\" value=\"打印最近交易\" onclick=\"print("+len+")\" style=\"display:none\"/><input type=\"button\" name=\"reSave\" id=\"reSave"+len+"\" value=\"保存小票信息\" onclick=\"reSave("+len+")\" style=\"display:none\"/><input type=\"button\" name=\"reprint\" id=\"reprint"+len+"\" value=\"重打印小票\" onclick=\"reprint("+len+")\" style=\"display:none\"/></td></tr>"
				   jQuery(tr).appendTo("#refundTable");
			    }
			    jQuery("#refunded").text(currentRefund);
			    if(parseFloat(account)==currentRefund){
					jQuery("#close").show();//显示退款完成按钮
				}
			}
		}
	});
});
</script>