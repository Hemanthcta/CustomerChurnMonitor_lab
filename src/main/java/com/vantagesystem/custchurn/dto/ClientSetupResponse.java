package com.vantagesystem.custchurn.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientSetupResponse {

    private String name;

    private String clientIdentifier;
    
    private String result;
    
    private String resultMsg;
}