package com.covid.warriors.service;

import java.util.List;
import java.util.Map;

import com.covid.warriors.request.model.CustomMessage;
import com.covid.warriors.request.model.ResponseMessage;
import com.covid.warriors.response.model.MessageInfo;

public interface CovidWarriorsService {

	String sendMessage(String city, String category);

	List<MessageInfo> getResponses(String city, String category, int daysToMinus);

	Map<String, List<MessageInfo>> getPositiveMessages(List<MessageInfo> messages, String city, String category, boolean updateContact);

	String sendMessageCustom(CustomMessage customMessage);

	List<String> getCityList();

	List<String> getCategoryList();

	long getMinTime(int daysToMinus);

	String getMessageForCategory(String category);

	void saveDataForSentMessages(CustomMessage customMessage, List<String> validNumberList);

	String forwardMessage(List<ResponseMessage> messages);

	String uploadContactData(String path);

	int getCountOfValidNumberByCityAndCategory(String city, String category);

	void saveDataForSentMessagesFromSocialMedia(CustomMessage customMessage);

}
