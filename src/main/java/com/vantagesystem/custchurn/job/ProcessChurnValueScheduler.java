package com.vantagesystem.custchurn.job;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.vantagesystem.custchurn.repository.ClientDetailsRepository;
import com.vantagesystem.custchurn.repository.EmployeeDetailsRepository;
import com.vantagesystem.custchurn.service.CustomerChurnService;

@Service
public class ProcessChurnValueScheduler {

	private static final Logger logger = LogManager.getLogger(ProcessChurnValueScheduler.class);
	
	private final CustomerChurnService customerChurnService;
	 private final ClientDetailsRepository clientDetailsRepository;
	 private final EmployeeDetailsRepository employeeDetailsRepository;
	 
	 public ProcessChurnValueScheduler(CustomerChurnService customerChurnService, ClientDetailsRepository clientDetailsRepository, EmployeeDetailsRepository employeeDetailsRepository)
	 {
		 this.customerChurnService = customerChurnService;
		 this.clientDetailsRepository = clientDetailsRepository;
		 this.employeeDetailsRepository = employeeDetailsRepository;
	 }
	 
	 
	 
	 @Scheduled(cron = "0 5 0 * * SUN")
	 public void weeklyLoginHistoryScheduler() {
		 logger.info("Scheduled weekly login history update started");
		 customerChurnService.updateWeeklyLoginHistory();
		 logger.info("Scheduled weekly login history update completed");
	    }
	 
	 
	 @Scheduled(cron = "0 0 3 * * SUN")
	 public void calculateAndUpdateChurnValue()
	 { 
		 logger.info("Scheduled churn calculation started");
		 customerChurnService.calculateAndUpdateChurnValue();
		 logger.info("Scheduled churn calculation completed");
	 }
	 
	 
	 @Scheduled(cron = "0 0 9 * * MON")
	 public void generateClientChurnReport()
	 { 
		 logger.info("Scheduled churn report generation started");
		 customerChurnService.generateChurnReport();
		 logger.info("Scheduled churn report generation completed");
	 }
	 
}
