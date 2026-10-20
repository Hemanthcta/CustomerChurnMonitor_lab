package com.vantagesystem.custchurn.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vantagesystem.custchurn.dto.ClientSetupRequest;
import com.vantagesystem.custchurn.dto.ClientSetupResponse;
import com.vantagesystem.custchurn.service.CustomerChurnService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/churnreport")
public class ChurnReportController {

	private static final Logger logger = LogManager.getLogger(ChurnReportController.class);

	private final CustomerChurnService customerChurnService;
	
	public ChurnReportController(CustomerChurnService customerChurnService)
	{
		this.customerChurnService = customerChurnService;
	}
	
	@PostMapping("/calculateChurnPerc")
	public ResponseEntity<Void> calculateChurnPerc()
	{
		 logger.info("Received request to calculate client churn values");
		 customerChurnService.calculateAndUpdateChurnValue();
		 return ResponseEntity.accepted().build();
	}
	
	@PostMapping("/generateChurnReport")
	public ResponseEntity<Void> generateChurnReport()
	{
		 logger.info("Received request to generate the client churn report");
		 customerChurnService.generateChurnReport();
		 return ResponseEntity.accepted().build();
	}
}
