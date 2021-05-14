package com.covid.warriors.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.covid.warriors.repository.ContactRepository;
import com.covid.warriors.request.model.CustomMessage;
import com.covid.warriors.request.model.TwitterMetadataResponse;
import com.covid.warriors.request.model.WebhookMessageResponse;
import com.covid.warriors.response.model.MessageInfo;
import com.covid.warriors.service.CovidWarriorsService;
import com.covid.warriors.service.impl.UrlService;
import com.fasterxml.jackson.core.JsonProcessingException;

@RestController  
@Configuration
@Component
@CrossOrigin
public class MessageSenderController {
	
	@Autowired
	private CovidWarriorsService covidWarriorsService;
	
	
	@Autowired
	private UrlService urlService;
	
	@Value("${days.before}")
	private int daysBefore;
	
	@Autowired
	private ContactRepository contactRepo;
	
	@RequestMapping("/ping")  
	public String ping()   
	{  
		return "Running...!";  
	}  
	
	//below endpoint is for testing purpose only
	@RequestMapping(value="/test") 
	public ResponseEntity<?> saveFrom(@RequestParam("city") String city, 
			@RequestParam("category") String category) throws JsonProcessingException   
	{  
		return ResponseEntity.ok().body(contactRepo.findTop250ByCityAndCategoryAndValidOrderByLastMessageReceivedTimeDesc(city, category, true).size());
	}
	
	@RequestMapping(value = "/checkObj", method = RequestMethod.POST)
	public ResponseEntity<?> check(@RequestBody CustomMessage customMessage) throws JsonProcessingException
	{  
		if(customMessage.getObj() != null) { 
			if(customMessage.getObj() instanceof List) {
				System.out.println("list : " + customMessage.getObj());
			} else if(customMessage.getObj() instanceof String) {
				System.out.println("Strng : " + customMessage.getObj());
			}
		}
	    //String response = covidWarriorsService.sendMessageCustom(customMessage);
		return ResponseEntity.ok().body("Message Sent Response is : " + true);  
	}
	
	@RequestMapping("/sendMessage")  
	public ResponseEntity<?> sendMessage(@RequestParam("city") String city, 
			@RequestParam("category") String category) throws JsonProcessingException   
	{  
		
	    String response = covidWarriorsService.sendMessage(city, category);
		return ResponseEntity.ok().body("Message Sent Response is : " + response);  
	}
	
	@RequestMapping(value = "/receiveMessage", method = RequestMethod.POST)
	public ResponseEntity<?> receiveMessage(@RequestBody WebhookMessageResponse message) throws JsonProcessingException
	{  
		String response = covidWarriorsService.forwardMessage(message.getMessages());
		return ResponseEntity.ok().body("Message Forward Response is : " + response);  
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
		
		List<MessageInfo> messages = covidWarriorsService.getResponses(city, category, daysBefore);
		Map<String, List<MessageInfo>> messageInfoMap = new HashMap<>();
		for(MessageInfo messageInfo : messages) {
			List<MessageInfo> messageInfoList = messageInfoMap
					.getOrDefault(messageInfo.getChatIdMobileNumber(), new ArrayList<>());
			messageInfoList.add(messageInfo);
			messageInfoMap.putIfAbsent(messageInfo.getChatIdMobileNumber(), messageInfoList);
		}
		return ResponseEntity.ok(messageInfoMap);
		//return ResponseEntity.ok().body(covidWarriorsService.getPositiveMessages(messages, city, category, true));
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
		responseMap.put("data", covidWarriorsService.getMinTime(daysBefore));
		return ResponseEntity.ok().body(responseMap);
	}
	
	@RequestMapping("/getMessage")  
	public ResponseEntity<?> getMessage(@RequestParam("category") String category) 
	{  	
		Map<String, String> responseMap = new HashMap<>();
		responseMap.put("data", covidWarriorsService.getMessageForCategory(category));
		return ResponseEntity.ok().body(responseMap);
	}
	
	@RequestMapping(value="/subscribe", method = RequestMethod.POST) 
	public ResponseEntity<?> subscribe(@RequestBody CustomMessage customMessage) 
	{  	
	//	covidWarriorsService.saveDataForSentMessages(customMessage, customMessage.getMobileList());
		covidWarriorsService.sendSms(customMessage, customMessage.getMobileList());
	    return ResponseEntity.ok().body("Saved");
	}
	
	@RequestMapping(value="/count") 
	public ResponseEntity<?> getCount(@RequestParam String city, @RequestParam String category) 
	{  	
		Map<String, Integer> responseMap = new HashMap<>();
		int dbCount = covidWarriorsService.getCountOfValidNumberByCityAndCategory(city, category);
		//int count = dbCount > 250 ? 250 : dbCount;
		responseMap.put("data", dbCount);
	    return ResponseEntity.ok().body(responseMap);
	}
	
	@RequestMapping(value="/sendFromSocialMedia", method = RequestMethod.POST) 
	public ResponseEntity<?> sendFromSocialMedia(@RequestBody CustomMessage customMessage) 
	{  	
		int count = covidWarriorsService.saveDataForSentMessagesFromSocialMedia(customMessage);
		Map<String, Integer> responseMap = new HashMap<>();
		responseMap.put("data", count);
	    return ResponseEntity.ok().body(responseMap);
	}
	
	@RequestMapping(value="/getTwitterData", method = RequestMethod.POST) 
	public ResponseEntity<?> getTwitterData(@RequestBody CustomMessage customMessage) 
	{  	
		TwitterMetadataResponse twitterMetadata = covidWarriorsService.getTwitterData(customMessage);
		return ResponseEntity.ok().body(twitterMetadata);
	}
	
	@RequestMapping(value="/shortenUrl", method = RequestMethod.GET) 
	public ResponseEntity<?> shortenUrl(@RequestParam("url") String url) 
	{  	
		String urlResponse = urlService.convertToShortUrl(url);
		return ResponseEntity.ok().body(urlResponse);
	}
	
	@RequestMapping(value="/sendSms", method = RequestMethod.POST) 
	public ResponseEntity<?> sendSms(@RequestBody CustomMessage customMessage) 
	{  	
		covidWarriorsService.sendSms(customMessage, customMessage.getMobileList());
		
		return ResponseEntity.ok().body("true");
	}
}
