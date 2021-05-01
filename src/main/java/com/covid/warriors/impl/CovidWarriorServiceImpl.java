package com.covid.warriors.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
	
	@Value("${days.before}")
	private int daysBefore;

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
	public String sendMessageCustom(String city, String category, String message, List<String> mobileList) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		List<ContactEntity> contacts = contactRepo.findByCityAndCategory(city, category);
		if (!CollectionUtils.isEmpty(mobileList)) {
			mobileList.parallelStream().forEach(contact -> {
				contact = contact.replaceAll("[()\\s-]+", "");
				if(contact.contains("+")) {
					contact = contact.replace("+", "");
				}
				Pattern p1 = Pattern.compile("(91)?[6-9][0-9]{9}");
				Pattern p2 = Pattern.compile("(0)?[6-9][0-9]{9}");
				Matcher m1 = p1.matcher(contact);
				Matcher m2 = p2.matcher(contact);
				boolean isPhoneWithNineOne = (m1.find() && m1.group().equals(contact));
				boolean isPhoneWithZero = (m2.find() && m2.group().equals(contact));
				if(contact.length() == 10) {
					contact = "91" + contact;
				} else if(isPhoneWithNineOne) {
					contact = contact;
				} else if(isPhoneWithZero) {
					contact = contact.substring(1, contact.length());
				}
				if(contact.length()==10) {
					contact = "91" + contact;
				}
				boolean resend = true;
				if(contact.length() == 12) {
					ContactEntity contactEntity = contactRepo.findByMobileNumberAndCityAndCategory(contact, city, category);
					if(contactEntity == null) {
						contactEntity = new ContactEntity();
						contactEntity.setMobileNumber(contact);
						contactEntity.setCity(city.toUpperCase());
						contactEntity.setCategory(category.toUpperCase());
					}
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
		boolean isContinue = true;
	//	int pageCount = 0;
	//	do {
			//String url = apiUrl + instanceId + "/messagesHistory?token=" + token + "&page=" + pageCount;
			long maxTime = (Double.valueOf((Math.floor(new Date().getTime() / 1000)))).longValue();
			
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, -daysBefore);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			long minTime = (Double.valueOf(Math.floor(cal.getTimeInMillis()/ 1000))).longValue();
			
			String url = apiUrl + instanceId + "/messages?limit=0&token=" + token + "&min_time=" + minTime + "&max_time=" + maxTime;
			System.out.println("URL : " + url);
			String response = restTemplate.exchange(url, HttpMethod.GET, null, String.class).getBody();
			// System.out.println(response);
			try {
				GetMessagesResponse responseObj = mapper.readValue(response, GetMessagesResponse.class);
				if (responseObj != null && !responseObj.getMessages().isEmpty()) {
				//	pageCount++;
					messages.addAll(getValidResponses(city, category, responseObj.getMessages()));
				} /*else {
					isContinue = false;
					break;
				}*/
			} catch (JsonProcessingException ex) {
				System.out.println("Error while parsing response : " + ex);
			}
	//	} while(isContinue);
		return messages;
	}

	private List<MessageInfo> getValidResponses(String city, String category, List<MessageInfo> messages) {
		System.out.println("Filtering valid messages -> " + messages.size());
		
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
					if (contact.getLastMessageReceivedTime() == null ||
							contact.getLastMessageReceivedTime().before(responseTime)) {
						contact.setLastMessageReceivedTime(new Date(response.getTime()));
						update = true;
					}
					if (contact.getMessageSentCount() == null || contact.getMessageSentCount() > 0) {
						contact.setMessageSentCount(0);
						update = true;
					}
					if (update) {
						contactRepo.saveAndFlush(contact);
					}
				} catch (Exception ex) {
					System.out.println("Error while storing response time : " + ex.toString());
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
		System.out.println("9822976424".startsWith("91"));
		
		String contact = "7588037827";
		contact = contact.replaceAll("[()\\s-]+", "");
		contact = contact.replace("+", "");
		Pattern p1 = Pattern.compile("(91)?[6-9][0-9]{9}");
		Pattern p2 = Pattern.compile("(0)?[6-9][0-9]{9}");
		Matcher m1 = p1.matcher(contact);
		Matcher m2 = p2.matcher(contact);
		boolean isPhoneWithNineOne = (m1.find() && m1.group().equals(contact));
		boolean isPhoneWithZero = (m2.find() && m2.group().equals(contact));
		if(contact.length() == 10) {
			contact = "91" + contact;
		} else if(isPhoneWithNineOne) {
			contact = contact;
		} else if(isPhoneWithZero) {
			contact = contact.substring(1, contact.length());
		}
		if(contact.length()==10) {
			contact = "91" + contact;
		}
		System.out.println(contact);
		
		long currTime = (Double.valueOf((Math.floor(new Date().getTime() / 1000)))).longValue();
		System.out.println(currTime);
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -3);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		long prevTime = (Double.valueOf(Math.floor(cal.getTimeInMillis()/ 1000))).longValue();
		System.out.println(prevTime);
		
		MessageInfo mObj1 = new MessageInfo();
		mObj1.setTime(currTime);
		MessageInfo mObj2 = new MessageInfo();
		mObj2.setTime(prevTime);
		
		List<MessageInfo> list = new ArrayList<MessageInfo>();
		list.add(mObj2);
		list.add(mObj1);
		System.out.println("before -> " + list);
		list.sort((MessageInfo m11, MessageInfo m12)->Long.compare(m12.getTime(), m11.getTime())); 
		System.out.println("after -> " + list);
	}

	@Override
	public Map<String, List<MessageInfo>> getPositiveMessages(List<MessageInfo> messageInfos) {
		if (Objects.nonNull(messageInfos) && !messageInfos.isEmpty()) {
			Map<String, List<MessageInfo>> messageInfoMap = new HashMap<>();
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
							List<MessageInfo> messageInfoList = messageInfoMap
									.getOrDefault(messageInfo.getChatIdMobileNumber(), new ArrayList<>());
							messageInfoList.add(messageInfo);
							messageInfoMap.putIfAbsent(messageInfo.getChatIdMobileNumber(), messageInfoList);
						}
					//}
				}
			}
			for (Map.Entry<String,List<MessageInfo>> entry : messageInfoMap.entrySet()) {
				if(!CollectionUtils.isEmpty(entry.getValue())) {
					entry.getValue().sort((MessageInfo m11, MessageInfo m12)->Long.compare(m12.getTime(), m11.getTime())); 
				}
			}
			return messageInfoMap;
		}
		return Collections.emptyMap();
	}

	@Override
	public List<String> getCityList() {
		List<String> cityList = contactRepo.findAllDistinctCity();
		return cityList;
	}

	@Override
	public List<String> getCategoryList() {
		List<String> categoryList = categoryMessageRepo.findAllDistinctCategory();
		return categoryList;
	}
}
