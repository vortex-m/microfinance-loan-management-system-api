package com.microfinance.loan.manager.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardResponse {
    private Integer totalPendingLoans;
    private Integer totalFraudAlerts;
    private Integer totalOpenAudits;
    private Integer totalActiveStaff;
    private Integer pendingOfficerReview;
    private Integer pendingManagerApproval;
    private Integer approvedLoans;
    private Integer disbursedLoans;
    private Integer rejectedLoans;
    private Integer closedLoans;
    private Integer activeUsers;
    private Integer activeOfficers;
    private Integer activeAgents;
    private Double totalPortfolioAmount;
    private Double totalCollectedCash;
    private Double totalSettledCash;
    private Double totalUnsettledCash;
}
