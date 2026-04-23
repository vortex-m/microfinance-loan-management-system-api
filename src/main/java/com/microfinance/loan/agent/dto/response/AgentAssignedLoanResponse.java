package com.microfinance.loan.agent.dto.response;

import com.microfinance.loan.common.enums.DisbursalMode;
import com.microfinance.loan.common.enums.LoanStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentAssignedLoanResponse {
    private Long loanApplicationId;
    private String applicationNumber;
    private LoanStatus applicationStatus;
    private Double requestedAmount;
    private Double approvedAmount;
    private Integer tenureMonths;
    private String loanPurpose;
    private DisbursalMode disbursalMode;

    private Long userId;
    private String userName;
    private String userPhone;
    private String userEmail;

    private Long loanId;
    private String loanNumber;
    private LoanStatus loanStatus;
    private Double emiAmount;
    private Integer totalEmis;
    private Integer emisPending;
    private Integer emisOverdue;
    private Double totalPaidAmount;
    private Double outstandingPrincipal;
    private LocalDate nextDueDate;

    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;
}

