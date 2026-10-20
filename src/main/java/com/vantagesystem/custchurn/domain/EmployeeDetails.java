package com.vantagesystem.custchurn.domain;


import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "employee_details")
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmployeeDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "client_identifier", nullable = false)
    private String clientIdentifier;

    @Column(name = "employee_id", nullable = false)
    private String employeeId;

    @Column(name = "create_date", nullable = false)
    private LocalDateTime createDate;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @Column(name = "login_count_per_day")
    private Integer weeklyLoginCount;

    @Column(name = "last_login_datetime")
    private LocalDateTime lastLoginDateTime;

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