package com.covid.warriors.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	@RequestMapping("/sendMessageCustom")  
	public ResponseEntity<?> sendMessageCustom(@RequestParam("city") String city, 
			@RequestParam("category") String category, @RequestParam("message") String message,
			@RequestParam("mobileList") List<String> mobileList) throws JsonProcessingException   
	{  
		
	    String response = covidWarriorsService.sendMessageCustom(city, category, message, mobileList);
		return ResponseEntity.ok().body("Message Sent Response is : " + response);  
	}
	
	@CrossOrigin
	@RequestMapping("/getResponse")  
	public ResponseEntity<?> getResponse(@RequestParam("city") String city, 
			@RequestParam("category") String category) throws JsonProcessingException   
	{  
		
		List<MessageInfo> messages = covidWarriorsService.getResponses(city, category);
		return ResponseEntity.ok().body(covidWarriorsService.getPositiveMessages(messages));
	}
	
	@CrossOrigin
	@RequestMapping("/getCity")  
	public ResponseEntity<?> getCityList() throws JsonProcessingException   
	{  
		
		List<String> cityList = covidWarriorsService.getCityList();
		Map<String, List<String>> responseMap = new HashMap<>();
		responseMap.put("data", cityList);
		return ResponseEntity.ok().body(responseMap);
	}
	
	@CrossOrigin
	@RequestMapping("/getCategory")  
	public ResponseEntity<?> getCategoryList() throws JsonProcessingException   
	{  	
		List<String> categoryList = covidWarriorsService.getCategoryList();
		Map<String, List<String>> responseMap = new HashMap<>();
		responseMap.put("data", categoryList);
		return ResponseEntity.ok().body(responseMap);
	}
}
