package com.vantagesystem.custchurn.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateLoginCountRequest {

	@NotBlank
	private String clientIdentifier;
	
	@NotBlank
	private String employeeId;
	
	@NotNull
    private LocalDateTime loginDateTime;
}
