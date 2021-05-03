package com.covid.warriors.controller;


import com.covid.warriors.service.OcrService;
import com.covid.warriors.service.impl.CovidWarriorServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class DataUploadController {

    @Autowired
    private CovidWarriorServiceImpl covidWarriorService;

    @Autowired
    private OcrService ocrService;

    @RequestMapping(value = "/upload/contact", method = RequestMethod.GET)
    public String uploadContactData(@RequestParam String path) {
        return covidWarriorService.uploadContactData(path);
    }

    @RequestMapping(value = "/vision/text/upload", method = RequestMethod.PUT)
    public void uploadImageForTextExtraction(@RequestParam("file") MultipartFile file) {
        if(!file.isEmpty()) {
            ocrService.uploadImaageForProcessing(file);
        }
    }
}
