package com.microfinance.loan.loan.entity;

import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.common.enums.LoanStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "loan_status_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Which loan
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    // Status Change
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus newStatus;

    // Who changed it
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    private Users changedBy;

    private String changedByRole;       // ROLE_OFFICER, ROLE_SYSTEM etc.

    // Why it changed
    private String changeReason;
    private String remarks;

    // System or Manual
    private Boolean isSystemGenerated;  // true = auto by system
    // false = manual by user/officer

    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.isSystemGenerated = false;
    }
}