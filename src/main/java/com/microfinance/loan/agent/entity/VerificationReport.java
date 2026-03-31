package com.microfinance.loan.agent.entity;

import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.common.enums.VerificationStatus;
import com.microfinance.loan.user.entity.LoanApplication;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "verification_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String reportCode;          // e.g. VR-00123

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_application_id", nullable = false, unique = true)
    private LoanApplication loanApplication;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false)
    private Users agent;

    // Physical Verification Details
    private Boolean applicantPresent;
    private Boolean addressVerified;
    private Boolean businessVerified;   // if loan is for business
    private Boolean incomeVerified;

    // Location at time of visit
    private Double visitLatitude;
    private Double visitLongitude;
    private String visitAddress;        // reverse geocoded address

    // Verification Outcome
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus verificationStatus;

    // Remarks
    private String agentRemarks;
    private String environmentObservation;  // neighbourhood, living conditions
    private String businessObservation;     // if applicable

    // Risk flags by agent
    private Boolean addressMismatch;
    private Boolean suspiciousActivity;
    private Boolean documentTampering;
    private String riskNotes;

    // Submitted info
    private LocalDateTime visitedAt;
    private LocalDateTime submittedAt;
    private Boolean isSubmitted;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.verificationStatus = VerificationStatus.PENDING;
        this.isSubmitted = false;
        this.addressMismatch = false;
        this.suspiciousActivity = false;
        this.documentTampering = false;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}