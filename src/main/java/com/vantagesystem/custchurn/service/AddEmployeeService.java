package com.vantagesystem.custchurn.service;

import org.springframework.stereotype.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vantagesystem.custchurn.domain.EmployeeDetails;
import com.vantagesystem.custchurn.dto.AddEmployeeRequest;
import com.vantagesystem.custchurn.dto.AddEmployeeResponse;
import com.vantagesystem.custchurn.repository.EmployeeDetailsRepository;

@Service
public class AddEmployeeService {

	private static final Logger logger = LogManager.getLogger(AddEmployeeService.class);
	
	private final EmployeeDetailsRepository empRepo;
	
	public AddEmployeeService(EmployeeDetailsRepository empRepo)
	{
		this.empRepo = empRepo;
	}
	
	
	public AddEmployeeResponse addEmployee(AddEmployeeRequest request)
	{
		AddEmployeeResponse resp = null;
		try {
			EmployeeDetails empDtls = employeeDtlsMapper(request);
			empRepo.save(empDtls);
			logger.info("Added employee to client {}", request.getClientIdentifier());
		}catch(Exception e)
		{
			logger.error("Failed to add employee to client {}", request.getClientIdentifier(), e);
			resp = new AddEmployeeResponse();
			resp.setResult("Failure");
			resp.setResultMsg(e.getMessage());
			return resp;
		}
		resp = new AddEmployeeResponse();
		resp.setResult("SUCCESS");
		return resp;
	}
	
	
	private EmployeeDetails employeeDtlsMapper(AddEmployeeRequest request)
	{
		EmployeeDetails empDtls = new EmployeeDetails();
		
		empDtls.setClientIdentifier(request.getClientIdentifier());
		empDtls.setEmployeeId(request.getEmployeeId());
		empDtls.setLastLoginDateTime(request.getLastLoginDateTime());
		return empDtls;
	}

}
