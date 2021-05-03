package com.covid.warriors.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.covid.warriors.request.model.ProviderRequest;
import com.covid.warriors.service.ProviderRegistrationService;

@RestController
@CrossOrigin
public class ProviderRegistrationController {

    @Autowired
    private ProviderRegistrationService providerRegistrationService;

    @RequestMapping(value = "/providers", method = RequestMethod.POST)
    public String RegisterProvider(@RequestBody ProviderRequest providerRequest) {
        if(providerRegistrationService.registerProvider(providerRequest)) {
            return "{\"status\":\"SUCCESS\"}";
        }
        return "{\"status\":\"FAILED\"}";
    }
}
