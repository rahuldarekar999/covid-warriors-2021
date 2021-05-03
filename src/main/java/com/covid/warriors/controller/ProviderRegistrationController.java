package com.covid.warriors.controller;

import com.covid.warriors.entity.model.ContactEntity;
import com.covid.warriors.request.model.ProviderRequest;
import com.covid.warriors.service.ProviderRegistrationService;
import com.covid.warriors.utils.ContactUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
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
