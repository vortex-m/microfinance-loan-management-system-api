package com.microfinance.loan.officer.entity;

import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.common.enums.OfficerStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "officer_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfficerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private Users users;


    @Column(nullable = false, unique = true)
    private String officerCode;

    private String designation;
    private String department;
    private String branch;
    private String branchCode;


    private LocalDate joiningDate;
    private String employeeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfficerStatus officerStatus;


    private Integer totalLoansReviewed;
    private Integer totalApproved;
    private Integer totalRejected;
    private Integer totalReVerified;
    private Double approvalRate;

    private Integer flaggedDecisions;   // flag by manager
    private LocalDateTime lastAuditedAt;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.officerStatus = OfficerStatus.ACTIVE;
        this.totalLoansReviewed = 0;
        this.totalApproved = 0;
        this.totalRejected = 0;
        this.totalReVerified = 0;
        this.flaggedDecisions = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}