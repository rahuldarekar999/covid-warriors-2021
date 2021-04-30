package com.covid.warriors.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
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
import com.covid.warriors.response.model.CheckPhoneResponse;
import com.covid.warriors.response.model.GetMessagesResponse;
import com.covid.warriors.response.model.MessageInfo;
import com.covid.warriors.response.model.SendMessageResponse;
import com.covid.warriors.service.CovidWarriorsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twilio.Twilio;

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

	@Value("${stop.message.sent.count}")
	private int stopMessageSentCount;

//	@Value("#{'${valid.response.black.list}'.split(',')}")
	@Value("#{'${valid.response.black.list}'.split(',')}")
	private List<String> blackListOfResponses;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private ContactRepository contactRepo;

	@Autowired
	private CategoryMessageRepository categoryMessageRepo;

	@Override
	public String sendMessage(String city, String category) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		List<ContactEntity> contacts = contactRepo.findByCityAndCategory(city, category);
		if (!CollectionUtils.isEmpty(contacts)) {
			contacts.parallelStream().forEach(contact -> {
				boolean resend = true;
				if (contact.getLastMessageSentTime() != null) {
					Date currDate = new Date();
					long diff = currDate.getTime() - contact.getLastMessageSentTime().getTime();
					long lastSentDiff = diff / (60 * 1000);
					if (lastSentDiff < resendWaitMin)
						resend = false;
				}
				if (contact.getLastMessageReceivedTime() != null) {
					Date currDate = new Date();
					long diff = currDate.getTime() - contact.getLastMessageReceivedTime().getTime();
					long lastReceivedDiff = diff / (60 * 1000);
					if (lastReceivedDiff < resendWaitMin)
						resend = false;
				} else if (contact.getLastMessageReceivedTime() == null && contact.getMessageSentCount() != null
						&& contact.getMessageSentCount() >= stopMessageSentCount) {
					resend = false;
				}
				if (resend) {
					MessageRequest request = new MessageRequest();
					CategoryMessage message = categoryMessageRepo.findByCategory(contact.getCategory());
					request.setBody(message.getMessage());
					request.setPhone(Long.valueOf(contact.getMobileNumber()));
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
							if (responseObjMessage.isSent()) {
								contact.setLastMessageSentTime(new Date());
								contact.setMessageSentCount(
										contact.getMessageSentCount() != null ? contact.getMessageSentCount() + 1 : 0);
								contactRepo.saveAndFlush(contact);
							}

							System.out.println("Response for : " + contact.getMobileNumber() + " : " + response);

						} else {
							contact.setWhatsAppExist(false);
							contactRepo.saveAndFlush(contact);
						}
					} catch (Exception ex) {
						System.out.println("Error while parsing response : " + ex);
					}
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
		String response = restTemplate.exchange(url, HttpMethod.GET, null, String.class).getBody();
		// System.out.println(response);
		try {
			GetMessagesResponse responseObj = mapper.readValue(response, GetMessagesResponse.class);
			if (responseObj != null) {
				messages = getValidResponses(city, category, responseObj.getMessages());
			}
		} catch (JsonProcessingException ex) {
			System.out.println("Error while parsing response : " + ex);
		}
		return messages;
	}

	private List<MessageInfo> getValidResponses(String city, String category, List<MessageInfo> messages) {
		System.out.println("Filtering valid messages" + messages.size());
		List<ContactEntity> contacts = contactRepo.findByCityAndCategory(city, category);
		List<String> validNumbers = contacts.stream().map(ContactEntity::getMobileNumber).collect(Collectors.toList());

		List<MessageInfo> validResponses = messages.stream()
				.filter(m -> (validNumbers.contains(m.getChatIdMobileNumber()) && !m.isFromMe()))
				.collect(Collectors.toList());
		if (!CollectionUtils.isEmpty(validResponses)) {
			validResponses.forEach(response -> {
				try {
					System.out.println("Contact checking : " + response.getChatIdMobileNumber());
					ContactEntity contact = contactRepo
							.findByMobileNumberAndCityAndCategory(response.getChatIdMobileNumber(), city, category);
					Date responseTime = new Date(response.getTime());
					Date currTime = new Date();
					boolean update = false;
					if (currTime.before(responseTime)) {
						contact.setLastMessageReceivedTime(new Date(response.getTime()));
						update = true;
					}
					if (contact.getMessageSentCount() > 0) {
						contact.setMessageSentCount(0);
						update = true;
					}
					if (update) {
						contactRepo.saveAndFlush(contact);
					}
				} catch (Exception ex) {
					System.out.println("Error while storing response time : " + ex);
				}
			});
		}
		return validResponses;
	}

	public static void main(String[] args) {
		Twilio.init("AC04e8e8d3a6b5eff922219c1fcde05f39", "b9caa7ec18e5388cd529b2621cff21ec");
		/*
		 * Call call = Call.creator( new com.twilio.type.PhoneNumber("+919923175711"),
		 * new com.twilio.type.PhoneNumber("+917588037827"), new
		 * com.twilio.type.Twiml("<Response><Say>Ahoy, World!</Say></Response>"))
		 * .create();
		 * 
		 * System.out.println(call.getSid() + " : " + call.getStatus() + " : " +
		 * call.getAnsweredBy() + " : ");
		 */

		/*
		 * PhoneNumber phoneNumber = PhoneNumber.fetcher( new
		 * com.twilio.type.PhoneNumber("+917719508074"))
		 * .setType(Arrays.asList("carrier")).fetch();
		 * 
		 * System.out.println(phoneNumber);
		 */
		boolean result = StringUtils.contains(
				"This is test response who are you? I dont know you.,why are you spamming me../stop sending messaged"
						.toLowerCase(),
				"who are you");
		System.out.println(result);
	}

	@Override
	public Map<String, Set<MessageInfo>> getPositiveMessages(List<MessageInfo> messageInfos) {
		if (Objects.nonNull(messageInfos) && !messageInfos.isEmpty()) {
			Map<String, Set<MessageInfo>> messageInfoMap = new HashMap<>();
			for (MessageInfo messageInfo : messageInfos) {
				if (StringUtils.isNotBlank(messageInfo.getBody())) {
					//	System.out.println("msg -> " + messageInfo.getBody());
					//if(messageInfo.isValid()) {
						boolean isValid = true;
						blackListOfResponses.stream().forEach(msg -> {
					//		System.out.println("black list -> " + msg);
					//		System.out.println("condition -> " + StringUtils.contains(messageInfo.getBody().toLowerCase(), msg));
							if (StringUtils.contains(messageInfo.getBody().toLowerCase(), msg)) {
								messageInfo.setValid(false);
							}
					//		System.out.println("map -> " + messageInfo.isValid());
						});
						if(messageInfo.isValid()) {
							Set<MessageInfo> messageInfoList = messageInfoMap
									.getOrDefault(messageInfo.getChatIdMobileNumber(), new TreeSet<>());
							messageInfoList.add(messageInfo);
							messageInfoMap.putIfAbsent(messageInfo.getChatIdMobileNumber(), messageInfoList);
						}
					//}
				}
			}
			return messageInfoMap;
		}
		return Collections.emptyMap();
	}
}
