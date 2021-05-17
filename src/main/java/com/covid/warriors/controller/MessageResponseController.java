package com.covid.warriors.controller;

import java.util.HashMap;
import java.util.Map;

import com.covid.warriors.request.model.ConfirmationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.covid.warriors.entity.model.MessageResponseEntity;
import com.covid.warriors.service.MessageResponseService;

@RestController
@CrossOrigin
public class MessageResponseController {

    @Autowired
    private MessageResponseService messageResponseService;

    @RequestMapping(value = "/confirm", method = RequestMethod.POST)
    public Map<String, String> saveMessageResponse(@RequestBody ConfirmationRequest confirmationRequest) {
        String status = messageResponseService.saveMessageResponse(confirmationRequest);
        Map<String, String> resMap = new HashMap<>(1);
        resMap.put("status", status);
        return resMap;
    }
    
    @RequestMapping(value = "/user-metadata", method = RequestMethod.GET)
    public Map<String, MessageResponseEntity> getUserMetadata(@RequestParam String p) {
    	Map<String, MessageResponseEntity> reesponseMap = messageResponseService.getUserMetadata(p);
        return reesponseMap;
    }
}
