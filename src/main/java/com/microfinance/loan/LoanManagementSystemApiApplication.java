package com.microfinance.loan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LoanManagementSystemApiApplication {
	public static void main(String[] args) {
		SpringApplication.run(LoanManagementSystemApiApplication.class, args);
	}

}
