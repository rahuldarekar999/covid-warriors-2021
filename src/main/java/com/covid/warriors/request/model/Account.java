package com.covid.warriors.request.model;

public class Account {
	private String APIKey;
	private String SenderId;
	private String Channel;
	private String DCS;
	private String Route;

	
	public String getSenderId() {
		return SenderId;
	}

	public String getChannel() {
		return Channel;
	}

	public String getDCS() {
		return DCS;
	}

	public String getRoute() {
		return Route;
	}

	public void setSenderId(String SenderId) {
		this.SenderId = SenderId;
	}

	public void setChannel(String Channel) {
		this.Channel = Channel;
	}

	public void setDCS(String DCS) {
		this.DCS = DCS;
	}

	public void setRoute(String Route) {
		this.Route = Route;
	}

	public String getAPIKey() {
		return APIKey;
	}

	public void setAPIKey(String aPIKey) {
		APIKey = aPIKey;
	}

	@Override
	public String toString() {
		return "Account [APIKey=" + APIKey + ", SenderId=" + SenderId + ", Channel=" + Channel + ", DCS=" + DCS
				+ ", Route=" + Route + "]";
	}
}
