package com.vantagesystem.custchurn.service;



import org.springframework.stereotype.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vantagesystem.custchurn.domain.ClientDetails;
import com.vantagesystem.custchurn.dto.ClientSetupRequest;
import com.vantagesystem.custchurn.dto.ClientSetupResponse;
import com.vantagesystem.custchurn.repository.ClientDetailsRepository;


@Service
public class AccountSetupService {

	private static final Logger logger = LogManager.getLogger(AccountSetupService.class);

	private final ClientDetailsRepository clientDetailsRepository;
	
	public AccountSetupService(ClientDetailsRepository clientDetailsRepository) {
        this.clientDetailsRepository = clientDetailsRepository;
    }
	
	public ClientSetupResponse createAccount(ClientSetupRequest inpReq)
	{
		ClientDetails clientDtls = clientDetailsMapper(inpReq);
		ClientSetupResponse resp = null;
		
		try {
			clientDetailsRepository.save(clientDtls);
			logger.info("Created client account with identifier {}", clientDtls.getClientIdentifier());
		}catch(Exception ex)
		{
			logger.error("Failed to create client account", ex);
			resp = new ClientSetupResponse();
			resp.setResult("FAILURE");
			resp.setResultMsg(ex.getMessage());
			return resp;
		}
		
		resp = new ClientSetupResponse();
		resp.setClientIdentifier(clientDtls.getClientIdentifier());
		resp.setName(clientDtls.getName());
		resp.setResult("SUCCESS");
		return resp;
	}
	
	
	private ClientDetails clientDetailsMapper(ClientSetupRequest clientSetupRequest)
	{
		ClientDetails clientDtls = new ClientDetails();
		
		clientDtls.setClientIdentifier("C"+ String.valueOf(System.currentTimeMillis() / 1000));
		clientDtls.setName(clientSetupRequest.getName());
		clientDtls.setLicenseStartDateTime(clientSetupRequest.getLicenseStartDateTime());
		clientDtls.setLicenseEndDateTime(clientSetupRequest.getLicenseEndDateTime());
		
		clientDtls.setAddress(clientSetupRequest.getClientAddress());
		clientDtls.setPhoneNumber(clientSetupRequest.getPhoneNumber());
		
		return clientDtls;
		
	}
}
