package com.microfinance.loan.user.entity;

import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.common.enums.LoanStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "loan_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String applicationNumber;   // LOAN-2024-00123

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    // Loan Request Details
    @Column(nullable = false)
    private Double requestedAmount;

    @Column(nullable = false)
    private Integer tenureMonths;       // 6, 12, 24, 36 months

    @Column(nullable = false)
    private String loanPurpose;         // BUSINESS, EDUCATION, MEDICAL, HOME etc.

    private String loanPurposeDescription;

    // Approved Details (filled after approval)
    private Double approvedAmount;
    private Double interestRate;        // annual %
    private Double processingFee;
    private Double emiAmount;

    // Status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status;

    // Tracking
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_agent_id")
    private Users assignedAgent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_officer_id")
    private Users assignedOfficer;

    // Remarks
    private String userRemarks;
    private String officerRemarks;
    private String rejectionReason;

    // Disbursal Info
    private LocalDateTime disbursedAt;
    private String disbursalReference;

    // Closure Info
    private LocalDateTime closedAt;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = LoanStatus.PENDING;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}