package com.covid.warriors.controller;


import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.covid.warriors.service.CovidWarriorsService;
import com.covid.warriors.service.OcrService;

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
    public Set<String> uploadImageForTextExtraction(@RequestParam("file") List<MultipartFile> files)  {
        Set<String> phoneNumbers = new HashSet<>();
        if(Objects.nonNull(files) && !files.isEmpty()) {
            files.forEach(file -> {
                String location = ocrService.uploadImaageForProcessing(file);
                phoneNumbers.addAll(ocrService.getOcrParsedContents(location));
            });
        }
        return phoneNumbers;
    }
}
