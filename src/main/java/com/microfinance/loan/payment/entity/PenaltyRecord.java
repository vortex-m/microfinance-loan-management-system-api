package com.microfinance.loan.payment.entity;

import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.common.enums.PenaltyStatus;
import com.microfinance.loan.loan.entity.Loan;
import com.microfinance.loan.loan.entity.LoanEmiSchedule;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "penalty_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PenaltyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String penaltyCode;             // e.g. PNL-00123

    // Which loan & EMI
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emi_schedule_id", nullable = false)
    private LoanEmiSchedule emiSchedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    // Penalty Details
    @Column(nullable = false)
    private LocalDate emiDueDate;           // original due date

    @Column(nullable = false)
    private Integer daysOverdue;            // how many days late

    @Column(nullable = false)
    private Double penaltyRate;             // % per day or flat

    private String penaltyType;             // DAILY_PERCENTAGE, FLAT, TIERED

    @Column(nullable = false)
    private Double penaltyAmount;           // total penalty charged

    // Status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PenaltyStatus penaltyStatus;    // CHARGED, PAID, WAIVED, PARTIAL

    private Double paidAmount;
    private Double waivedAmount;
    private Double remainingPenalty;

    // Waiver Info (if waived by officer/manager)
    private Boolean isWaived;
    private String waiverReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "waived_by")
    private Users waivedBy;

    private LocalDateTime waivedAt;

    // Payment linked
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;                // payment through which penalty was paid

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.penaltyStatus = PenaltyStatus.CHARGED;
        this.paidAmount = 0.0;
        this.waivedAmount = 0.0;
        this.isWaived = false;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}