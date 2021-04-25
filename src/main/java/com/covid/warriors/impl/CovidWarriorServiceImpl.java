package com.covid.warriors.impl;

import java.util.Arrays;
import java.util.List;

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
import com.covid.warriors.service.CovidWarriorsService;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CovidWarriorServiceImpl implements CovidWarriorsService {

	@Value("${api.url}")
	private String apiUrl;
	
	@Value("${instance.id}")
	private String instanceId;
	
	@Value("${token}")
	private String token;
	
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
	    		MessageRequest request = new MessageRequest();
	    		CategoryMessage message = categoryMessageRepo.findByCategory(contact.getCategory());
	    		request.setBody(message.getMessage());
	    		request.setPhone(Long.valueOf(contact.getMobileNumber()));
	    		HttpEntity<MessageRequest> entity = new HttpEntity<MessageRequest>(request,headers);
	    	    String url = apiUrl + instanceId + "/sendMessage?token=" + token;
	    	    System.out.println("URL : " + url);
	    	    String response = restTemplate.exchange(
	    	    		url, HttpMethod.POST, entity, String.class).getBody();
	    	    System.out.println("Response for : " + contact.getMobileNumber() + " : " + response);
	    	});
	    	return "Message Successfully Sent";  
	    }
		return "No Data for given City And Category";  
	}
}
