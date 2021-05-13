package com.covid.warriors.service;

import com.covid.warriors.entity.model.MessageResponseEntity;
import com.covid.warriors.repository.MessageResponseRepository;
import com.covid.warriors.service.impl.UrlService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class MessageResponseService {

    @Autowired
    private UrlService urlService;

    @Autowired
    private MessageResponseRepository messageResponseRepository;

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
            messageResponseEntity.setCategory(paramsMap.get("ct"));
            messageResponseEntity.setCity(paramsMap.get("c"));
            messageResponseEntity.setMessage(paramsMap.get("a"));
            messageResponseEntity.setMobile(paramsMap.get("m"));
            messageResponseEntity.setCreatedAt(LocalDateTime.now());
            return messageResponseEntity;
        }
        return null;
    }
}
