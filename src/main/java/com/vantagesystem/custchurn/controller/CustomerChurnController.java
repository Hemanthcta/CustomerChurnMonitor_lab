package com.vantagesystem.custchurn.controller;

import jakarta.validation.Valid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vantagesystem.custchurn.dto.AddEmployeeRequest;
import com.vantagesystem.custchurn.dto.AddEmployeeResponse;
import com.vantagesystem.custchurn.dto.ClientSetupRequest;
import com.vantagesystem.custchurn.dto.ClientSetupResponse;
import com.vantagesystem.custchurn.dto.UpdateLoginCountRequest;
import com.vantagesystem.custchurn.service.AccountSetupService;
import com.vantagesystem.custchurn.service.AddEmployeeService;
import com.vantagesystem.custchurn.service.LoginCountService;

@RestController
@RequestMapping("/customerchurn")
public class CustomerChurnController {

	private static final Logger logger = LogManager.getLogger(CustomerChurnController.class);

	private final AccountSetupService accountSetupService;
	private final AddEmployeeService addEmpService;
	private final LoginCountService loginCntService;
	
	public CustomerChurnController(AccountSetupService accountSetupService, AddEmployeeService addEmployeeService, LoginCountService loginCntService) {
        this.accountSetupService = accountSetupService;
        this.addEmpService = addEmployeeService;
        this.loginCntService = loginCntService;
    }
	
	@PostMapping("/addAccount")
	public ClientSetupResponse setupCustAccount(@Valid @RequestBody ClientSetupRequest request)
	{
		logger.info("Received request to create a client account : " + request.getName());
		return accountSetupService.createAccount(request);
	}
	
	@PostMapping("/addEmployee")
	public AddEmployeeResponse addEmployee(@Valid @RequestBody AddEmployeeRequest request)
	{
		logger.info("Received request to add an employee : " + request.getEmployeeId());
		return addEmpService.addEmployee(request);
	}
	
	
	@PostMapping("/updateLoginCount")
	public ResponseEntity<Void> updateLoginCount(@Valid @RequestBody UpdateLoginCountRequest request)
	{ 
		logger.info("Received request to update an employee login count");
		loginCntService.updateLoginCount(request);
		
		return ResponseEntity.accepted().build();
	}

}
