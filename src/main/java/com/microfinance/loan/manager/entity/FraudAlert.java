package com.microfinance.loan.manager.entity;

import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.common.enums.FraudAlertStatus;
import com.microfinance.loan.user.entity.LoanApplication;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_alerts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String alertCode;           // e.g. FRD-00123

    // Which loan triggered the alert
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_application_id", nullable = false)
    private LoanApplication loanApplication;

    // Who is suspected
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suspected_user_id")
    private Users suspectedUser;

    // Alert Source
    @Column(nullable = false)
    private String alertSource;         // AI_SYSTEM, AGENT_REPORT,
    // OFFICER_FLAG, SYSTEM_RULE

    @Column(nullable = false)
    private String alertType;           // DUPLICATE_KYC, ADDRESS_MISMATCH,
    // INCOME_MISMATCH, MULTIPLE_LOANS,
    // SUSPICIOUS_DOCS, HIGH_RISK_SCORE

    @Column(nullable = false)
    private String alertDescription;

    private String riskLevel;           // LOW, MEDIUM, HIGH, CRITICAL

    private Double riskScore;           // AI risk score at time of alert

    // Status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FraudAlertStatus alertStatus;  // OPEN, ACKNOWLEDGED,
    // INVESTIGATING, RESOLVED, DISMISSED

    // Manager Action
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acknowledged_by")
    private Users acknowledgedBy;

    private LocalDateTime acknowledgedAt;
    private String managerRemarks;
    private String resolutionNotes;
    private LocalDateTime resolvedAt;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.alertStatus = FraudAlertStatus.OPEN;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}