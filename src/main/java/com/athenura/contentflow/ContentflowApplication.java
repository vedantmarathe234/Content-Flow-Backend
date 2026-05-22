package com.athenura.contentflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ContentflowApplication {

	public static void main(String[] args) {
		SpringApplication.run(ContentflowApplication.class, args);
	}

}
