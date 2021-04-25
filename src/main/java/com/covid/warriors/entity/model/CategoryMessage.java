package com.covid.warriors.entity.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "CATEGORY_MESSAGE")
public class CategoryMessage {

	@Column(name = "category")
	@Id
	private String category;
	
	@Column(name="message")
	private String message;

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
	
	

	@Override
	public String toString() {
		return "CategoryMessage [category=" + category + ", message=" + message + "]";
	}
}
