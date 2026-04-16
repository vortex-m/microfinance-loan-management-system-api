package com.microfinance.loan.manager.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkAgentReassignResponse {
    private Long oldAgentUserId;
    private Long newAgentUserId;
    private Integer loanApplicationsUpdated;
    private Integer bookedLoansUpdated;
    private String reason;
}

