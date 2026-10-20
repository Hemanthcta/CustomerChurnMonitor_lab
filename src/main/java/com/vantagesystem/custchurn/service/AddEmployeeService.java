package com.vantagesystem.custchurn.service;

import org.springframework.stereotype.Service;

import com.vantagesystem.custchurn.domain.EmployeeDetails;
import com.vantagesystem.custchurn.dto.AddEmployeeRequest;
import com.vantagesystem.custchurn.dto.AddEmployeeResponse;
import com.vantagesystem.custchurn.repository.EmployeeDetailsRepository;

@Service
public class AddEmployeeService {
	
	private final EmployeeDetailsRepository empRepo;
	
	public AddEmployeeService(EmployeeDetailsRepository empRepo)
	{
		this.empRepo = empRepo;
	}
	
	
	public AddEmployeeResponse addEmployee(AddEmployeeRequest request)
	{
		EmployeeDetails empDtls = employeeDtlsMapper(request);
		AddEmployeeResponse resp = null;
		try {
			empRepo.save(empDtls);
		}catch(Exception e)
		{
			//Add logger
			resp = new AddEmployeeResponse();
			resp.setResult("Failure");
			resp.setResultMsg("Failed to add employee details");
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
