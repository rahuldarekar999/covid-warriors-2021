package com.covid.warriors.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.covid.warriors.request.model.CustomMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import com.covid.warriors.response.model.MessageInfo;
import com.covid.warriors.service.CovidWarriorsService;
import com.fasterxml.jackson.core.JsonProcessingException;

@RestController  
@Configuration
@Component
@CrossOrigin
public class MessageSenderController {
	
	@Autowired
	private CovidWarriorsService covidWarriorsService;
	
	@RequestMapping("/ping")  
	public String ping()   
	{  
		return "Running...!";  
	}  
	
	@RequestMapping("/sendMessage")  
	public ResponseEntity<?> sendMessage(@RequestParam("city") String city, 
			@RequestParam("category") String category) throws JsonProcessingException   
	{  
		
	    String response = covidWarriorsService.sendMessage(city, category);
		return ResponseEntity.ok().body("Message Sent Response is : " + response);  
	}
	
	@RequestMapping(value = "/sendMessageCustom", method = RequestMethod.POST)
	public ResponseEntity<?> sendMessageCustom(@RequestBody CustomMessage customMessage) throws JsonProcessingException
	{  
		
	    String response = covidWarriorsService.sendMessageCustom(customMessage);
		return ResponseEntity.ok().body("Message Sent Response is : " + response);  
	}
	
	@RequestMapping("/getResponse")  
	public ResponseEntity<?> getResponse(@RequestParam("city") String city, 
			@RequestParam("category") String category) throws JsonProcessingException   
	{  
		
		List<MessageInfo> messages = covidWarriorsService.getResponses(city, category);
		return ResponseEntity.ok().body(covidWarriorsService.getPositiveMessages(messages));
	}
	
	@RequestMapping("/getCity")  
	public ResponseEntity<?> getCityList()   
	{  
		
		List<String> cityList = covidWarriorsService.getCityList();
		Map<String, List<String>> responseMap = new HashMap<>();
		responseMap.put("data", cityList);
		return ResponseEntity.ok().body(responseMap);
	}
	
	@RequestMapping("/getCategory")  
	public ResponseEntity<?> getCategoryList() 
	{  	
		List<String> categoryList = covidWarriorsService.getCategoryList();
		Map<String, List<String>> responseMap = new HashMap<>();
		responseMap.put("data", categoryList);
		return ResponseEntity.ok().body(responseMap);
	}
	
	
	@RequestMapping("/minTime")  
	public ResponseEntity<?> getMinTime() 
	{  	
		Map<String, Long> responseMap = new HashMap<>();
		responseMap.put("data", covidWarriorsService.getMinTime());
		return ResponseEntity.ok().body(responseMap);
	}
}
