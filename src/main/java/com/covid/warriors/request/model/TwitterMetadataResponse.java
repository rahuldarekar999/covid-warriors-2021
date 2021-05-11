package com.covid.warriors.request.model;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TwitterMetadataResponse {
	private String html;
	private Set<String> mobileList;
	
	
	public Set<String> getMobileList() {
		return mobileList;
	}
	public void setMobileList(Set<String> mobileList) {
		this.mobileList = mobileList;
	}
	public String getHtml() {
		return html;
	}
	public void setHtml(String html) {
		this.html = html;
	}
}
