package com.covid.warriors.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.covid.warriors.request.model.FeedbackRequest;
import com.covid.warriors.service.FeedbackService;

@RestController
@CrossOrigin
public class FeedbackController {
    @Autowired
    private FeedbackService feedbackService;

    @RequestMapping(value = "/feedbacks", method = RequestMethod.POST)
    public Map<String, String> saveFeedback(@RequestBody FeedbackRequest feedbackRequest) {
        Map<String, String> responseMap = new HashMap<>(1);
        responseMap.put("status", feedbackService.saveFeedback(feedbackRequest));
        return responseMap;
    }

}
