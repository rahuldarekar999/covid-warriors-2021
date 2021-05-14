package com.covid.warriors.service;

import java.util.List;

public interface CovidWarriorsSmsService {

	String sendSms(List<String> mobileList, String msg);

	String sendSms(List<String> mobileList, String city, String category, String subCat, String from);
	
}
