package com.vantagesystem.custchurn.job;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vantagesystem.custchurn.domain.ClientDetails;
import com.vantagesystem.custchurn.repository.ClientDetailsRepository;
import com.vantagesystem.custchurn.repository.EmployeeDetailsRepository;
import com.vantagesystem.custchurn.service.CustomerChurnService;

@Service
public class ProcessChurnValueScheduler {

	private static final Logger logger = LogManager.getLogger(ProcessChurnValueScheduler.class);

	// Job1 : Weekly job : Sun 12am : To count the total login of employee and add it total weekly count list. Then reset the employee daily login count to 0.
	// Job2: Calculate the Churn.
	
	private final CustomerChurnService customerChurnService;
	 private final ClientDetailsRepository clientDetailsRepository;
	 private final EmployeeDetailsRepository employeeDetailsRepository;
	 
	 public ProcessChurnValueScheduler(CustomerChurnService customerChurnService, ClientDetailsRepository clientDetailsRepository, EmployeeDetailsRepository employeeDetailsRepository)
	 {
		 this.customerChurnService = customerChurnService;
		 this.clientDetailsRepository = clientDetailsRepository;
		 this.employeeDetailsRepository = employeeDetailsRepository;
	 }
	 
	 
	 
	 @Scheduled(cron = "0 12 19 * * *")
	 public void weeklyLoginHistoryScheduler() {
		 logger.info("Scheduled weekly login history update started");
		 customerChurnService.updateWeeklyLoginHistory();
		 logger.info("Scheduled weekly login history update completed");
	    }
	 
	 
	 @Scheduled(cron = "* 26 15 * * *")
	 public void calculateAndUpdateChurnValue()
	 { 
		 logger.info("Scheduled churn calculation started");
		 customerChurnService.calculateAndUpdateChurnValue();
		 logger.info("Scheduled churn calculation completed");
	 }
	 
	 
	 @Scheduled(cron = "* 44 16 * * *")
	 public void generateClientChurnReport()
	 { 
		 logger.info("Scheduled churn report generation started");
		 customerChurnService.generateChurnReport();
		 logger.info("Scheduled churn report generation completed");
	 }
	 
}
