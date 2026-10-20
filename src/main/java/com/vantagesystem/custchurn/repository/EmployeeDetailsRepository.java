package com.vantagesystem.custchurn.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.vantagesystem.custchurn.domain.EmployeeDetails;


public interface EmployeeDetailsRepository extends JpaRepository<EmployeeDetails, Long>
{
	
	
	@Transactional
	@Modifying
	@Query("UPDATE EmployeeDetails SET lastLoginDateTime = :lastLoginDateTime, weeklyLoginCount = COALESCE(weeklyLoginCount, 0)+1 WHERE clientIdentifier = :clientIdentifier AND employeeId = :employeeId")
	int updateLoginCount(
	        @Param("clientIdentifier") String clientIdentifier,
	        @Param("employeeId") String employeeId,
	        @Param("lastLoginDateTime") LocalDateTime lastLoginDateTime);
	
	
	@Query("SELECT COALESCE(SUM(e.weeklyLoginCount), 0) FROM EmployeeDetails e WHERE e.clientIdentifier = :clientIdentifier")
	Integer getWeeklyLoginCount(@Param("clientIdentifier") String clientIdentifier);

	@Modifying
	@Transactional
	@Query("UPDATE EmployeeDetails e SET e.weeklyLoginCount = 0 WHERE e.clientIdentifier = :clientIdentifier")
	void resetWeeklyLoginCount(@Param("clientIdentifier") String clientIdentifier);
	
}
