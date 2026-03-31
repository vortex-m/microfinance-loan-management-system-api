package com.microfinance.loan.manager.entity;

import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.user.entity.LoanApplication;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String auditCode;           // e.g. AUD-00123

    // Who performed the action
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by", nullable = false)
    private Users performedBy;          // officer / agent / user

    private String performedByRole;     // ROLE_OFFICER, ROLE_AGENT

    // Which loan it's related to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_application_id")
    private LoanApplication loanApplication;

    // Action Details
    @Column(nullable = false)
    private String actionType;          // LOAN_APPROVED, LOAN_REJECTED,
    // KYC_VERIFIED, VERIFICATION_SUBMITTED
    // EMI_PAID, FRAUD_FLAGGED 

    private String actionDescription;   // human readable description

    // Before & After State (for full trail)
    private String previousState;       // e.g. PENDING
    private String newState;            // e.g. APPROVED

    // Extra context
    private String remarks;
    private String ipAddress;           // from where action was performed
    private String deviceInfo;          // browser/device info

    // Flagged by manager
    private Boolean isFlagged;          // manager marked this suspicious
    private String flagReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flagged_by")
    private Users flaggedBy;            // manager who flagged

    private LocalDateTime flaggedAt;

    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.isFlagged = false;
    }
}