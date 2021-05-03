package com.covid.warriors.request.model;

import java.util.List;

public class WebhookMessageResponse {
   private List<ResponseMessage> messages;

	public List<ResponseMessage> getMessages() {
		return messages;
	}
	
	public void setMessages(List<ResponseMessage> messages) {
		this.messages = messages;
	}

	@Override
	public String toString() {
		return "WebhookMessageResponse [messages=" + messages + "]";
	}
}
