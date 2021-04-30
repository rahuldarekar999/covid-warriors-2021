package com.covid.warriors.response.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GetMessagesResponse {
	private List<MessageInfo> messages;
	
	private int page;
	
	private int lastMessageNumber;

	public List<MessageInfo> getMessages() {
		return messages;
	}

	public void setMessages(List<MessageInfo> messages) {
		this.messages = messages;
	}

	public int getLastMessageNumber() {
		return lastMessageNumber;
	}

	public void setLastMessageNumber(int lastMessageNumber) {
		this.lastMessageNumber = lastMessageNumber;
	}
	
	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	@Override
	public String toString() {
		return "GetMessagesResponse [messages=" + messages + ", lastMessageNumber=" + lastMessageNumber + "]";
	}
	
	
}
