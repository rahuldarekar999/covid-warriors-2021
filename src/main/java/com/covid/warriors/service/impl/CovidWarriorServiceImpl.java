package com.covid.warriors.service.impl;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.covid.warriors.entity.model.*;
import com.covid.warriors.repository.*;
import org.apache.commons.lang3.StringUtils;
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

import com.covid.warriors.request.model.CustomMessage;
import com.covid.warriors.request.model.MessageRequest;
import com.covid.warriors.request.model.ResponseMessage;
import com.covid.warriors.request.model.TwitterMetadataResponse;
import com.covid.warriors.response.model.CheckPhoneResponse;
import com.covid.warriors.response.model.GetMessagesResponse;
import com.covid.warriors.response.model.MessageInfo;
import com.covid.warriors.response.model.SendMessageResponse;
import com.covid.warriors.service.CovidWarriorsService;
import com.covid.warriors.service.CovidWarriorsSmsService;
import com.covid.warriors.service.DataScraperService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.twilio.Twilio;

@Service
public class CovidWarriorServiceImpl implements CovidWarriorsService {

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

	@Value("${resend.wait.min}")
	private int resendWaitMin;

	@Value("${stop.message.sent.count}")
	private int stopMessageSentCount;
	
	@Value("${days.before}")
	private int daysBefore;
	
	@Value("${forward.feature}")
	private boolean forwardFeatureOn;
	
	@Value("${twitter.feature}")
	private boolean twitterFeatureOn;
		
	@Value("${message.response}")
	private String responseMessage;

	@Value("${subscription.message1}")
	private String subscriptionMessage1; 
	
	@Value("${subscription.message2}")
	private String subscriptionMessage2; 
	
	@Value("${forward.before}")
	private int forwardBefore;
	
	@Value("${forward.message.limit}")
	private int forwardMsgLimit;
	
	@Value("${send.help.message}")
	private String helpMessageText;
	
	@Value("${twitter.send.notification.message}")
	private String twitterNotificationMessage;

	
//	@Value("#{'${valid.response.black.list}'.split(',')}")
	@Value("#{'${valid.response.black.list}'.split(',')}")
	private List<String> blackListOfResponses;

	@Value("${sms.notification.message}")
	private String smsNotification;
	
	@Value("#{'${valid.response.black.list.forward}'.split(',')}")
	private List<String> blackListOfForwardResponses;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private ContactRepository contactRepo;

	@Autowired
	private CategoryMessageRepository categoryMessageRepo;
	
	@Autowired
	private SentMessageMetadataRepository sentMetadataMessageRepo;
	
	@Autowired
	private AsyncCovidWarriorServiceImpl asyncCovidWarriorServiceImpl;

	@Autowired
	private CityRepository cityRepository;
	
	@Autowired
	private DataScraperService dataScraperService;
	
	@Autowired
	private CovidWarriorsSmsService covidWarriorSmsServiceImpl;

	@Value("${response.message.daysBefore}")
	private Long responseDaysBefore;

	@Autowired
	private MessageResponseRepository messageResponseRepository;

	@Override
	public String sendMessage(String city, String category) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		List<ContactEntity> contacts = contactRepo.findTop250ByCityAndCategoryAndValidOrderByLastMessageReceivedTimeDesc(city, category, true);
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
	@Async
	public String sendMessageCustom(CustomMessage customMessage) {
		if (!CollectionUtils.isEmpty(customMessage.getMobileList())) {
			Set<String> distinctNumbers = new LinkedHashSet<String>();  
			customMessage.getMobileList().forEach(contact -> {
				distinctNumbers.add(getPhoneNumber(contact));
			});			
			distinctNumbers.forEach(contact -> {
				String message = prepareMessage(customMessage.getCity(), customMessage.getCategory(), getPhoneNumber(customMessage.getFrom()), customMessage.getSubCat());
				asyncCovidWarriorServiceImpl.sendAsyncMessage(contact, customMessage.getCity(), customMessage.getCategory(), message);
			});
			if(StringUtils.isNotBlank(customMessage.getFrom())) {
				saveDataForSentMessages(customMessage, customMessage.getMobileList());
			}
			return "Message Successfully Sent";
		}
		return "No Data for given City And Category";
	}
	
