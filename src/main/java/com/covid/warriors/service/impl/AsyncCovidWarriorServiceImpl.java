package com.covid.warriors.service.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import com.covid.warriors.entity.model.ContactEntity;
import com.covid.warriors.entity.model.SentMessageMetadataEntity;
import com.covid.warriors.repository.SentMessageMetadataRepository;
import com.covid.warriors.request.model.MessageRequest;
import com.covid.warriors.request.model.ResponseMessage;
import com.covid.warriors.response.model.CheckPhoneResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AsyncCovidWarriorServiceImpl {

	@Value("${api.url}")
	private String apiUrl;

	@Value("${instance.id.forward}")
	private String instanceIdForwards;		
	
	@Value("${token.forward}")
	private String tokenForForwards;		
	
	@Value("${message.response}")
	private String responseMessage;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private ObjectMapper mapper;
	
	@Autowired
	private SentMessageMetadataRepository sentMetadataMessageRepo;
	
	@Async
	public void forwardMessage(List<ContactEntity> entityList, SentMessageMetadataEntity forwardObj, String receivedFrom, ResponseMessage message) {
		
		if(!CollectionUtils.isEmpty(entityList)) {
			entityList.forEach(entity -> {
				if(entity.getMobileNumber().equals(receivedFrom)) {
					MessageRequest request = new MessageRequest();
					String messageStr = responseMessage;
					messageStr = messageStr.replaceAll("!mob!", receivedFrom).replace("!city!", forwardObj.getCity()).replace("!cat!", forwardObj.getCategory()).replace("!msg!", message.getBody());
					request.setBody(messageStr);
					request.setPhone(Long.valueOf(forwardObj.getFrom()));
					System.out.println("sub : " + request);
					forwardMessageToNumber(request);	
				}
			});										
		}
		forwardObj.setSentOn(new Date());
		sentMetadataMessageRepo.saveAndFlush(forwardObj);
	}

	private void forwardMessageToNumber(MessageRequest request) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
			String url = apiUrl + instanceIdForwards + "/checkPhone?token=" + tokenForForwards + "&phone="
					+ request.getPhone();

			String responseForCheckPhone = restTemplate.exchange(url, HttpMethod.GET, null, String.class).getBody();
			CheckPhoneResponse responseObj = mapper.readValue(responseForCheckPhone, CheckPhoneResponse.class);
			if (responseObj != null && "exists".equalsIgnoreCase(responseObj.getResult())) {
				HttpEntity<MessageRequest> entity = new HttpEntity<MessageRequest>(request, headers);
				url = apiUrl + instanceIdForwards + "/sendMessage?token=" + tokenForForwards;
				System.out.println("URL : " + url);
				restTemplate.exchange(url, HttpMethod.POST, entity, String.class).getBody();
			}
		} catch (Exception ex) {
			System.out.println("Error while parsing response for forward: " + ex);
		}
	}
}
