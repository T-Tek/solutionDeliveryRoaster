package com.solutiondeliveryroaster.solutiondelivery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SolutiondeliveryApplication {

	public static void main(String[] args) {
		SpringApplication.run(SolutiondeliveryApplication.class, args);
	}

}
