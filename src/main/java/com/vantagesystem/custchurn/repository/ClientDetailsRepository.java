package com.vantagesystem.custchurn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vantagesystem.custchurn.domain.ClientDetails;


public interface ClientDetailsRepository extends JpaRepository<ClientDetails, Long>
{
	
}

