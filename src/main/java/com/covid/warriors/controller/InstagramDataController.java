package com.covid.warriors.controller;

import com.covid.warriors.request.model.FeedbackRequest;
import com.covid.warriors.service.InstagramScraperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@CrossOrigin
public class InstagramDataController {

    @Autowired
    private InstagramScraperService instagramScraperService;

    @RequestMapping(value = "/data/insta", method = RequestMethod.GET)
    public Set<String> getNumbersFromInstagram(@RequestParam String city, @RequestParam String category) {
        return instagramScraperService.getContactsFromInstagram(city, category);
    }
}
