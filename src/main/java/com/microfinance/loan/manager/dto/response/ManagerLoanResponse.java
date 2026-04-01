package com.microfinance.loan.manager.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ManagerLoanResponse {
    private Long loanId;
    private String applicationNumber;
    private String status;
    private Double requestedAmount;
}

