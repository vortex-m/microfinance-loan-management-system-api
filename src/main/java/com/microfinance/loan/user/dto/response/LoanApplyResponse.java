package com.microfinance.loan.user.dto.response;

import com.microfinance.loan.common.enums.DisbursalMode;
import com.microfinance.loan.common.enums.LoanStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApplyResponse {
    private Long loanApplicationId;
    private String applicationNumber;
    private LoanStatus status;

    private Double requestedAmount;
    private Integer tenureMonths;
    private String loanPurpose;

    private DisbursalMode disbursalMode;
    private String disbursalBankName;
    private String disbursalBankAccount;
    private String disbursalIfscCode;
    private String disbursalBankProofUrl;
    private String disbursalBankProofFileName;

    private LocalDateTime appliedAt;
}
