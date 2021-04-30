package com.covid.warriors.response.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageInfo implements Comparable<MessageInfo> {
	
	private String id;
	
	private String body;
	private String type;
	private String author;
	private String chatId;
	private boolean fromMe;
	private long time;
	
	private boolean isValid=true;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getChatId() {
		return chatId;
	}
	public void setChatId(String chatId) {
		this.chatId = chatId;
	}
	
	public String getChatIdMobileNumber() {
		if(chatId!=null) {
			//System.out.println(chatId.substring(0, chatId.indexOf("@")));
			return chatId.substring(0, chatId.indexOf("@"));
			
		}
		return "";
	}
	

	public boolean isFromMe() {
		return fromMe;
	}
	public void setFromMe(boolean fromMe) {
		this.fromMe = fromMe;
	}
	
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	
	public boolean isValid() {
		return isValid;
	}
	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}
	@Override
	public String toString() {
		return "GetMessageResponse [id=" + id + ", body=" + body + ", type=" + type + ", author="
				+ author + ", chatId=" + chatId + "]";
	}

	@Override
	public int compareTo(MessageInfo o) {
		return (int) (o.time - time);
	}
}
