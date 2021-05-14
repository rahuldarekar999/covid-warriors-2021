package com.covid.warriors.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.covid.warriors.service.MessageResponseService;

@RestController
@CrossOrigin
public class MessageResponseController {

    @Autowired
    private MessageResponseService messageResponseService;

    @RequestMapping(value = "/confirm", method = RequestMethod.PUT)
    public Map<String, String> saveMessageResponse(@RequestParam String p) {
        String status = messageResponseService.saveMessageResponse(p);
        Map<String, String> resMap = new HashMap<>(1);
        resMap.put("status", status);
        return resMap;
    }
}
