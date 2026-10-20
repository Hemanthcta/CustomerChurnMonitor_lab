package com.vantagesystem.custchurn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class CustomerChurnMonitorApplication {

	private static final Logger logger = LogManager.getLogger(CustomerChurnMonitorApplication.class);

	public static void main(String[] args) {
		logger.info("Starting Customer Churn Monitor");
		SpringApplication.run(CustomerChurnMonitorApplication.class, args);
		logger.info("Customer Churn Monitor started");
	}

}
