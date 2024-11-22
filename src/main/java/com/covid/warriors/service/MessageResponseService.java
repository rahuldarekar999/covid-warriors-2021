package com.covid.warriors.service;

import java.time.LocalDateTime;
import java.util.*;

import com.covid.warriors.request.model.ConfirmationRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.covid.warriors.entity.model.MessageResponseEntity;
import com.covid.warriors.entity.model.SentMessageMetadataEntity;
import com.covid.warriors.repository.MessageResponseRepository;
import com.covid.warriors.repository.SentMessageMetadataRepository;
import com.covid.warriors.service.impl.UrlService;

@Service
public class MessageResponseService {

	@Autowired
	private UrlService urlService;

	@Value("${sms.reply.message}")
	private String smsReplyMessage;

	@Value("#{'${valid.sms.response.list}'.split(',')}")
	private List<String> validAnswers;

	@Autowired
	private MessageResponseRepository messageResponseRepository;

	@Autowired
	private CovidWarriorsSmsService covidWarriorSmsServiceImpl;

	@Autowired
	private SentMessageMetadataRepository sentMetadataMessageRepo;
	
	public String saveMessageResponse(ConfirmationRequest confirmationRequest) {
		try {
			/*String decodedParams = urlService.getOriginalUrl(param);
			MessageResponseEntity messageResponseEntity = getMessageResponseEntity(decodedParams);*/
			MessageResponseEntity messageResponseEntity = new MessageResponseEntity();
			messageResponseEntity.setMobile(confirmationRequest.getMobile());
			messageResponseEntity.setCity(confirmationRequest.getCity());
			messageResponseEntity.setCategory(confirmationRequest.getCategory());
			messageResponseEntity.setMessage(confirmationRequest.getMessage());
			messageResponseEntity.setSubCategory(confirmationRequest.getSubCategory());
			messageResponseEntity.setCreatedAt(LocalDateTime.now());
			System.out.println("Reponse Received from : " + confirmationRequest.getMobile() + 
					" : response msg : " + confirmationRequest.getMessage());
			if (StringUtils.isNotBlank(messageResponseEntity.getMessage())
					&& validAnswers.contains(messageResponseEntity.getMessage().toLowerCase())) {
				String msg = smsReplyMessage;
				msg = msg.replace("!cat!", messageResponseEntity.getCategory() + "-" + confirmationRequest.getSubCategory())
						.replace("!mob!", messageResponseEntity.getMobile())
						.replace("!city!", messageResponseEntity.getCity());
				List<String> mobileList = sentMetadataMessageRepo.findOnlyMobileByCityAndCategory(messageResponseEntity.getCity(),
						messageResponseEntity.getCategory());
				if(!CollectionUtils.isEmpty(mobileList)) {
					System.out.println("Sending the notification for city : " + messageResponseEntity.getCity()
					 + " : category : " + messageResponseEntity.getCategory());
					System.out.println("Mobile List is : " + mobileList.size());
					covidWarriorSmsServiceImpl.sendSms(mobileList, msg);
				} else {
					System.out.println("no one subscribed for the notification for city : " + messageResponseEntity.getCity()
					 + " : category : " + messageResponseEntity.getCategory());
					
				}
			}
			messageResponseRepository.save(messageResponseEntity);
			return "SUCCESS";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "FAILED";
	}

	private MessageResponseEntity getMessageResponseEntity(String params) {
		if (StringUtils.isNotBlank(params)) {
			Map<String, String> paramsMap = new HashMap<>();
			String[] pairs = params.split("&");
			for (String pair : pairs) {
				String[] value = pair.split("=");
				paramsMap.put(value[0], value[1]);
			}
			MessageResponseEntity messageResponseEntity = new MessageResponseEntity();
			messageResponseEntity.setCategory(paramsMap.get("c"));
			messageResponseEntity.setCity(paramsMap.get("ct"));
			messageResponseEntity.setMessage(paramsMap.get("a"));
			messageResponseEntity.setMobile(paramsMap.get("m"));
			messageResponseEntity.setCreatedAt(LocalDateTime.now());
			messageResponseEntity.setSubCategory(paramsMap.get("sc"));
			if (StringUtils.isNotBlank(messageResponseEntity.getMessage())
					&& validAnswers.contains(messageResponseEntity.getMessage().toLowerCase())) {
				String msg = smsReplyMessage;
				msg = msg.replace("!cat!", messageResponseEntity.getCategory())
						.replace("!mob!", messageResponseEntity.getMobile())
						.replace("!city!", messageResponseEntity.getCity());

				covidWarriorSmsServiceImpl.sendSms(Collections.singletonList(paramsMap.get("f")), msg);
			}
			return messageResponseEntity;
		}
		return null;
	}

	public Map<String, MessageResponseEntity> getUserMetadata(String p) {
		String decodedParams = urlService.getOriginalUrlWithoutDelete(p);
		MessageResponseEntity messageResponseEntity = getMessageResponseEntityOnly(decodedParams);
		Map<String, MessageResponseEntity> responseMap = new HashMap<>();
		responseMap.put("data", messageResponseEntity);
		return responseMap;
	}

	private MessageResponseEntity getMessageResponseEntityOnly(String params) {
		MessageResponseEntity messageResponseEntity = new MessageResponseEntity();
		if (StringUtils.isNotBlank(params)) {
			Map<String, String> paramsMap = new HashMap<>();
			String[] pairs = params.split("&");
			for (String pair : pairs) {
				String[] value = pair.split("=");
				paramsMap.put(value[0], value[1]);
			}

			messageResponseEntity.setCategory(paramsMap.get("c"));
			messageResponseEntity.setCity(paramsMap.get("ct"));
			messageResponseEntity.setMobile(paramsMap.get("m"));
			messageResponseEntity.setSubCategory(paramsMap.get("sc"));
		}
		return messageResponseEntity;
	}
}
