package com.vantagesystem.custchurn.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.vantagesystem.custchurn.dto.UpdateLoginCountRequest;
import com.vantagesystem.custchurn.dto.UpdateLoginCountResponse;
import com.vantagesystem.custchurn.repository.EmployeeDetailsRepository;

@Service
public class LoginCountService {

private final EmployeeDetailsRepository empRepo;
	
	public LoginCountService(EmployeeDetailsRepository empRepo)
	{
		this.empRepo = empRepo;
	}
	
	@Async
	public void updateLoginCount(UpdateLoginCountRequest request)
	{
		UpdateLoginCountResponse updateLoginCountResponse = null;
		int updated = 0;
		try {
		updated = empRepo.updateLoginCount(request.getClientIdentifier(), request.getEmployeeId(), request.getLoginDateTime());
		if(updated == 0)
			throw new Exception("Incorrect clientID or employeeID");
		}catch(Exception e)
		{
			//Add logger
			
		}
		// Add Logger
	}
	
	

}
