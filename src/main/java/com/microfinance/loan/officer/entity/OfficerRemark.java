package com.microfinance.loan.officer.entity;

import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.user.entity.LoanApplication;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "officer_remarks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfficerRemark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Which loan this remark belongs to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_application_id", nullable = false)
    private LoanApplication loanApplication;

    // Who added the remark
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "officer_id", nullable = false)
    private Users officer;

    // Remark Details
    @Column(nullable = false, columnDefinition = "TEXT")
    private String remark;

    @Column(nullable = false)
    private String remarkType;          // GENERAL, KYC_NOTE, RISK_NOTE,
    // FRAUD_SUSPICION, APPROVAL_NOTE,
    // REJECTION_NOTE, RE_VERIFY_NOTE

    private String remarkStage;         // at which loan stage remark was added
    // KYC_REVIEW, AGENT_REVIEW,
    // AI_REVIEW, FINAL_DECISION

    // Visibility
    private Boolean visibleToUser;      // can applicant see this?
    private Boolean visibleToManager;   // always true for audit

    // Internal flag
    private Boolean isInternal;         // internal notes not shown to user

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.visibleToUser = false;
        this.visibleToManager = true;
        this.isInternal = true;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}