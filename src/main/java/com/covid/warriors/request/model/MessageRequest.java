package com.covid.warriors.request.model;

import java.io.Serializable;

public class MessageRequest implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String body;
	private long phone;
	
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public long getPhone() {
		return phone;
	}
	public void setPhone(long phone) {
		this.phone = phone;
	}
	
	@Override
	public String toString() {
		return "MessageRequest [body=" + body + ", phone=" + phone + "]";
	}
	
}
