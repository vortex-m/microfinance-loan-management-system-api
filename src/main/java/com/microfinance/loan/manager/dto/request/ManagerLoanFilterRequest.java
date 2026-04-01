package com.microfinance.loan.manager.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ManagerLoanFilterRequest {
    private String loanStatus;
    private Long officerId;
    private Long agentId;
}

