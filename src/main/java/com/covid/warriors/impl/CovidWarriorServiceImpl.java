package com.covid.warriors.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import com.covid.warriors.entity.model.CategoryMessage;
import com.covid.warriors.entity.model.ContactEntity;
import com.covid.warriors.repository.CategoryMessageRepository;
import com.covid.warriors.repository.ContactRepository;
import com.covid.warriors.request.model.MessageRequest;
import com.covid.warriors.response.model.GetMessagesResponse;
import com.covid.warriors.response.model.MessageInfo;
import com.covid.warriors.response.model.SendMessageResponse;
import com.covid.warriors.service.CovidWarriorsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CovidWarriorServiceImpl implements CovidWarriorsService {

	@Value("${api.url}")
	private String apiUrl;
	
	@Value("${instance.id}")
	private String instanceId;
	
	@Value("${token}")
	private String token;
	
	@Value("${resend.wait.min}")
	private int resendWaitMin;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private ObjectMapper mapper;
	
	@Autowired
	private ContactRepository contactRepo;
	
	@Autowired
	private CategoryMessageRepository categoryMessageRepo;
	
	@Override
	public String sendMessage(String city, 
			String category) 
	{  
		HttpHeaders headers = new HttpHeaders();
	    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
	    List<ContactEntity> contacts = contactRepo.findByCityAndCategory(city, category);
	    if(!CollectionUtils.isEmpty(contacts)) {
	    	contacts.parallelStream().forEach(contact ->{
	    		boolean resend = true;
	    		if(contact.getLastMessageSentTime() != null) {
	    			Date currDate = new Date();
	    			long diff = currDate.getTime() - contact.getLastMessageSentTime().getTime();
	    			long lastSentDiff = diff / (60 * 1000);
	                if (lastSentDiff < resendWaitMin) resend = false; 
	    		}
	    		if(contact.getLastMessageReceivedTime() != null) {
	    			Date currDate = new Date();
	    			long diff = currDate.getTime() - contact.getLastMessageReceivedTime().getTime();
	    			long lastReceivedDiff = diff / (60 * 1000);
	    			if (lastReceivedDiff < resendWaitMin) resend = false; 
	    		}
	    		if(resend){
		    		MessageRequest request = new MessageRequest();
		    		CategoryMessage message = categoryMessageRepo.findByCategory(contact.getCategory());
		    		request.setBody(message.getMessage());
		    		request.setPhone(Long.valueOf(contact.getMobileNumber()));
		    		HttpEntity<MessageRequest> entity = new HttpEntity<MessageRequest>(request,headers);
		    	    String url = apiUrl + instanceId + "/sendMessage?token=" + token;
		    	    System.out.println("URL : " + url);
		    	    String response = restTemplate.exchange(
		    	    		url, HttpMethod.POST, entity, String.class).getBody();
		    	    try {
		    	    	SendMessageResponse responseObj = mapper.readValue(response, SendMessageResponse.class);
		    	    	if(responseObj.isSent()) {
		    	    		contact.setLastMessageSentTime(new Date());
		    	    		contactRepo.saveAndFlush(contact);
		    	    	}
		    	    } catch(Exception ex){
		    	    	System.out.println("Error while parsing response");
		    		}
		    	    System.out.println("Response for : " + contact.getMobileNumber() + " : " + response);
	    		}
	    	});
	    	return "Message Successfully Sent";  
	    }
		return "No Data for given City And Category";  
	}

	@Override
	public List<MessageInfo> getResponses(String city, String category) {
		HttpHeaders headers = new HttpHeaders();
	    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
	    List<MessageInfo> messages = new ArrayList<MessageInfo>();
	    
	   
	    String url = apiUrl + instanceId + "/messagesHistory?token=" + token;
	    System.out.println("URL : " + url);
	    String response = restTemplate.exchange(
	    		url, HttpMethod.GET, null, String.class).getBody();
	   // System.out.println(response);
	    try {
			GetMessagesResponse responseObj = mapper.readValue(response, GetMessagesResponse.class);
			if(responseObj!=null) {
				messages = getValidResponses(city,category,responseObj.getMessages());
			}
		} catch (JsonProcessingException e) {
			System.out.println("Error while parsing response");
		}
		return messages;
	}
	
	private List<MessageInfo> getValidResponses(String city, String category, List<MessageInfo> messages) {
		System.out.println("Filtering valid messages" + messages.size());
		List<ContactEntity> contacts = contactRepo.findByCityAndCategory(city, category);
		List<String> validNumbers = contacts.stream()
			    .map(ContactEntity::getMobileNumber)
			    .collect(Collectors.toList());
		
		List<MessageInfo> validResponses = messages
				  .stream()
				  .filter(m -> (validNumbers.contains(m.getChatIdMobileNumber()) && !m.isFromMe()))
				  .collect(Collectors.toList());
		return validResponses;
	}
	
}
