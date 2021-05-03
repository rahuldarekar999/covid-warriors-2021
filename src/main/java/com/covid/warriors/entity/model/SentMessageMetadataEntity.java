package com.covid.warriors.entity.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="sent_msg_metadata")
public class SentMessageMetadataEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(name = "from_mob")
	private String from;
	
	@Column(name = "category")
	private String category;
	
	@Column(name = "to_mob")
	private String to;
	
	@Column(name="sent_on")
	private Date sentOn;
	
	@Column(name="forward_flag")
	private Boolean isForward;
	
	@Column(name="subscribed")
	private Boolean subscribed;
	
	@Column(name="city")
	private String city;
	
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}
	
	public void setTo(String to) {
		this.to = to;
	}

	public Date getSentOn() {
		return sentOn;
	}

	public void setSentOn(Date sentOn) {
		this.sentOn = sentOn;
	}	

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Boolean getIsForward() {
		return isForward;
	}

	public void setIsForward(Boolean isForward) {
		this.isForward = isForward;
	}

	public Boolean getSubscribed() {
		return subscribed;
	}

	public void setSubscribed(Boolean subscribed) {
		this.subscribed = subscribed;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	@Override
	public String toString() {
		return "SentMessageMetadataEntity [id=" + id + ", from=" + from + ", category=" + category + ", to=" + to
				+ ", sentOn=" + sentOn + "]";
	}
}
