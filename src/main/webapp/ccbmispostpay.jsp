<%@ page language="java" contentType="text/html; charset="
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<%
	String price = request.getParameter("price");
	String orderNum = request.getParameter("orderNum");
	String corderid = request.getParameter("corderid");
	String typeId = request.getParameter("typeId");
%>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>建行MIS POST 处理</title>
<script type="text/javascript" src="<%=request.getContextPath() %>/js/jquery.js"></script>
<script type="text/javascript" src="<%=request.getContextPath() %>/js/jquery-1.3.2.js"></script>
<style type="text/css">

</style>
</head>
<body>
   <div id="ocx"></div>
   <div id="toPay" align="center" width="500" >
		<div id="header" style="margin:20px;background-color:#99bbbb">
				<h1>MIS POS支付信息</h1>
		</div>
		<table width="500">
			<tr>
				<th style="width: 30%"><h2>应收金额</h2></th>
				<th id ="account" style="width: 30%"><%=price %></th>
				<td colspan='2'></td>
			</tr>
			<tr>
				<th style="width: 30%"><h2>已收金额</h2></th>
				<th  id ="received" style="width: 30%">0</th>
				<td colspan='2'></td>
			</tr>
			<tr>
			    <td ><input type="button" id="addRow" value="新增" style="width:60px;height:25px"/></td>
			    <td ><input type="button" id="delRow" value="删除" style="width:60px;height:25px"/></td>
			    <td colspan="2"></td>
			</tr>
		</table>
		<br/>
		<table id="payTable" width="500">
		    <tr>
		    	<th style="width: 20%">选择</th>
		    	<th style="width: 40%">交易金额</th>
		    	<th style="width: 40%">操作</th>
		    </tr>
            <tr id ="tr1" name = "payRow" align="center">
            	<td ><input type="checkbox" id="cbs1" name="cbs"  value="1"></td>
                <td align="center">
                   <input type="text" id="price1" name="price" value='<%=price %>'/>
                   <input type="hidden" id="id1" name="id"  />
                   <input type="hidden" name="msg" id="msg1" />
                </td>
                <td>
                    <input type="button" name="pay" id="pay1" value="支付"  onclick="pay(1)" style="display:block"/>
                    <input type="button" name="refund" id="refund1" value="退款" onclick="refund(1)" style="display:none"/>
                    <input type="button" name="print" id="print1" value="打印最近交易" onclick="print(1)" style="display:none"/>
                    <input type="button" name="reprint" id="reprint1" value="重打印小票" onclick="reprint(1)" style="display:none"/>
                    <input type="button" name="reSave"  id="reSave1" value="保存小票信息" onclick="reSave(1)" style="display:none" /> 
               </td>
            </tr>
		</table>
		<div align="center">
			<input type="button" id="finish" value="完成支付" onclick="paynotify()" style="display:none" />
			<input type="button" id="close" value="关闭" onclick="closeWindow()" style="display:none" />
		</div>
	</div>
</body>
</html>
<script language="JavaScript" type="text/javascript">