	@Override
	@Async
	public void saveDataForSentMessages(CustomMessage customMessage, List<String> validNumberList) {
		try {
			if(customMessage.getFrom() != null) {
				String mobile = getPhoneNumber(customMessage.getFrom());
				SentMessageMetadataEntity entity = sentMetadataMessageRepo.findByFromAndCityAndCategory(mobile, customMessage.getCity(), customMessage.getCategory());
				boolean subscribeUser = true;
				if(entity == null) {
					entity = new SentMessageMetadataEntity();
				}
				if(entity.getSubscribed() != null && Boolean.compare(entity.getSubscribed().booleanValue(), customMessage.isSubscribed()) == 0) {
					subscribeUser = false;
					String customMessageForHelp = prepareMessage(entity.getCity(), entity.getCategory(), entity.getFrom(), customMessage.getSubCat());
					List<ContactEntity> entiryList = contactRepo.findTop250ByCityAndCategoryAndValidOrderByLastMessageReceivedTimeDesc(entity.getCity(), entity.getCategory(), true);
					List<ContactEntity> masterList = new ArrayList<>();
					/*if("MEDICINE".equalsIgnoreCase(entity.getCategory())) {
						List<ContactEntity> hospitalList = contactRepo.findByCityAndCategoryAndValid(entity.getCity(), "BED", true);
						if(!CollectionUtils.isEmpty(hospitalList)) {
							masterList.addAll(hospitalList);
						}
					}*/
					if(!CollectionUtils.isEmpty(entiryList)) {
						masterList.addAll(entiryList);
					}
					asyncCovidWarriorServiceImpl.sendMessageToAll(masterList, customMessageForHelp);
				}
				if(!customMessage.isSubscribed()) {
					String to = String.join(",", validNumberList);
					entity.setTo(to);
					entity.setFrom(mobile);
					entity.setCategory(customMessage.getCategory() != null ? customMessage.getCategory().toUpperCase():"");
					entity.setCity(customMessage.getCity() != null ? customMessage.getCity().toUpperCase() : "'");
					// entity.setSentOn(new Date());
					entity.setIsForward(customMessage.isForward());
					entity.setSubscribed(true);
					entity = sentMetadataMessageRepo.saveAndFlush(entity);
				} else if(subscribeUser){
					entity.setFrom(mobile);
					entity.setCategory(customMessage.getCategory() != null ? customMessage.getCategory().toUpperCase():"");
					entity.setCity(customMessage.getCity() != null ? customMessage.getCity().toUpperCase() : "'");
					// entity.setSentOn(new Date());
					entity.setIsForward(customMessage.isForward());
					entity.setSubscribed(customMessage.isSubscribed());
					entity = sentMetadataMessageRepo.saveAndFlush(entity);
					if(entity != null) {
						MessageRequest request1 = new MessageRequest();
						String messageStr = subscriptionMessage1;
						messageStr = messageStr.replace("!city!", entity.getCity()).replace("!cat!", entity.getCategory());
						request1.setBody(messageStr);
						request1.setPhone(Long.valueOf(entity.getFrom()));
						
						forwardMessageToNumber(request1);
						MessageRequest request2 = new MessageRequest();
						messageStr = subscriptionMessage2;
						request2.setBody(messageStr);
						request2.setPhone(Long.valueOf(entity.getFrom()));
						forwardMessageToNumber(request2);
						
						List<MessageInfo> messages = getResponses(entity.getCity(), entity.getCategory(), forwardBefore);
						System.out.println("message count " + messages.size());
						messages = getPositiveMessagesForForward(messages);
						int msgCounter = 0;
						for(MessageInfo msg : messages){
							if(msgCounter < forwardMsgLimit) {
								MessageRequest request = new MessageRequest();
								messageStr = responseMessage;
								String receivedFrom = msg.getChatId() != null ? msg.getChatId().substring(0, msg.getChatId().indexOf("@")) : null;
								messageStr = messageStr.replaceAll("!mob!", receivedFrom).replace("!city!", entity.getCity()).replace("!cat!", entity.getCategory()).replace("!msg!", msg.getBody());
								request.setBody(messageStr);
								request.setPhone(Long.valueOf(entity.getFrom()));
								asyncCovidWarriorServiceImpl.forwardMessageToNumber(request);
								msgCounter++;
							}
						}
						String customMessageForHelp = prepareMessage(entity.getCity(), entity.getCategory(), entity.getFrom(), customMessage.getSubCat());
						List<ContactEntity> entiryList = contactRepo.findTop250ByCityAndCategoryAndValidOrderByLastMessageReceivedTimeDesc(entity.getCity(), entity.getCategory(), true);
						List<ContactEntity> masterList = new ArrayList<>();
						/*if("MEDICINE".equalsIgnoreCase(entity.getCategory())) {
							List<ContactEntity> hospitalList = contactRepo.findByCityAndCategoryAndValid(entity.getCity(), "BED", true);
							if(!CollectionUtils.isEmpty(hospitalList)) {
								masterList.addAll(hospitalList);
							}
						}*/
						if(!CollectionUtils.isEmpty(entiryList)) {
							masterList.addAll(entiryList);
						}
						asyncCovidWarriorServiceImpl.sendMessageToAll(masterList, customMessageForHelp);
					}
					
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("Exception while saving data " + ex);
		}
	}

	@Override
	@Async
	public void sendSms(CustomMessage customMessage, List<String> validNumberList) {
		try {
			if(customMessage.getFrom() != null) {
				String mobile = getPhoneNumber(customMessage.getFrom());
				SentMessageMetadataEntity entity = sentMetadataMessageRepo.findByFromAndCityAndCategory(mobile, customMessage.getCity(), customMessage.getCategory());
				boolean subscribeUser = true;
				if(entity == null) {
					entity = new SentMessageMetadataEntity();
				}
				
				if(entity.getSubscribed() != null && Boolean.compare(entity.getSubscribed().booleanValue(), customMessage.isSubscribed()) == 0) {
					subscribeUser = false;
					List<String> entiryList = contactRepo.findMobileByCityAndCategoryAndValid(customMessage.getCity(), customMessage.getCategory(), true);
					List<String> masterList = new ArrayList<>();
					/*if("MEDICINE".equalsIgnoreCase(entity.getCategory())) {
						List<ContactEntity> hospitalList = contactRepo.findByCityAndCategoryAndValid(entity.getCity(), "BED", true);
						if(!CollectionUtils.isEmpty(hospitalList)) {
							masterList.addAll(hospitalList);
						}
					}*/
					if(!CollectionUtils.isEmpty(entiryList)) {
						masterList.addAll(entiryList);
					}
					if(!CollectionUtils.isEmpty(masterList)) {
						String smsMsg = smsNotification;
						smsMsg = smsMsg.replace("!count!", masterList.size()+"");
						List<String> fromList = new ArrayList<>();
						fromList.add(mobile);
						covidWarriorSmsServiceImpl.sendSms(fromList, smsMsg);
						System.out.println("Sending messages to : " + masterList.size());
						covidWarriorSmsServiceImpl.sendSms(masterList, customMessage.getCity(), customMessage.getCategory(), customMessage.getSubCat(), mobile);
					}
				}
				if(!customMessage.isSubscribed()) {
					
					String smsMsg = smsNotification;
					smsMsg = smsMsg.replace("!count!", validNumberList.size()+"");
					List<String> fromList = new ArrayList<>();
					fromList.add(mobile);
					covidWarriorSmsServiceImpl.sendSms(fromList, smsMsg);
					covidWarriorSmsServiceImpl.sendSms(validNumberList, customMessage.getCity(), customMessage.getCategory(), customMessage.getSubCat(), mobile);
					String to = String.join(",", validNumberList);
					entity.setTo(to);
					entity.setFrom(mobile);
					
					entity.setCategory(customMessage.getCategory() != null ? customMessage.getCategory().toUpperCase():"");
					entity.setCity(customMessage.getCity() != null ? customMessage.getCity().toUpperCase() : "'");
					// entity.setSentOn(new Date());
					entity.setIsForward(customMessage.isForward());
					entity.setSubscribed(true);
					entity = sentMetadataMessageRepo.saveAndFlush(entity);
				} else if(subscribeUser){
					entity.setFrom(mobile);
					entity.setCategory(customMessage.getCategory() != null ? customMessage.getCategory().toUpperCase():"");
					entity.setCity(customMessage.getCity() != null ? customMessage.getCity().toUpperCase() : "'");
					// entity.setSentOn(new Date());
					entity.setIsForward(customMessage.isForward());
					entity.setSubscribed(customMessage.isSubscribed());
					entity = sentMetadataMessageRepo.saveAndFlush(entity);
					if(entity != null) {
						/*MessageRequest request1 = new MessageRequest();
						String messageStr = subscriptionMessage1;
						messageStr = messageStr.replace("!city!", entity.getCity()).replace("!cat!", entity.getCategory());
						request1.setBody(messageStr);
						request1.setPhone(Long.valueOf(entity.getFrom()));
						
						forwardMessageToNumber(request1);
						MessageRequest request2 = new MessageRequest();
						messageStr = subscriptionMessage2;
						request2.setBody(messageStr);
						request2.setPhone(Long.valueOf(entity.getFrom()));
						forwardMessageToNumber(request2);
						
						List<MessageInfo> messages = getResponses(entity.getCity(), entity.getCategory(), forwardBefore);
						System.out.println("message count " + messages.size());
						messages = getPositiveMessagesForForward(messages);
						int msgCounter = 0;
						for(MessageInfo msg : messages){
							if(msgCounter < forwardMsgLimit) {
								MessageRequest request = new MessageRequest();
								messageStr = responseMessage;
								String receivedFrom = msg.getChatId() != null ? msg.getChatId().substring(0, msg.getChatId().indexOf("@")) : null;
								messageStr = messageStr.replaceAll("!mob!", receivedFrom).replace("!city!", entity.getCity()).replace("!cat!", entity.getCategory()).replace("!msg!", msg.getBody());
								request.setBody(messageStr);
								request.setPhone(Long.valueOf(entity.getFrom()));
								asyncCovidWarriorServiceImpl.forwardMessageToNumber(request);
								msgCounter++;
							}
						}*/
						List<String> entiryList = contactRepo.findMobileByCityAndCategoryAndValid(customMessage.getCity(), customMessage.getCategory(), true);
						List<String> masterList = new ArrayList<>();
						/*if("MEDICINE".equalsIgnoreCase(entity.getCategory())) {
							List<ContactEntity> hospitalList = contactRepo.findByCityAndCategoryAndValid(entity.getCity(), "BED", true);
							if(!CollectionUtils.isEmpty(hospitalList)) {
								masterList.addAll(hospitalList);
							}
						}*/
						if(!CollectionUtils.isEmpty(entiryList)) {
							masterList.addAll(entiryList);
						}
						if(!CollectionUtils.isEmpty(masterList)) {
							String smsMsg = smsNotification;
							smsMsg = smsMsg.replace("!count!", masterList.size()+"");
							List<String> fromList = new ArrayList<>();
							fromList.add(mobile);
							covidWarriorSmsServiceImpl.sendSms(fromList, smsMsg);
							System.out.println("Sending messages to : " + masterList.size());
							covidWarriorSmsServiceImpl.sendSms(masterList, customMessage.getCity(), customMessage.getCategory(), customMessage.getSubCat(), mobile);
						}
					}
					
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("Exception while saving data " + ex);
		}
	}	

	private List<MessageInfo> getPositiveMessagesForForward(List<MessageInfo> messages) {
		List<MessageInfo> validMessages = new ArrayList<>();
		for (MessageInfo messageInfo : messages) {
			if (StringUtils.isNotBlank(messageInfo.getBody())) {
					blackListOfForwardResponses.stream().forEach(msg -> {
						if (StringUtils.contains(messageInfo.getBody().toLowerCase(), msg)) {
							messageInfo.setValid(false);
							validMessages.add(messageInfo);
						}
					});
			}
		}
		return validMessages;
	}

	private String prepareMessage(String city, String category, String from, String subCatText) {
		String messageStr = helpMessageText;
		return messageStr.replaceAll("!mob!", from).replace("!cat!", subCatText);
	}

	@Override
	public List<MessageInfo> getResponses(String city, String category, int daysToMinus) {
		/*HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		List<MessageInfo> messages = new ArrayList<MessageInfo>();
		boolean isContinue = true;
	//	int pageCount = 0;
	//	do {
			//String url = apiUrl + instanceId + "/messagesHistory?token=" + token + "&page=" + pageCount;
			long maxTime = (Double.valueOf((Math.floor(new Date().getTime() / 1000)))).longValue();
			
			long minTime = getMinTime(daysToMinus);
			String url = apiUrl + instanceId + "/messages?limit=0&token=" + token + "&min_time=" + minTime + "&max_time=" + maxTime;
			System.out.println("URL : " + url);
			String response = restTemplate.exchange(url, HttpMethod.GET, null, String.class).getBody();
			// System.out.println(response);
			try {
				GetMessagesResponse responseObj = mapper.readValue(response, GetMessagesResponse.class);
				if (responseObj != null && !responseObj.getMessages().isEmpty()) {
				//	pageCount++;
					messages.addAll(getValidResponses(city, category, responseObj.getMessages()));
				} *//*else {
					isContinue = false;
					break;
				}*//*
			} catch (JsonProcessingException ex) {
				System.out.println("Error while parsing response : " + ex);
			}
	//	} while(isContinue);
		return messages;*/
		return getResponseMessages(city, category);
	}

	private List<MessageInfo> getResponseMessages(String city, String category) {
		List<MessageResponseEntity> messageResponseEntities = messageResponseRepository
				.findByCreatedAtAfterAndMessageAndCityAndCategory(LocalDateTime.now().minusDays(responseDaysBefore)
						,"y",city, category);
		if(Objects.nonNull(messageResponseEntities) && !messageResponseEntities.isEmpty()) {
			List<MessageInfo> responseMessages = messageResponseEntities.stream()
					.map(message -> {
						MessageInfo messageInfo = new MessageInfo();
						messageInfo.setTime(message.getCreatedAt().atZone(ZoneId.systemDefault())
								.toInstant().toEpochMilli());
						messageInfo.setBody("y".equals(message.getMessage()) ? "Yes" : "");
						messageInfo.setChatIdMobileNumber(message.getMobile());
						return messageInfo;
					}).collect(Collectors.toList());
			return responseMessages;
		}
		return Collections.emptyList();
	}

	@Override
	public long getMinTime(int daysToMinus) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -daysToMinus);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		long minTime = (Double.valueOf(Math.floor(cal.getTimeInMillis()/ 1000))).longValue();
		
		return minTime;
	}

	private List<MessageInfo> getValidResponses(String city, String category, List<MessageInfo> messages) {
		System.out.println("Filtering valid messages -> " + messages.size());
		messages.sort((MessageInfo m11, MessageInfo m12)->Long.compare(m12.getTime(), m11.getTime())); 
	
		List<ContactEntity> contacts = contactRepo.findByCityAndCategory(city, category);
		List<String> validNumbers = contacts.stream().filter(c -> (city.equalsIgnoreCase(c.getCity()) 
				&& category.equalsIgnoreCase(c.getCategory()))).map(ContactEntity::getMobileNumber).collect(Collectors.toList());

		List<MessageInfo> validResponses = messages.stream()
				.filter(m -> (validNumbers.contains(m.getChatIdMobileNumber()) && !m.isFromMe()))
				.collect(Collectors.toList());
		if (!CollectionUtils.isEmpty(validResponses)) {
			validResponses.forEach(response -> {
				try {
				//	System.out.println("Contact checking : " + response.getChatIdMobileNumber());
					ContactEntity contact = contactRepo
							.findByMobileNumberAndCityAndCategoryAndValid(response.getChatIdMobileNumber(), city, category, true);
					Date responseTime = new Date(response.getTime());
					boolean update = false;
					if(contact != null) {
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
					}
				} catch (Exception ex) {
					ex.printStackTrace();
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
		cal.add(Calendar.HOUR, -6);
//		cal.set(Calendar.HOUR_OF_DAY, 0);
//		cal.set(Calendar.MINUTE, 0);0
//		cal.set(Calendar.SECOND, 0);
//		cal.set(Calendar.MILLISECOND, 0);
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
		
		List<String> numList = new ArrayList<>();
		numList.add("12345");
		numList.add("23423");
		String numListStr = String.join(",", numList);
		System.out.println(numListStr);
		
		String messageStr = "Response from  +!mob! (from https://indiafightbacks.com) \nCity      !city! Category  !cat! \nMessage !msg! \nhttps://wa.me/!mob!";
		
		messageStr = messageStr.replaceAll("!mob!", "123456").replace("!city!", "PUNE").replace("!cat!", "OXY").replace("!msg!", "TEST MSG");
		
		System.out.println("test -> " + messageStr);
	}

	@Override
	public Map<String, List<MessageInfo>> getPositiveMessages(List<MessageInfo> messageInfos, String city, String category, boolean updateContact) {
		if (Objects.nonNull(messageInfos) && !messageInfos.isEmpty()) {
			Map<String, List<MessageInfo>> messageInfoMap = new LinkedHashMap<>();
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
								if(updateContact) {
									try {
										ContactEntity contact = contactRepo.findByMobileNumberAndCityAndCategory(messageInfo.getChatIdMobileNumber(), city, category);
										contact.setValid(false);
										contactRepo.saveAndFlush(contact);
									} catch(Exception ex) {
										System.out.println("Exception while saving contact for invalid number : " + ex);
										ex.printStackTrace();
									}
								}
							}
					//		System.out.println("map -> " + messageInfo.isValid());
						});
						if(updateContact) {
							List<MessageInfo> messageInfoList = messageInfoMap
									.getOrDefault(messageInfo.getChatIdMobileNumber(), new ArrayList<>());
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

	@Override
	public List<String> getCityList() {
		List<String> cityList = cityRepository.findAll().stream().map(CityEntity::getCity)
		.collect(Collectors.toList());
		return cityList;
	}

	@Override
	public List<String> getCategoryList() {
		List<String> categoryList = categoryMessageRepo.findAllDistinctCategory();
		return categoryList;
	}

	public String uploadContactData(String path) {
		List<ContactEntity> contactEntities = new ArrayList<>();
		try (CSVReader reader = new CSVReader(new FileReader(path))) {
			List<String[]> rows = reader.readAll();
			rows.forEach(row -> {
				if(StringUtils.isNotBlank(row[1])) {
					String contact = row[1].replaceAll("[()\\s-]+", "");
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
					if(contact.length() == 12) {
						ContactEntity contactEntity = new ContactEntity();
						contactEntity.setMobileNumber(contact);
						contactEntity.setCategory(StringUtils.isEmpty(row[0]) ? "" : row[0].toUpperCase());
						contactEntity.setPinCode(row[2]);
						contactEntity.setCity(StringUtils.isEmpty(row[3]) ? "" : row[3].toUpperCase());
						contactEntity.setState(StringUtils.isEmpty(row[4]) ? "" : row[4].toUpperCase());
						contactEntity.setValid(Boolean.TRUE);
						contactEntities.add(contactEntity);
						if(contactEntities.size() == 10) {
							try {
								contactRepo.saveAll(contactEntities);
								contactEntities.clear();
							} catch (Exception e) {
								contactEntities.clear();
							}

						}
					}

				}
			});
			if(contactEntities.size() > 0)
				try {
					contactRepo.saveAll(contactEntities);
				} catch (Exception e) { }


		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CsvException e) {
			e.printStackTrace();
		}
		return "OK";
	}

	@Override
	public String getMessageForCategory(String category) {
		return categoryMessageRepo.findMessageByCategory(category);
	}


	@Override
	public String forwardMessage(List<ResponseMessage> messages) {
		if(forwardFeatureOn) {
			List<SentMessageMetadataEntity> listOfNumbersToForwardMessage = sentMetadataMessageRepo.findAllWhereForwardIsTrue();
			if(!CollectionUtils.isEmpty(listOfNumbersToForwardMessage)) {
				listOfNumbersToForwardMessage.forEach(forwardObj ->{
					if(forwardObj.getIsForward() != null && forwardObj.getIsForward()) {
						messages.forEach(message -> {
							
							String receivedFrom = message.getChatId() != null ? message.getChatId().substring(0, message.getChatId().indexOf("@")) : null;
							if(receivedFrom != null && !message.isFromMe()) {
								if(!forwardObj.getSubscribed()) {
									if(forwardObj.getTo() != null && forwardObj.getTo().contains(receivedFrom)) {
										String messageStr = responseMessage;
										MessageRequest request = new MessageRequest();
										messageStr = messageStr.replaceAll("!mob!", receivedFrom).replace("!city!", forwardObj.getCity()).replace("!cat!", forwardObj.getCategory()).replace("!msg!", message.getBody());
										request.setBody(messageStr + " \n");
										request.setPhone(Long.valueOf(forwardObj.getFrom()));
									//	System.out.println("frwd : " + request);
										forwardMessageToNumber(request);
									}
								} else {
									if("ALL".equalsIgnoreCase(forwardObj.getCategory())) {
										List<ContactEntity> entityList = contactRepo.findByValid(true);
										asyncCovidWarriorServiceImpl.forwardMessage(entityList, forwardObj, receivedFrom, message);
									} else {
										List<ContactEntity> entityList = contactRepo.findByCityAndCategoryAndValid(forwardObj.getCity(), forwardObj.getCategory(), true);
										asyncCovidWarriorServiceImpl.forwardMessage(entityList, forwardObj, receivedFrom, message);
										/*if(!CollectionUtils.isEmpty(entityList)) {
											entityList.forEach(entity -> {
												if(entity.getMobileNumber().equals(receivedFrom)) {
													MessageRequest request = new MessageRequest();
													String messageStr = responseMessage;
													messageStr = messageStr.replaceAll("!mob!", receivedFrom).replace("!city!", forwardObj.getCity()).replace("!cat!", forwardObj.getCategory()).replace("!msg!", message.getBody());
													request.setBody(messageStr);
													request.setPhone(Long.valueOf(forwardObj.getFrom()));
											//		System.out.println("sub : " + request);
													forwardMessageToNumber(request);	
												}
											});										
										}*/
									}
								}
							}
						});
						forwardObj.setSentOn(new Date());
						sentMetadataMessageRepo.saveAndFlush(forwardObj);
					}
				});
			}
				
		} else {
			System.out.println("Msg received : " + messages);
		}
		return "Response Forwatded";
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
	
	private String getPhoneNumber(String contact) {
		contact = contact.replaceAll("[()\\s-]+", "");
		contact = contact.trim().replaceAll(" ", "");
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

	@Override
	public int getCountOfValidNumberByCityAndCategory(String city, String category) {
		return contactRepo.countByCityAndCategoryAndValid(city, category, true);
	}

	@Override
	public int saveDataForSentMessagesFromSocialMedia(CustomMessage customMessage) {
		boolean useTwitterApi = true;
		if(!CollectionUtils.isEmpty(customMessage.getMobileList())) {
			useTwitterApi = false;
		}
		if(useTwitterApi) {	
			String searchText = StringUtils.isNotBlank(customMessage.getSubCat()) ? customMessage.getCategory() + " OR " + customMessage.getSubCat() : customMessage.getCategory();
			Set<String> contacts = dataScraperService.scrapeDataFromTwitterUrl(customMessage.getCity(), customMessage.getCategory(), searchText);
			customMessage.setMobileList(new ArrayList<String>(contacts));
		}
		if(twitterFeatureOn && !CollectionUtils.isEmpty(customMessage.getMobileList())) {
			try {
				if(customMessage.getFrom() != null) {
					
					String mobile = getPhoneNumber(customMessage.getFrom());
					
					MessageRequest request1 = new MessageRequest();
					String messageStr = twitterNotificationMessage;
					messageStr = messageStr.replaceAll("!count!", customMessage.getMobileList().size() + "");
					request1.setBody(messageStr);
					request1.setPhone(Long.valueOf(customMessage.getFrom()));
					
					forwardMessageToNumber(request1);
					
					SentMessageMetadataEntity entity = sentMetadataMessageRepo.findByFromAndCityAndCategory(mobile, customMessage.getCity(), customMessage.getCategory());
					if(entity == null) {
						entity = new SentMessageMetadataEntity();
					}
					
					//if(!customMessage.isSubscribed()) {
						String to = String.join(",", customMessage.getMobileList());
						entity.setTo(to);
						entity.setFrom(mobile);
						entity.setCategory(customMessage.getCategory() != null ? customMessage.getCategory().toUpperCase():"");
						entity.setCity(customMessage.getCity() != null ? customMessage.getCity().toUpperCase() : "'");
						// entity.setSentOn(new Date());
						entity.setIsForward(customMessage.isForward());
						entity.setSubscribed(true);
						entity = sentMetadataMessageRepo.saveAndFlush(entity);
						
						sendMessageCustom(customMessage);
						
						
						
				//	} 
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				System.out.println("Exception while saving data " + ex);
			}
		}
		return customMessage.getMobileList().size();
	}

	@Override
	public TwitterMetadataResponse getTwitterData(CustomMessage customMessage) {
		String searchText = StringUtils.isNotBlank(customMessage.getSubCat()) ? customMessage.getCategory() + " OR " + customMessage.getSubCat() : customMessage.getCategory();
		TwitterMetadataResponse twitterMetadataResponse = dataScraperService.scrapeDataFromTwitterUrlMetadata(customMessage.getCity(), customMessage.getCategory(), searchText);
		return twitterMetadataResponse;
	}
}
