package com.microfinance.loan.officer.entity;

import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.common.enums.ManagerDecision;
import com.microfinance.loan.common.enums.LoanStatus;
import com.microfinance.loan.common.enums.ReviewDecision;
import com.microfinance.loan.user.entity.LoanApplication;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "loan_reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String reviewCode;          // e.g. REV-00123

    // Which loan is being reviewed
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_application_id", nullable = false, unique = true)
    private LoanApplication loanApplication;

    // Which officer is reviewing
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "officer_id", nullable = false)
    private Users officer;

    // Checklist — what officer verified
    private Boolean kycVerified;
    private Boolean agentReportReviewed;
    private Boolean incomeVerified;
    private Boolean addressVerified;
    private Boolean aiScoreChecked;
    private Boolean fraudSignalsChecked;

    // AI Score at time of review (snapshot)
    private Double creditScoreAtReview;
    private Double riskScoreAtReview;
    private String fraudSignalsSummary;  // JSON or text summary

    // Decision
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewDecision decision;

    // If approved
    private Double approvedAmount;
    private Double interestRate;
    private Integer approvedTenureMonths;
    private Double processingFee;
    private Double emiAmount;

    // If rejected
    private String rejectionReason;
    private String rejectionCategory;   // KYC_FAILED, HIGH_RISK, FRAUD_DETECTED
    // INCOME_INSUFFICIENT, AGENT_REPORT_FAILED

    // If re-verify
    private String reVerifyReason;
    private String reVerifyInstructions; // what agent should re-check

    // Remarks
    private String officerRemarks;

    // Manager final-stage review
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Users manager;

    @Enumerated(EnumType.STRING)
    private ManagerDecision managerDecision;

    private LocalDateTime managerDecisionTakenAt;
    private String managerRemarks;

    // Review Timeline
    private LocalDateTime reviewStartedAt;
    private LocalDateTime decisionTakenAt;

    // Status of loan when review was done
    @Enumerated(EnumType.STRING)
    private LoanStatus loanStatusAtReview;

    // Flagged by manager after audit
    private Boolean isFlaggedByManager;
    private String managerFlagReason;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.decision = ReviewDecision.PENDING;
        this.managerDecision = ManagerDecision.PENDING;
        this.kycVerified = false;
        this.agentReportReviewed = false;
        this.incomeVerified = false;
        this.addressVerified = false;
        this.aiScoreChecked = false;
        this.fraudSignalsChecked = false;
        this.isFlaggedByManager = false;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}