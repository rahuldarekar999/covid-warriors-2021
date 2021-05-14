package com.covid.warriors.request.model;

public class Message {
	private String Number;
	private String Text;

	// Getter Methods

	public String getNumber() {
		return Number;
	}

	public String getText() {
		return Text;
	}

	// Setter Methods

	public void setNumber(String Number) {
		this.Number = Number;
	}

	public void setText(String Text) {
		this.Text = Text;
	}

	@Override
	public String toString() {
		return "Message [Number=" + Number + ", Text=" + Text + "]";
	}
	
	
}
