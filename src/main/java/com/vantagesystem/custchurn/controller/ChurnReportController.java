package com.vantagesystem.custchurn.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vantagesystem.custchurn.dto.ClientSetupRequest;
import com.vantagesystem.custchurn.dto.ClientSetupResponse;
import com.vantagesystem.custchurn.service.CustomerChurnService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/churnreport")
public class ChurnReportController {

	private final CustomerChurnService customerChurnService;
	
	public ChurnReportController(CustomerChurnService customerChurnService)
	{
		this.customerChurnService = customerChurnService;
	}
	
	@PostMapping("/calculateChurnPerc")
	public ResponseEntity<Void> calculateChurnPerc()
	{
		 customerChurnService.calculateAndUpdateChurnValue();
		 return ResponseEntity.accepted().build();
	}
	
	@PostMapping("/generateChurnReport")
	public ResponseEntity<Void> generateChurnReport()
	{
		 customerChurnService.generateChurnReport();
		 return ResponseEntity.accepted().build();
	}
}
