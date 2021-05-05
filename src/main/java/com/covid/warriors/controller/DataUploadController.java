package com.covid.warriors.controller;


import com.covid.warriors.service.CovidWarriorsService;
import com.covid.warriors.service.OcrService;
import com.covid.warriors.service.impl.CovidWarriorServiceImpl;

import com.google.i18n.phonenumbers.PhoneNumberMatch;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@CrossOrigin
public class DataUploadController {

    @Autowired
    private CovidWarriorsService covidWarriorService;

    @Autowired
    private OcrService ocrService;

    @RequestMapping(value = "/upload/contact", method = RequestMethod.GET)
    public String uploadContactData(@RequestParam String path) {
        return covidWarriorService.uploadContactData(path);
    }

    @RequestMapping(value = "/vision/text/upload", method = RequestMethod.PUT)
    public Set<String> uploadImageForTextExtraction(@RequestParam("file") MultipartFile file) throws InterruptedException {
        Set<String> phoneNumbers = new HashSet<>();
        if(!file.isEmpty()) {
            String location = ocrService.uploadImaageForProcessing(file);
            phoneNumbers = ocrService.getOcrParsedContents(location);
        }
        return phoneNumbers;
    }
}
