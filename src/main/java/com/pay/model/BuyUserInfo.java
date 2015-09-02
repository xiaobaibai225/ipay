package com.pay.model;

import com.pay.framework.annotation.db.Column;
import com.pay.framework.annotation.db.Table;

@Table("buyer_user_info")
public class BuyUserInfo {

	private String userid;

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public String getUsername() {
		return username;
	}

	@Override
	public String toString() {
		return "BuyUserInfo [userid=" + userid + ", username=" + username + ", phone=" + phone + ", email=" + email + ", address=" + address
				+ ", zipcode=" + zipcode + ", realname=" + realname + "]";
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getZipcode() {
		return zipcode;
	}

	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}

	public String getRealname() {
		return realname;
	}

	public void setRealname(String realname) {
		this.realname = realname;
	}

	private String username;

	private String phone;

	private String email;

	private String address;

	private String zipcode ;
	
	private String realname ;
	
	private String account;
	
	@Column("createtime")
	private String createdate;

	public String getCreatedate() {
		return createdate;
	}

	public void setCreatedate(String createdate) {
		this.createdate = createdate;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	
}
