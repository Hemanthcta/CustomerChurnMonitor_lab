package com.vantagesystem.custchurn.controller;

import jakarta.validation.Valid;

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
		return accountSetupService.createAccount(request);
	}
	
	@PostMapping("/addEmployee")
	public AddEmployeeResponse addEmployee(@Valid @RequestBody AddEmployeeRequest request)
	{
		return addEmpService.addEmployee(request);
	}
	
	
	@PostMapping("/updateLoginCount")
	public ResponseEntity<Void> updateLoginCount(@Valid @RequestBody UpdateLoginCountRequest request)
	{ 
		loginCntService.updateLoginCount(request);
		
		return ResponseEntity.accepted().build();
	}

}
