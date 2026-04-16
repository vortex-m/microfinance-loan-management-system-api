package com.microfinance.loan.manager.dto.response;

import com.microfinance.loan.common.enums.DisbursalMode;
import com.microfinance.loan.common.enums.LoanStatus;
import com.microfinance.loan.common.enums.OriginChannel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ManagerLoanResponse {
    private Long loanId;
    private Long loanApplicationId;
    private Long userId;
    private Long createdByAgentId;
    private Long assignedAgentId;
    private Long assignedOfficerId;
    private String applicationNumber;
    private LoanStatus status;
    private OriginChannel originChannel;
    private Double requestedAmount;
    private DisbursalMode disbursalMode;
    private String disbursalBankName;
    private String disbursalBankAccount;
    private String disbursalIfscCode;
}

