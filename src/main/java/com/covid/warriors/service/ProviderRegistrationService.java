package com.covid.warriors.service;

import com.covid.warriors.entity.model.ContactEntity;
import com.covid.warriors.repository.ContactRepository;
import com.covid.warriors.request.model.ProviderRequest;
import com.covid.warriors.utils.ContactUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProviderRegistrationService {

    @Autowired
    private ContactRepository contactRepository;

    public boolean registerProvider(ProviderRequest providerRequest) {
        try {
            String contact = ContactUtil.getFormattedContact(providerRequest.getMobile());
            if(StringUtils.isNotBlank(contact) && contact.length() == 12) {
                ContactEntity contactEntity = new ContactEntity();
                contactEntity.setMobileNumber(contact);
                contactEntity.setCity(providerRequest.getCity().toUpperCase());
                contactEntity.setCategory(providerRequest.getCategory().toUpperCase());
                contactEntity.setSource("WEBSITE");
                contactRepository.save(contactEntity);
                return Boolean.TRUE;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Boolean.FALSE;
    }
}
