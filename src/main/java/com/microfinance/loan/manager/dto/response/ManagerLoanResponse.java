package com.microfinance.loan.manager.dto.response;

import com.microfinance.loan.common.enums.DisbursalMode;
import com.microfinance.loan.common.enums.LoanStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ManagerLoanResponse {
    private Long loanId;
    private String applicationNumber;
    private LoanStatus status;
    private Double requestedAmount;
    private DisbursalMode disbursalMode;
    private String disbursalBankName;
    private String disbursalBankAccount;
    private String disbursalIfscCode;
}