//全局变量
var rlen = 1;
if(navigator.platform.indexOf("64")>0){
	$("#ocx").html("<object  name=\"KeeperClient_64\" classid=\"clsid:7458EE69-37A2-4BD9-B13C-C70D1CC772E2\" style=\"height:18pt;width:120;display:none\"codebase='.\\ccbmispos\\KeeperClient_64.CAB'#version=-1,-1,-1,-1\"></object>");
}else{
	$("#ocx").html("<object  name=\"KeeperClient\" classid=\"clsid:9BB1BFD1-D279-462B-BB7B-74AEF30A6BDA\" style=\"height:18pt;width:120;display:none\"codebase='.\\ccbmispos\\KeeperClient.CAB'#version=-1,-1,-1,-1\"></object>");
}
jQuery(document).ready(function() {
//window.open("http://www.soho3q.com/ipay/pay/misposRefund?corderid=1508131128344220&companyid=350001&newcorderid=20150820180816681366&sign=230a2eb037518147e4c37c65daddc807&refund_fee=1");
	//增加行
	jQuery("#addRow").click(function() {
		rlen++;
		var tr = "<tr id=\"tr"+rlen+"\" name = \"payRow\" align=\"center\"><td><input type=\"checkbox\" id=\"cbs"+rlen+"\" name=\"cbs\" value=\""+rlen+"\"></td><td><input type=\"text\" id=\"price"+rlen+"\" name=\"price\"/><input type=\"hidden\" id=\"id"+rlen+"\" name=\"id\"  /><input type=\"hidden\" name=\"msg\" id=\"msg"+rlen+"\" /></td><td><input type=\"button\" name=\"pay\" id=\"pay"+rlen+"\" value=\"支付\" onclick=\"pay("+rlen+")\" style=\"display:block\"/><input type=\"button\" name=\"refund\" id=\"refund"+rlen+"\" value=\"退款\" onclick=\"refund("+rlen+")\" style=\"display:none\"/><input type=\"button\" name=\"print\" id=\"print"+rlen+"\" value=\"打印最近交易\" onclick=\"print("+rlen+")\" style=\"display:none\"/><input type=\"button\" name=\"reprint\" id=\"reprint"+rlen+"\" value=\"重打印小票\" onclick=\"reprint("+rlen+")\" style=\"display:none\"/><input type=\"button\" name=\"reSave\"  id=\"reSave"+rlen+"\" value=\"保存小票信息\" onclick=\"reSave("+rlen+")\" style=\"display:none\" /></td></tr>"
		jQuery(tr).appendTo("#payTable");
	});
	//删除行
	jQuery("#delRow").click(function() {
		//var len =$("tr[name='payRow']").length ;
		//if($("#pay"+len).is(":hidden")){
		//	return ;
		//}
		//$("#tr"+len).remove();
		var checkIdCount = 0;
		jQuery('[name=cbs]','#payTable').each(function() {
			if(jQuery(this).attr('checked')){
				checkIdCount++;
			};
		});
		if(checkIdCount==0){
			alert('请选中要删除的数据！');
			return;
		}
		jQuery('[name=cbs]','#payTable').each(function() {
			if(jQuery(this).attr('checked')){
				var len = jQuery(this).val();
				$("#tr"+len).remove();
			};
		});
	});
	//控制页面点击退格键  返回到别的页面
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

function misposTrans(msg){
	if(navigator.platform.indexOf("64")>0){
		return KeeperClient_64.misposTrans(msg,"1,0,0,1");
	}else{
		return KeeperClient.misposTrans(msg,"1,0,0,1");
	}
}
//付款
function pay(len){
		var account = jQuery("#account").text();
		var price = $("#price"+len).val();
		var hasReceived = jQuery("#received").text();
		if(hasReceived==null||""==hasReceived){
			hasReceived = 0;
		}
	    if(!isNum(price)){
		   alert("请输入正确的金额！");
		   return ;
	    }
	    if((parseFloat(hasReceived)+parseFloat(price))>parseFloat(account)){
	    	alert("支付总金额大于应收金额！");
	    	return ;
	    }
		//var url = "http://114.251.247.103/ipay/pay/getMisposPayParam/40001";
		var url = '<%=request.getContextPath() %>/pay/getMisposPayParam/'+<%=typeId%>;
		jQuery.ajax({
			type: "POST",
			url: url,
			data: {price:price,corderid:'<%=corderid%>',orderNum:'<%=orderNum %>'},
			async: false,
			dataType:'text',
			success: function(data){
				var json = eval("("+data+")");
				if(json.flag){
					 //var ret = KeeperClient.misposTrans(json.msg,"1,0,0,1");
					 var ret = misposTrans(json.msg);
					 var reg=new RegExp("^00");
					 if(ret==null||ret==""||ret=="null"){//pos返回信息为空
						 alert("网络异常，信息接收失败，请点击页面打印最近交易按钮，来确认本次交易是否成功！");
						 jQuery("#id"+len).val(json.id);
						 jQuery("#price"+len).attr("disabled",true);
						 jQuery("#price"+len).attr("readOnly","readOnly");//将input元素设置为readonly
						 jQuery("#pay"+len).attr("disabled",true);
						 jQuery("#cbs"+len).hide();//隐藏checkbox
						 jQuery("#print"+len).show();//显示打印按钮
						 return ;
					 }
					 if(reg.test(ret)){// 返回00开始，标示交易成功，
					    	if(saveMisposMsg(ret,len,json.id,1)){
					    		alert("支付成功！");
						    	jQuery("#id"+len).val(json.id);
						    	jQuery("#msg"+len).val(ret);
								jQuery("#pay"+len).hide();
								var rec = parseFloat(price)+parseFloat(hasReceived);
								jQuery("#received").text(rec);//更改实际收款金额
								jQuery("#price"+len).attr("disabled",true);
								jQuery("#price"+len).attr("readOnly","readOnly");//将input元素设置为readonly
								jQuery("#refund"+len).show();//显示退款按钮
								jQuery("#cbs"+len).hide();//隐藏checkbox
								jQuery("#reprint"+len).show();//显示重新打印小票按钮
								if(parseFloat(account)==rec){
									jQuery("#finish").show();//显示支付完成按钮
								}
					    	}else{
								alert("支付成功，后台数据保存失败，请点击页面手动保存小票信息按钮！");
								jQuery("#id"+len).val(json.id);
					    		jQuery("#msg"+len).val(ret);
					    		jQuery("#price"+len).attr("disabled",true);
					    		jQuery("#price"+len).attr("readOnly","readOnly");//将input元素设置为readonly
								jQuery("#pay"+len).attr("disabled",true);
								jQuery("#reSave"+len).show();//显示重新保存按钮
								jQuery("#cbs"+len).hide();//隐藏checkbox
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
				 //var ret = KeeperClient.misposTrans(json.msg,"1,0,0,1");
				 var ret = misposTrans(json.msg);
				 var reg=new RegExp("^00");
				 if(reg.test(ret)){// 返回00开始，标示交易成功，
					if(confirm("请核对小票，确认是否是当前交易？")){
						 jQuery("#print"+len).hide();
						 jQuery("#msg"+len).val(ret);
						 jQuery("#reSave"+len).show();//显示重新保存按钮
						 reSave(len);
					}else{
						if($("#pay"+len).is(":hidden")){//退款时
							 jQuery("#print"+len).hide();
							 jQuery("#refund"+len).attr("disabled",false);
						}else{
							 jQuery("#price"+len).attr("disabled",false);
							 jQuery("#price"+len).attr("readOnly","");
							 jQuery("#print"+len).hide();
							 jQuery("#pay"+len).attr("disabled",false);
							 jQuery("#cbs"+len).show();//显示checkbox
						}
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

//保存小票信息
function saveMisposMsg(msg,len,id,count){
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
					flag= true;
				}else {
					var counts = parseInt(count)+1;
					if(counts==3){
						flag= false;
					}else{
						saveMisposMsg(msg,len,id,counts);
					}
				}
			}
		});
		return flag;
 }

//重新保存小票信息
function reSave(len){
		var msg = $("#msg"+len).val();
		var id = $("#id"+len).val();
		var account = jQuery("#account").text();
		var price = $("#price"+len).val();
		var hasReceived = jQuery("#received").text();
		if(hasReceived==null||""==hasReceived){
			hasReceived = 0;
		}
		
		if(saveMisposMsg(msg,len,id,1)){
			jQuery("#reSave"+len).hide();//隐藏重新保存按钮
			if($("#pay"+len).is(":hidden")){//退款时
				jQuery("#refund"+len).attr("value","退款成功");
				jQuery("#refund"+len).attr("disabled",true);
				jQuery("#reprint"+len).show();
				var rec = parseFloat(hasReceived)-parseFloat(price);
				jQuery("#received").text(rec);
				jQuery("#finish").hide();//隐藏支付完成按钮
				//if(rec==0){//全部退款完   显示关闭按钮
				//	jQuery("#finish").show();
				//}
			}else{
				jQuery("#pay"+len).hide();
				jQuery("#refund"+len).show();
				jQuery("#reprint"+len).show();
				var rec = parseFloat(price)+parseFloat(hasReceived);
				jQuery("#received").text(rec);
				if(parseFloat(account)==rec){
					jQuery("#finish").show();
				}
			}
		}else{
			alert("保存小票信息失败，请重新保存！");
		}
 }

//退款
function refund(len){
	    var outerfundno ="";
		//var url = "http://114.251.247.103/ipay/pay/getMisposRefundParam/40001";
		var url = '<%=request.getContextPath() %>/pay/getMisposRefundParam/'+<%=typeId%>;
		var price = $("#price"+len).val();
		var id = jQuery("#id"+len).val();
		var hasReceived = jQuery("#received").text();
		var account = jQuery("#account").text();
		if(hasReceived==null||""==hasReceived){
			hasReceived = 0;
		}
		jQuery.ajax({
			type: "POST",
			url: url,
			data: {price:price,id:id,outerfundno:outerfundno},
			async: false,
			dataType:'text',
			success: function(data){
				var json = eval("("+data+")"); 
				if(json.flag){
					 //var ret = KeeperClient.misposTrans(json.msg,"1,0,0,1");
					 var ret = misposTrans(json.msg);
					 var reg=new RegExp("^00");
					 if(ret==null||ret==""||ret=="null"){
						 alert("网络异常，信息接收失败，请点击页面打印最近交易按钮，来确认本次交易是否成功！");
						 jQuery("#id"+len).val(json.id);
						 $("#refund"+len).attr("disabled",true);
						 jQuery("#print"+len).show();//显示打印按钮
						 return ;
					 }
					 if(reg.test(ret)){// 返回00开始，标示交易成功，
					    	if(saveMisposMsg(ret,len,json.refundId,1)){
					    		alert("退款成功！！！");
					    		jQuery("#id"+len).val(json.refundId);
								jQuery("#refund"+len).attr("value","退款成功");
								jQuery("#refund"+len).attr("disabled",true);
								jQuery("#reprint"+len).show();
								var rec = parseFloat(hasReceived)-parseFloat(price);
								jQuery("#received").text(rec);
								jQuery("#finish").hide();//隐藏支付完成按钮
								//if(rec==0){
								//	jQuery("#close").show();//显示关闭按钮
								//}
					    	}else{
					    		alert("退款成功，后台数据保存失败，请点击页面手动保存小票信息按钮！");
					    		jQuery("#id"+len).val(json.refundId);
					    		jQuery("#msg"+len).val(ret);
								jQuery("#refund"+len).attr("disabled",true);
								jQuery("#reSave"+len).show();//显示重新保存按钮
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

// 交易结果使用ajax 通知 接口 
function paynotify(){
	//var url = "http://114.251.247.103/ipay/pay/servernotify/40001";
	var url = '<%=request.getContextPath() %>/pay/servernotify/'+<%=typeId%> ;
	jQuery.ajax({
		type: "POST",
		url: url,
		data: 'orderNo=<%= orderNum %>',
		async: false,
		dataType:'text',
		success: function(data){
			if("success"==data){
				jQuery('[name=refund]','#payTable').each(function() {
					jQuery(this).attr("disabled",true);
				});
				jQuery("#finish").attr("disabled",true);
				jQuery("#close").show();
				alert("支付成功！");
			}else if("noorder"==data){
				alert("没有该订单，请确认该订单是否已经发起过支付！");
			}else if("fail"==data){
				alert("交易失败！请重新点击完成支付按钮");
			}
		}
	});
}

//关闭
function closeWindow(){
	alert("关闭窗口");
	window.opener=null;
	window.close();
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
				var allowPay = 0;
				var len = 0 ;
			    for(var i=0;i<jsons.length;i++){
			    	len++ ;
			    	if(parseFloat(jsons[i].allowRefund)<=0){
			    		len--;
			    		continue ;
			    	}
			    	if(len==1){
						$("#tr1").remove();
			    	}
			    	 allowPay= parseFloat(allowPay)+parseFloat(jsons[i].allowRefund);
			    	 var tr = "<tr id=\"tr"+len+"\" name = \"payRow\" align=\"center\" ><td><input type=\"checkbox\" id=\"cbs"+len+"\" name=\"cbs\" value=\""+len+"\"></td><td><input type=\"text\" id=\"price"+len+"\" name=\"price\" readOnly=\"readOnly\" value="+jsons[i].allowRefund+" ><input type=\"hidden\" id=\"id"+len+"\" name=\"id\" value="+jsons[i].id+" ><input type=\"hidden\" name=\"msg\" id=\"msg"+len+"\" /></td><td><input type=\"button\" name=\"pay\" id=\"pay"+len+"\" value=\"支付\" onclick=\"pay("+len+")\" style=\"display:none\"/><input type=\"button\" name=\"refund\" id=\"refund"+len+"\" value=\"退款\" onclick=\"refund("+len+")\" style=\"display:block\"/><input type=\"button\" name=\"print\" id=\"print"+len+"\" value=\"查询\" onclick=\"print("+len+")\" style=\"display:none\"/><input type=\"button\" name=\"reprint\" id=\"reprint"+len+"\" value=\"重打印小票\" onclick=\"reprint("+len+")\" style=\"display:block\"/><input type=\"button\" name=\"reSave\"  id=\"reSave"+len+"\" value=\"保存小票信息\" onclick=\"reSave("+len+")\" style=\"display:none\" /></td></tr>"
					 jQuery(tr).appendTo("#payTable");
			    }
			    rlen=len++;
				jQuery("#received").text(allowPay); 
				var account = jQuery("#account").text();
				if(parseFloat(account)==allowPay){
					jQuery("#finish").show();//显示支付完成按钮
				}
			}
		}
	});
});

</script>