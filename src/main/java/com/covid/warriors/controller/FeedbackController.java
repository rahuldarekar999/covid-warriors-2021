package com.covid.warriors.controller;

import com.covid.warriors.request.model.FeedbackRequest;
import com.covid.warriors.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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
