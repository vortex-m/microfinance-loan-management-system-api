package com.microfinance.loan.payment.entity;

import com.microfinance.loan.agent.entity.CashCollectionOtp;
import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.common.enums.PaymentStatus;
import com.microfinance.loan.loan.entity.Loan;
import com.microfinance.loan.loan.entity.LoanEmiSchedule;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String paymentNumber;           // e.g. PAY-2024-00123

    // Which loan & EMI
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emi_schedule_id", nullable = false)
    private LoanEmiSchedule emiSchedule;    // which EMI being paid

    // Who paid
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    // Payment Breakdown
    @Column(nullable = false)
    private Double totalPaidAmount;         // actual amount paid by user

    private Double principalPaid;
    private Double interestPaid;
    private Double penaltyPaid;             // if any penalty included
    private Double excessAmount;            // if user paid more than EMI

    // Payment Mode
    @Column(nullable = false)
    private String paymentMode;             // CASH, BANK_TRANSFER

    // Gateway Info
    private String gatewayName;             // RAZORPAY, PAYTM, CASHFREE
    private String gatewayTransactionId;    // from payment gateway
    private String gatewayOrderId;
    private String gatewayResponse;         // full JSON response
    private String gatewayStatus;           // SUCCESS, FAILED, PENDING

    // Optional reference for manual/bank transfer confirmation.
    private String paymentReference;

    // Payment Status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;    // INITIATED, SUCCESS,
    // FAILED, PENDING, REFUNDED

    // Timestamps
    private LocalDateTime initiatedAt;
    private LocalDateTime successAt;
    private LocalDateTime failedAt;

    private String failureReason;

    // Verified by (for cash payments)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by_agent_id")
    private Users verifiedBy;               // agent who verified cash collection

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cash_otp_id")
    private CashCollectionOtp cashCollectionOtp;

    private LocalDateTime verifiedAt;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.paymentStatus = PaymentStatus.INITIATED;
        this.initiatedAt = LocalDateTime.now();
        this.excessAmount = 0.0;
        this.penaltyPaid = 0.0;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}