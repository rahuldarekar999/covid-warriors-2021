package com.covid.warriors.controller;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.covid.warriors.response.model.MessageInfo;
import com.covid.warriors.service.CovidWarriorsService;
import com.fasterxml.jackson.core.JsonProcessingException;

@RestController  
@Configuration
@Component
public class MessageSenderController {
	
	@Autowired
	private CovidWarriorsService covidWarriorsService;
	
	@RequestMapping("/ping")  
	public String ping()   
	{  
		return "Running...!";  
	}  
	
	@CrossOrigin
	@RequestMapping("/sendMessage")  
	public ResponseEntity<?> sendMessage(@RequestParam("city") String city, 
			@RequestParam("category") String category) throws JsonProcessingException   
	{  
		
	    String response = covidWarriorsService.sendMessage(city, category);
		return ResponseEntity.ok().body("Message Sent Response is : " + response);  
	}
	
	@CrossOrigin
	@RequestMapping("/getResponse")  
	public ResponseEntity<?> getResponse(@RequestParam("city") String city, 
			@RequestParam("category") String category) throws JsonProcessingException   
	{  
		
		List<MessageInfo> messages = covidWarriorsService.getResponses(city, category);
		return ResponseEntity.ok().body(getMessageMap(messages));
	}

	private Map<String, Set<MessageInfo>> getMessageMap(List<MessageInfo> messageInfos) {
		if(Objects.nonNull(messageInfos) && !messageInfos.isEmpty()) {
			Map<String, Set<MessageInfo>> messageInfoMap = new HashMap<>();
			for(MessageInfo messageInfo : messageInfos) {
				Set<MessageInfo> messageInfoList = messageInfoMap.getOrDefault(messageInfo.getChatIdMobileNumber(), new TreeSet<>());
				messageInfoList.add(messageInfo);
				messageInfoMap.putIfAbsent(messageInfo.getChatIdMobileNumber(), messageInfoList);
			}
			return messageInfoMap;
		}
		return Collections.emptyMap();
	}
}
