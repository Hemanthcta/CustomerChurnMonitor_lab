package com.vantagesystem.custchurn.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ClientSetupRequest {

	@NotBlank
    private String name;

	@NotNull
    private LocalDateTime licenseStartDateTime;

	@NotNull
    private LocalDateTime licenseEndDateTime;
	
	private String clientAddress;
	
	private String phoneNumber;
}