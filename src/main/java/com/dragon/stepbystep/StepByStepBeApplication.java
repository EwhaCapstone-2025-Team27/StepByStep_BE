package com.dragon.stepbystep;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
//@EnableJpaAuditing
public class StepByStepBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(StepByStepBeApplication.class, args);
	}

}
