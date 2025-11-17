package com.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication(scanBasePackages = "com")
@EnableMongoRepositories(basePackages = "com.repository")
public class SpringMiniProject1Application {

	public static void main(String[] args) {
		SpringApplication.run(SpringMiniProject1Application.class, args);
	}

}
