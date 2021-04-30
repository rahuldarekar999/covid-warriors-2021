package com.covid.warriors.entity.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "CONTACT")
public class ContactEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(name="mobile")
	private String mobileNumber;
	
	@Column(name="city")
	private String city;
	
	@Column(name="state")
	private String state;
	
	@Column(name="pin_code")
	private String pinCode;
	
	@Column(name="category")
	private String category;
	
	@Column(name="last_message_sent_time")
	private Date lastMessageSentTime;
	
	@Column(name="last_message_received_time")
	private Date lastMessageReceivedTime;
	
	@Column(name="whats_app_exist")
	private Boolean whatsAppExist;
	
	@Column(name="message_sent_count")
	private Integer messageSentCount;
	
	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getPinCode() {
		return pinCode;
	}

	public void setPinCode(String pinCode) {
		this.pinCode = pinCode;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Date getLastMessageSentTime() {
		return lastMessageSentTime;
	}

	public void setLastMessageSentTime(Date lastMessageSentTime) {
		this.lastMessageSentTime = lastMessageSentTime;
	}

	public Date getLastMessageReceivedTime() {
		return lastMessageReceivedTime;
	}

	public void setLastMessageReceivedTime(Date lastMessageReceivedTime) {
		this.lastMessageReceivedTime = lastMessageReceivedTime;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Boolean isWhatsAppExist() {
		return whatsAppExist;
	}

	public void setWhatsAppExist(Boolean whatsAppExist) {
		this.whatsAppExist = whatsAppExist;
	}

	public Integer getMessageSentCount() {
		return messageSentCount;
	}

	public void setMessageSentCount(Integer messageSentCount) {
		this.messageSentCount = messageSentCount;
	}

	public Boolean getWhatsAppExist() {
		return whatsAppExist;
	}

	@Override
	public String toString() {
		return "Contact [mobileNumber=" + mobileNumber + ", city=" + city + ", state=" + state + ", pinCode=" + pinCode
				+ ", category=" + category + ", lastMessageSentTime=" + lastMessageSentTime
				+ ", lastMessageReceivedTime=" + lastMessageReceivedTime + "]";
	}
	
}
