package com.microfinance.loan.user.dto.response;

import com.microfinance.loan.common.enums.LoanStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanDetailResponse {

    // Application info
    private Long loanApplicationId;
    private String applicationNumber;
    private LoanStatus status;

    // Request details
    private Double requestedAmount;
    private Integer tenureMonths;
    private String loanPurpose;
    private String loanPurposeDescription;
    private String userRemarks;

    // Approved details (null if not yet approved)
    private String loanNumber;
    private Double approvedAmount;
    private Double interestRate;
    private String interestType;
    private Double emiAmount;
    private Double processingFee;
    private Double totalInterestPayable;
    private Double totalAmountPayable;

    // EMI tracking
    private Integer totalEmis;
    private Integer emisPaid;
    private Integer emisPending;
    private Integer emisOverdue;
    private Double outstandingPrincipal;
    private Double totalPaidAmount;

    // Dates
    private LocalDate disbursementDate;
    private LocalDate firstEmiDate;
    private LocalDate lastEmiDate;

    // Remarks
    private String officerRemarks;
    private String rejectionReason;

    private LocalDateTime appliedAt;
    private LocalDateTime disbursedAt;
}