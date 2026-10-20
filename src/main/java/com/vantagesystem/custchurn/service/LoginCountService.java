package com.vantagesystem.custchurn.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vantagesystem.custchurn.dto.UpdateLoginCountRequest;
import com.vantagesystem.custchurn.dto.UpdateLoginCountResponse;
import com.vantagesystem.custchurn.repository.EmployeeDetailsRepository;

@Service
public class LoginCountService {

private static final Logger logger = LogManager.getLogger(LoginCountService.class);

private final EmployeeDetailsRepository empRepo;
	
	public LoginCountService(EmployeeDetailsRepository empRepo)
	{
		this.empRepo = empRepo;
	}
	
	@Async
	public void updateLoginCount(UpdateLoginCountRequest request)
	{
		int updated = 0;
		try {
		updated = empRepo.updateLoginCount(request.getClientIdentifier(), request.getEmployeeId(), request.getLoginDateTime());
		if(updated == 0) {
			logger.warn("No employee login count was updated for the supplied client and employee identifiers");
			throw new Exception("Incorrect clientID or employeeID");
		}
		}catch(Exception e)
		{
			logger.error("Failed to update employee login count", e);
			return;
		}
		logger.info("Updated login count for {} employee record(s)", updated);
	}
	
	

}
