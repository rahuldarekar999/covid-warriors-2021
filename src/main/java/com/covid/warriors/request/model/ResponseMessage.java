package com.covid.warriors.request.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseMessage {
	private String id;
    private String body;
    private String type;
    private String senderName;
    private boolean fromMe;
    private String author;
    private long time;
    private String chatId;
    private int messageNumber;
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
	public String getSenderName() {
		return senderName;
	}
	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}
	public boolean isFromMe() {
		return fromMe;
	}
	public void setFromMe(boolean fromMe) {
		this.fromMe = fromMe;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public String getChatId() {
		return chatId;
	}
	public void setChatId(String chatId) {
		this.chatId = chatId;
	}
	public int getMessageNumber() {
		return messageNumber;
	}
	public void setMessageNumber(int messageNumber) {
		this.messageNumber = messageNumber;
	}
	@Override
	public String toString() {
		return "ResponseMessage [id=" + id + ", body=" + body + ", type=" + type + ", senderName=" + senderName
				+ ", fromMe=" + fromMe + ", author=" + author + ", time=" + time + ", chatId=" + chatId
				+ ", messageNumber=" + messageNumber + "]";
	}
}
