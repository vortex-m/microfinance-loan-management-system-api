package com.microfinance.loan.user.dto.response;

import com.microfinance.loan.common.enums.DisbursalMode;
import com.microfinance.loan.common.enums.LoanStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanStatusResponse {

    private List<LoanItem> loans;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LoanItem {
        private Long loanApplicationId;
        private String applicationNumber;
        private Long loanId;
        private String loanNumber;
        private Double requestedAmount;
        private Double approvedAmount;
        private Integer tenureMonths;
        private String loanPurpose;
        private DisbursalMode disbursalMode;
        private LoanStatus status;
        private Double emiAmount;
        private Double totalPaidAmount;
        private Double outstandingPrincipal;
        private LocalDate nextDueDate;
        private String rejectionReason;
        private LocalDateTime appliedAt;
        private LocalDateTime updatedAt;
    }
}