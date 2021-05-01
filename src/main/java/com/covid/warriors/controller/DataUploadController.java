package com.covid.warriors.controller;

import com.covid.warriors.impl.CovidWarriorServiceImpl;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@RestController
public class DataUploadController {

    @Autowired
    private CovidWarriorServiceImpl covidWarriorService;

    @RequestMapping(value = "/upload/contact", method = RequestMethod.GET)
    public String uploadContactData(@RequestParam String path) {
        return covidWarriorService.uploadContactData(path);
    }
}
