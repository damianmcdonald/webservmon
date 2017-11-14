package com.github.damianmcdonald.webservmon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WebservmonApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebservmonApplication.class, args);
	}
}
