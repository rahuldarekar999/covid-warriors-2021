package com.covid.warriors.request.model;

import java.util.List;

public class CustomMessage {
    private String city;
    private String category;
    private String message;
    private List<String> mobileList;
    private String from;
    private Object obj;
    private boolean isForward;
    
    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getMobileList() {
        return mobileList;
    }

    public void setMobileList(List<String> mobileList) {
        this.mobileList = mobileList;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

	public Object getObj() {
		return obj;
	}

	public void setObj(Object obj) {
		this.obj = obj;
	}

	public boolean isForward() {
		return isForward;
	}

	public void setForward(boolean isForward) {
		this.isForward = isForward;
	}  
}
