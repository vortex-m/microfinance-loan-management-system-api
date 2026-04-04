package com.microfinance.loan.user.dto.response;

import com.microfinance.loan.common.enums.DisbursalMode;
import com.microfinance.loan.common.enums.LoanStatus;
import lombok.*;

import java.time.LocalDateTime;
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
        private Double requestedAmount;
        private Integer tenureMonths;
        private String loanPurpose;
        private DisbursalMode disbursalMode;
        private LoanStatus status;
        private String rejectionReason;
        private LocalDateTime appliedAt;
        private LocalDateTime updatedAt;
    }
}