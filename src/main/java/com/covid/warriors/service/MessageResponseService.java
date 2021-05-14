package com.covid.warriors.service;

import java.time.LocalDateTime;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.covid.warriors.entity.model.MessageResponseEntity;
import com.covid.warriors.repository.MessageResponseRepository;
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
    
    public String saveMessageResponse(String param) {
        try {
            String decodedParams = urlService.getOriginalUrl(param);
            MessageResponseEntity messageResponseEntity = getMessageResponseEntity(decodedParams);
            if(Objects.nonNull(messageResponseEntity)) {
                messageResponseRepository.save(messageResponseEntity);
                return "SUCCESS";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "FAILED";
    }

    private MessageResponseEntity getMessageResponseEntity(String params) {
        if(StringUtils.isNotBlank(params)) {
            Map<String, String> paramsMap = new HashMap<>();
            String[] pairs = params.split("&");
            for(String pair : pairs) {
                String[] value = pair.split("=");
                paramsMap.put(value[0], value[1]);
            }
            MessageResponseEntity messageResponseEntity= new MessageResponseEntity();
            messageResponseEntity.setCategory(paramsMap.get("c"));
            messageResponseEntity.setCity(paramsMap.get("ct"));
            messageResponseEntity.setMessage(paramsMap.get("a"));
            messageResponseEntity.setMobile(paramsMap.get("m"));
            messageResponseEntity.setCreatedAt(LocalDateTime.now());
            messageResponseEntity.setSubCategory(paramsMap.get("sc"));
            if(StringUtils.isNotBlank(messageResponseEntity.getMessage()) && 
            		validAnswers.contains(messageResponseEntity.getMessage().toLowerCase())) {
	            String msg = smsReplyMessage;
	            msg = msg.replace("!cat!", messageResponseEntity.getCategory()).replace("!mob!", messageResponseEntity.getMobile())
	            		.replace("!city!", messageResponseEntity.getCity());


	            covidWarriorSmsServiceImpl.sendSms(Collections.singletonList(paramsMap.get("f")), msg);
            }
            return messageResponseEntity;
        }
        return null;
    }
}
