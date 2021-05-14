package com.covid.warriors.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.covid.warriors.request.model.Account;
import com.covid.warriors.request.model.Message;

@Service
public class AsyncSmsServiceImpl {
	
	@Value("${sms.api.link}")
	private String smsApiEndpoint;
	
	@Value("${sms.api.send}")
	private String smsApiSendContext;
	
	@Value("${sms.api.key}")
	private String apiKey;
	
	@Value("${sms.sender.id}")
	private String senderId;
	
	@Value("${sms.channel}")
	private String channel;
	
	@Value("${sms.dcs}")
	private String dcs;
	
	@Value("${sms.route}")
	private String route;
	
	@Autowired
	private RestTemplate restTemplate;

	@Async
	public void sendBulkSmsGatewayHub(String msg, String mobileNumbers) {
		
		Account account = new Account();
		account.setAPIKey(apiKey);
		account.setSenderId(senderId);
		account.setChannel(channel);
		account.setDCS(dcs);
		account.setRoute(route);
		
		Message message = new Message();
		List<Message> listOfMessages = new ArrayList<>();
		
		message.setNumber(mobileNumbers);
		message.setText(msg);
		
		listOfMessages.add(message);
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		Map<String, Object> request = new HashMap<>();
		request.put("Account", account);
		request.put("Messages", listOfMessages);
		
		HttpEntity<?> entity = new HttpEntity<>(request, headers);
		String response = restTemplate.exchange(smsApiEndpoint + smsApiSendContext, HttpMethod.POST, entity, String.class).getBody();
	}
}
