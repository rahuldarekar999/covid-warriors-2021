package com.covid.warriors.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
	
	@RequestMapping("/sendMessage")  
	public ResponseEntity<?> sendMessage(@RequestParam("city") String city, 
			@RequestParam("category") String category) throws JsonProcessingException   
	{  
		
	    String response = covidWarriorsService.sendMessage(city, category);
		return ResponseEntity.ok().body("Message Sent Response is : " + response);  
	}
}
