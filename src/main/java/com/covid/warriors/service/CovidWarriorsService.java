package com.covid.warriors.service;

import java.util.List;

import com.covid.warriors.response.model.MessageInfo;

public interface CovidWarriorsService {

	String sendMessage(String city, String category);

	List<MessageInfo> getResponses(String city, String category);

}
