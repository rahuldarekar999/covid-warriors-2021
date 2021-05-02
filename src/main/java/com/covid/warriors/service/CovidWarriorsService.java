package com.covid.warriors.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.covid.warriors.request.model.CustomMessage;
import com.covid.warriors.response.model.MessageInfo;

public interface CovidWarriorsService {

	String sendMessage(String city, String category);

	List<MessageInfo> getResponses(String city, String category);

	Map<String, List<MessageInfo>> getPositiveMessages(List<MessageInfo> messages);

	String sendMessageCustom(CustomMessage customMessage);

	List<String> getCityList();

	List<String> getCategoryList();

	long getMinTime();

	String getMessageForCategory(String category);

}
