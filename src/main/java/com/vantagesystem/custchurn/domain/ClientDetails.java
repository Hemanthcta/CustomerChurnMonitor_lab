package com.vantagesystem.custchurn.domain;



import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "client_details")
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientDetails {

	 @Id
	 @GeneratedValue(strategy = GenerationType.IDENTITY)
	 @Column(name = "id")
	private Long id;
	
	@Column(name = "client_identifier", nullable = false, unique = true, length = 7)
	private String clientIdentifier;
	
	@Column(name = "name")
    private String name;

    @Column(name = "address")
    private String address;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "create_date", nullable = false, updatable = false)
    private LocalDateTime createDate;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @Column(name = "license_start_datetime")
    private LocalDateTime licenseStartDateTime;

    @Column(name = "license_end_datetime")
    private LocalDateTime licenseEndDateTime;

    @Column(name = "churn_rate_2_weeks", precision = 5, scale = 2)
    private BigDecimal churnRate2Weeks;

    @Column(name = "churn_rate_4_weeks", precision = 5, scale = 2)
    private BigDecimal churnRate4Weeks;
	
    @Column(name = "churn_rate_12_weeks", precision = 5, scale = 2)
    private BigDecimal churnRate12Weeks; 
    
    @Column(name = "login_weekly_history")
    private String loginWeeklyHistory;
    
    @PrePersist
    public void prePersist() {
        createDate = LocalDateTime.now();
        lastUpdateDate = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        lastUpdateDate = LocalDateTime.now();
    }
	
}
