package com.microfinance.loan.payment.entity;

import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.loan.entity.Loan;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_receipts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String receiptNumber;           // e.g. RCP-2024-00123

    // Linked payment
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false, unique = true)
    private Payment payment;

    // Linked loan & user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    // Receipt Details (snapshot at time of payment)
    private String userName;
    private String userPhone;
    private String loanNumber;
    private Integer emiNumber;
    private Double emiAmount;
    private Double paidAmount;
    private Double penaltyAmount;
    private Double outstandingAfterPayment;
    private Integer emisRemainingAfterPayment;

    // Payment Info Snapshot
    private String paymentMode;
    private String transactionId;
    private LocalDateTime paidAt;

    // Receipt File
    private String receiptFileUrl;          // generated PDF receipt URL
    private Boolean isGenerated;            // PDF generated or not

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isGenerated = false;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}