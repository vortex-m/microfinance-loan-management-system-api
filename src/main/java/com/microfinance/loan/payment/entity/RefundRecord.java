package com.microfinance.loan.payment.entity;

import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.common.enums.RefundStatus;
import com.microfinance.loan.loan.entity.Loan;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "refund_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String refundCode;              // e.g. RFD-00123

    // Linked payment that needs refund
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    // Refund Details
    @Column(nullable = false)
    private Double refundAmount;

    @Column(nullable = false)
    private String refundReason;            // EXCESS_PAYMENT, LOAN_REJECTED,
    // DUPLICATE_PAYMENT, PROCESSING_FEE_REFUND

    private String refundMethod;            // ORIGINAL_SOURCE, BANK_TRANSFER, UPI

    // Bank Details for refund
    private String refundBankAccount;
    private String refundBankName;
    private String refundIfscCode;
    private String refundUpiId;

    // Status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefundStatus refundStatus;      // REQUESTED, APPROVED,
    // PROCESSING, COMPLETED, REJECTED

    // Gateway Refund Info
    private String gatewayRefundId;
    private String gatewayRefundStatus;
    private LocalDateTime gatewayProcessedAt;

    // Approval
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private Users approvedBy;               // officer/manager who approved

    private LocalDateTime approvedAt;
    private LocalDateTime processedAt;
    private LocalDateTime completedAt;

    private String rejectionReason;         // if refund rejected

    private String remarks;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.refundStatus = RefundStatus.REQUESTED;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}