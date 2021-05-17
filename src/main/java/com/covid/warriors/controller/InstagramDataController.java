package com.covid.warriors.controller;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.covid.warriors.service.InstagramScraperService;

@RestController
@CrossOrigin
public class InstagramDataController {

    @Autowired
    private InstagramScraperService instagramScraperService;

    @RequestMapping(value = "/data/insta", method = RequestMethod.GET)
    public Set<String> getNumbersFromInstagram(@RequestParam String city, @RequestParam String category) {
        return instagramScraperService.getContactsFromInstagram(city.toLowerCase(), category.toLowerCase());
    }
}
