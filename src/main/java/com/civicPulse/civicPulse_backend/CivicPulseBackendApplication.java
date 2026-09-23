package com.civicPulse.civicPulse_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CivicPulseBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(CivicPulseBackendApplication.class, args);
	}

}
