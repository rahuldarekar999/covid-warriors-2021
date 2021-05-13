package com.covid.warriors.controller;

import com.covid.warriors.request.model.FeedbackRequest;
import com.covid.warriors.service.MessageResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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
