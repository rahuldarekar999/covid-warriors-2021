package com.covid.warriors.response.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CheckPhoneResponse {

		private String result;

		public String getResult() {
			return result;
		}

		public void setResult(String result) {
			this.result = result;
		}

		@Override
		public String toString() {
			return "CheckPhoneReponse [result=" + result + "]";
		}
}
