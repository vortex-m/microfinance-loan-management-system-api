package com.microfinance.loan.agent.entity;

import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.common.enums.CashOtpStatus;
import com.microfinance.loan.loan.entity.Loan;
import com.microfinance.loan.loan.entity.LoanEmiSchedule;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cash_collection_otps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashCollectionOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emi_schedule_id", nullable = false)
    private LoanEmiSchedule emiSchedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false)
    private Users agent;

    @Column(nullable = false)
    private String otpHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CashOtpStatus otpStatus;

    @Column(nullable = false)
    private Integer attemptCount;

    @Column(nullable = false)
    private Integer maxAttempts;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime verifiedAt;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.otpStatus == null) {
            this.otpStatus = CashOtpStatus.ACTIVE;
        }
        if (this.attemptCount == null) {
            this.attemptCount = 0;
        }
        if (this.maxAttempts == null) {
            this.maxAttempts = 3;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

