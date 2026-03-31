package com.microfinance.loan.user.entity;

import com.microfinance.loan.common.entity.Users;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "emi_payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmiPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String paymentReference;    // e.g. PAY-2024-00456

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_application_id", nullable = false)
    private LoanApplication loanApplication;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    // EMI Details
    @Column(nullable = false)
    private Integer emiNumber;          // which EMI (1st, 2nd... Nth)

    @Column(nullable = false)
    private Double emiAmount;

    private Double principalComponent;
    private Double interestComponent;
    private Double penaltyAmount;       // if paid late

    @Column(nullable = false)
    private LocalDate dueDate;

    private LocalDate paidDate;

    @Column(nullable = false)
    private String paymentStatus;       // PENDING, PAID, OVERDUE, PARTIAL

    private String paymentMode;         // UPI, NETBANKING, CASH, CHEQUE
    private String transactionId;       // gateway transaction id
    private String gatewayResponse;

    private Double outstandingBalance;  // remaining after this payment

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.paymentStatus = "PENDING";
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}