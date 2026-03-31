package com.microfinance.loan.loan.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_emi_schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanEmiSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Which loan
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    // EMI Details
    @Column(nullable = false)
    private Integer emiNumber;          // 1, 2, 3 ... N

    @Column(nullable = false)
    private LocalDate dueDate;          // when this EMI is due

    @Column(nullable = false)
    private Double emiAmount;           // total EMI amount

    @Column(nullable = false)
    private Double principalComponent;  // principal portion

    @Column(nullable = false)
    private Double interestComponent;   // interest portion

    private Double outstandingPrincipal; // remaining after this EMI

    // Payment Status
    @Column(nullable = false)
    private String emiStatus;           // PENDING, PAID, OVERDUE,
    // PARTIALLY_PAID, WAIVED

    // Actual Payment Info (filled when paid)
    private Double paidAmount;
    private LocalDate paidDate;
    private String paymentReference;    // links to emi_payments table

    // Penalty
    private Double penaltyAmount;       // charged if paid late
    private Double penaltyPaid;
    private Integer daysOverdue;

    // Partial Payment
    private Double partialPaidAmount;
    private Double remainingAmount;     // after partial payment

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.emiStatus = "PENDING";
        this.paidAmount = 0.0;
        this.penaltyAmount = 0.0;
        this.penaltyPaid = 0.0;
        this.daysOverdue = 0;
        this.partialPaidAmount = 0.0;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}