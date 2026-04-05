package com.microfinance.loan.admin.dto.response;

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
public class BranchMetricsResponse {
    private String branchCode;
    private String branchName;
    private String regionCode;
    private String regionName;
    private Boolean active;

    private Long totalUsers;
    private Long activeUsers;
    private Long inactiveUsers;

    private Long totalOfficers;
    private Long activeOfficers;
    private Long totalAgents;
    private Long activeAgents;

    private Long totalLoans;
    private Long pendingLoans;
    private Long approvedLoans;
    private Long rejectedLoans;
    private Long disbursedLoans;
    private Long closedLoans;
    private Long activeLoans;

    private Double totalLoanAmount;
}

