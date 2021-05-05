package com.covid.warriors.service.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import com.covid.warriors.repository.ContactRepository;
import com.covid.warriors.repository.SentMessageMetadataRepository;
import com.covid.warriors.request.model.MessageRequest;
import com.covid.warriors.request.model.ResponseMessage;
import com.covid.warriors.response.model.CheckPhoneResponse;
import com.covid.warriors.response.model.SendMessageResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AsyncCovidWarriorServiceImpl {

	@Value("${api.url}")
	private String apiUrl;
	
	@Value("${instance.id}")
	private String instanceId;

	@Value("${instance.id.forward}")
	private String instanceIdForwards;		
	
	@Value("${token.forward}")
	private String tokenForForwards;		
	
	@Value("${token}")
	private String token;
	
	@Value("${message.response}")
	private String responseMessage;
	
	@Value("${stop.message.sent.count}")
	private int stopMessageSentCount;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private ObjectMapper mapper;
	
	@Autowired
	private SentMessageMetadataRepository sentMetadataMessageRepo;
	
	@Autowired
	private ContactRepository contactRepo;
	
	@Value("${resend.wait.min}")
	private int resendWaitMin;
	
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
				//	System.out.println("sub : " + request);
					forwardMessageToNumber(request);	
				}
			});										
		}
		try {
			forwardObj.setSentOn(new Date());
			sentMetadataMessageRepo.saveAndFlush(forwardObj);
		} catch(Exception ex) {
			System.out.println("Exception while saving subscribed user object : " + ex);
			ex.printStackTrace();
		}
	}
	
	@Async
	public void sendAsyncMessage(String contact, String city, String category, String message) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		contact = getPhoneNumber(contact);
		boolean resend = true;
		boolean isNew = false;
		if(contact.length() == 12) {
			ContactEntity contactEntity = contactRepo.findByMobileNumberAndCityAndCategoryAndValid(contact, city, category, true);
			if(contactEntity == null) {
				contactEntity = new ContactEntity();
				contactEntity.setMobileNumber(contact);
				contactEntity.setCity(city.toUpperCase());
				contactEntity.setCategory(category.toUpperCase());
				contactEntity.setValid(true);
				contactEntity.setSource("WEBSITE");
				isNew = true;
			}
			if(isNew || (contactEntity.getValid() != null && contactEntity.getValid())) {
				if (contactEntity.getLastMessageSentTime() != null) {
					Date currDate = new Date();
					long diff = currDate.getTime() - contactEntity.getLastMessageSentTime().getTime();
					long lastSentDiff = diff / (60 * 1000);
					if (lastSentDiff < resendWaitMin)
						resend = false;
				}
				if (contactEntity.getLastMessageReceivedTime() != null) {
					Date currDate = new Date();
					long diff = currDate.getTime() - contactEntity.getLastMessageReceivedTime().getTime();
					long lastReceivedDiff = diff / (60 * 1000);
					if (lastReceivedDiff < resendWaitMin)
						resend = false;
				} else if (contactEntity.getLastMessageReceivedTime() == null && contactEntity.getMessageSentCount() != null
						&& contactEntity.getMessageSentCount() >= stopMessageSentCount) {
					resend = false;
				}
				if (resend) {
					MessageRequest request = new MessageRequest();
					request.setBody(message);
					request.setPhone(Long.valueOf(contact));
					try {
						String url = apiUrl + instanceId + "/checkPhone?token=" + token + "&phone="
								+ request.getPhone();

						String responseForCheckPhone = restTemplate.exchange(url, HttpMethod.GET, null, String.class)
								.getBody();
						CheckPhoneResponse responseObj = mapper.readValue(responseForCheckPhone,
								CheckPhoneResponse.class);
						if (responseObj != null && "exists".equalsIgnoreCase(responseObj.getResult())) {
							HttpEntity<MessageRequest> entity = new HttpEntity<MessageRequest>(request, headers);
							url = apiUrl + instanceId + "/sendMessage?token=" + token;
							System.out.println("URL : " + url);
							String response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class)
									.getBody();

							SendMessageResponse responseObjMessage = mapper.readValue(response,
									SendMessageResponse.class);
							System.out.println("Response for : " + contact + " : " + response);
							
							if (responseObjMessage.isSent()) {
								contactEntity.setLastMessageSentTime(new Date());
								contactEntity.setMessageSentCount(
										contactEntity.getMessageSentCount() != null ? contactEntity.getMessageSentCount() + 1 : 0);
							//	contactRepo.saveAndFlush(contactEntity);
							}
					    } else {
					    	contactEntity.setWhatsAppExist(false);
						}
					} catch (Exception ex) {
						System.out.println("Error while parsing response : " + ex);
					}
				}
				contactRepo.saveAndFlush(contactEntity);
			}
		}
	}
	
	@Async
	public void forwardMessageToNumber(MessageRequest request) {
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
	
	private String getPhoneNumber(String contact) {
		contact.replaceAll("[()\\s-]+", "");
		if (contact.contains("+")) {
			contact = contact.replace("+", "");
		}
		Pattern p1 = Pattern.compile("(91)?[6-9][0-9]{9}");
		Pattern p2 = Pattern.compile("(0)?[6-9][0-9]{9}");
		Matcher m1 = p1.matcher(contact);
		Matcher m2 = p2.matcher(contact);
		boolean isPhoneWithNineOne = (m1.find() && m1.group().equals(contact));
		boolean isPhoneWithZero = (m2.find() && m2.group().equals(contact));
		if (contact.length() == 10) {
			contact = "91" + contact;
		} else if (isPhoneWithNineOne) {
			contact = contact;
		} else if (isPhoneWithZero) {
			contact = contact.substring(1, contact.length());
		}
		if (contact.length() == 10) {
			contact = "91" + contact;
		}
		return contact;
	}
}
