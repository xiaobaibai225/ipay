<%@ page language="java" contentType="text/html; charset="
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>支付平台测试页面</title>
</head>
<body>
	<div id="topay">
		<form id='payform' name='payform'
			action='http://115.182.51.40:8080/pay/6' method='post' target='_self'>
			<table>
				<tr>
					<td>用户ID</td>
					<td><input type='text' name='userid' value='17904223' maxlength="11"/>
				</tr>
				<tr>
					<td>用户名</td>
					<td><input type='text' name='username' value='werwersdf232' maxlength="30"/>
				</tr>
				<tr>
					<td>商户订单号(由商户生成)</td>
					<td><input type='text' name='corderid' value='1986516123423' maxlength="20"/></td>
				</tr>
				<tr>
					<td>商品ID</td>
					<td><input type='text' name='productid' value='2' maxlength="20"/>
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
					<td>跳转商户的页面地址</td>
					<td><input type='text' name='fronturl'
						value='http://www.baidu.com' maxlength="200"/>
				</tr>
				<tr>
					<td>通知商户的服务器端地址</td>
					<td><input type='text' name='backurl'
						value='http://www.baidu.com' maxlength="200"/>
				</tr>
				<tr>
					<td>商品金额</td>
					<td><input type='text' name='money' value='0.02' /></td>
				</tr>
				<tr>
					<td>支付通道</td>
					<td><select name="defaultbank">
							<option value="" selected>支付宝</option>
							
							<option value="CMB">招商银行</option>
							<option value="ICBC-DEBIT">中国工商银行</option>
							<option value="BOCB2C">中国银行</option>
							<option value="CCB-DEBIT">中国建设银行</option>
							
							<option value="COMM">交通银行</option>
							<option value="ABC">中国农业银行</option>
							<option value="CMBC">中国民生银行</option>
							<option value="CITIC">中信银行</option>
							
							<option value="CEBBANK">中国光大银行</option>
							<option value="CIB">兴业银行</option>
							<option value="BJBANK">北京银行</option>
							<option value="GDB">广发银行</option>
							
							
					</select></td>
				</tr>
				<tr>
					<td>公司标识</td>
					<td><input type='text' name='companyid' value='1' maxlength="10"/>
				</tr>
				<tr>
					<td>终端标识</td>
					<td><input type='text' name='deptid' value='111' maxlength="10"/>
				</tr>
				<tr>
					<td>签名</td>
					<td><input type='text' name='sign'
						value='cbd9418f68c2ec8e2f16123e0987a3ff' width="200px"/></td>
				</tr>
				<tr align="right">
					<td><input type="submit" value="确认支付" /></td>
				</tr>
			</table>
		</form>
	</div>
</body>
</html>