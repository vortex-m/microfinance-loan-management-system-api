package com.microfinance.loan.manager.entity;

import com.microfinance.loan.common.entity.Users;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "manager_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManagerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private Users users;


    @Column(nullable = false, unique = true)
    private String managerCode;

    private String designation;
    private String department;
    private String branch;
    private String branchCode;
    private String region;


    private LocalDate joiningDate;
    private String employeeId;


    private String accessLevel;
    private Boolean canExportReports;
    private Boolean canAcknowledgeFraud;


    private LocalDateTime lastLoginAt;
    private LocalDateTime lastReportGeneratedAt;
    private Integer totalAuditsPerformed;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.canExportReports = true;
        this.canAcknowledgeFraud = true;
        this.totalAuditsPerformed = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}