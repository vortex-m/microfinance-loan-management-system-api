package com.microfinance.loan.user.entity;

import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.common.enums.DisbursalMode;
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
    private String applicationNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    // Loan Request Details
    @Column(nullable = false)
    private Double requestedAmount;

    @Column(nullable = false)
    private Integer tenureMonths;



    @Column(nullable = false)
    private String loanPurpose;

    private String loanPurposeDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DisbursalMode disbursalMode;

    // Bank details are captured per loan when BANK_TRANSFER is chosen.
    private String disbursalBankName;
    private String disbursalBankAccount;
    private String disbursalIfscCode;
    private String disbursalBankProofUrl;
    private String disbursalBankProofFileName;

    // Approved Details (filled after approval)
    private Double approvedAmount;
    private Double interestRate;
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

    // Cash handover OTP for CASH disbursal mode.
    private String cashDisbursalOtpHash;

    @Enumerated(EnumType.STRING)
    private com.microfinance.loan.common.enums.CashOtpStatus cashDisbursalOtpStatus;

    private Integer cashDisbursalOtpAttempts;
    private LocalDateTime cashDisbursalOtpRequestedAt;
    private LocalDateTime cashDisbursalOtpExpiresAt;
    private LocalDateTime cashDisbursalOtpVerifiedAt;

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
        if (this.disbursalMode == null) {
            this.disbursalMode = DisbursalMode.BANK_TRANSFER;
        }
        if (this.cashDisbursalOtpAttempts == null) {
            this.cashDisbursalOtpAttempts = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}