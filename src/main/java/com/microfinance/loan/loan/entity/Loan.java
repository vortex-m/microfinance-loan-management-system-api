package com.microfinance.loan.loan.entity;

import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.common.enums.LoanStatus;
import com.microfinance.loan.user.entity.LoanApplication;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "loans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String loanNumber;              // e.g. LN-2024-00123

    // Linked to original application
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_application_id", nullable = false, unique = true)
    private LoanApplication loanApplication;

    // Borrower
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    // Backward-compatible final approver field.
    // Use managerApprovedBy for explicit two-step flow.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private Users approvedBy;

    // Two-step approval metadata
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "officer_approved_by")
    private Users officerApprovedBy;

    private LocalDateTime officerApprovedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_approved_by")
    private Users managerApprovedBy;

    private LocalDateTime managerApprovedAt;
    private String managerRejectionReason;

    // Agent who verified
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by_agent")
    private Users verifiedByAgent;

    // Loan Financial Details
    @Column(nullable = false)
    private Double principalAmount;         // approved loan amount

    @Column(nullable = false)
    private Double interestRate;            // annual interest rate %

    @Column(nullable = false)
    private String interestType;            // FLAT, REDUCING_BALANCE

    @Column(nullable = false)
    private Integer tenureMonths;           // total EMIs

    @Column(nullable = false)
    private Double emiAmount;              // monthly EMI

    private Double processingFee;
    private Double totalInterestPayable;
    private Double totalAmountPayable;      // principal + interest

    // Loan Purpose
    private String loanPurpose;             // BUSINESS, EDUCATION, MEDICAL etc.

    // Status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus loanStatus;

    // EMI Tracking
    @Column(nullable = false)
    private Integer totalEmis;              // same as tenureMonths
    private Integer emisPaid;
    private Integer emisPending;
    private Integer emisOverdue;

    // Amount Tracking
    private Double totalPaidAmount;
    private Double outstandingPrincipal;
    private Double outstandingInterest;
    private Double totalPenaltyCharged;
    private Double totalPenaltyPaid;

    // Dates
    @Column(nullable = false)
    private LocalDate disbursementDate;

    @Column(nullable = false)
    private LocalDate firstEmiDate;

    @Column(nullable = false)
    private LocalDate lastEmiDate;          // expected closure date

    private LocalDate actualClosureDate;    // filled when CLOSED

    // Disbursal Info
    private String disbursalBankAccount;    // user's bank account
    private String disbursalBankName;
    private String disbursalIfscCode;
    private String disbursalTransactionRef; // bank transaction reference
    private LocalDateTime disbursedAt;

    // Closure Info
    private String closureType;             // COMPLETED, FORECLOSED, WRITTEN_OFF
    private LocalDateTime closedAt;

    // Flags
    private Boolean isNpa;                  // Non Performing Asset
    private Boolean isFraudFlagged;
    private LocalDateTime npaSince;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.loanStatus = LoanStatus.PENDING_MANAGER_APPROVAL;
        this.emisPaid = 0;
        this.emisOverdue = 0;
        this.totalPaidAmount = 0.0;
        this.totalPenaltyCharged = 0.0;
        this.totalPenaltyPaid = 0.0;
        this.isNpa = false;
        this.isFraudFlagged = false;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}