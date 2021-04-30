package com.covid.warriors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class CovidWarriorsApplication {

	public static void main(String[] args) {
		SpringApplication.run(CovidWarriorsApplication.class, args);
	}	

	@Bean
	public RestTemplate getRestTemplate() {
		return new RestTemplate();
	}
}
