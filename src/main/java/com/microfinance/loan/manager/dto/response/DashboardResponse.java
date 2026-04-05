package com.microfinance.loan.manager.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardResponse {
    private Integer totalPendingLoans;
    private Integer totalFraudAlerts;
    private Integer totalOpenAudits;
    private Integer totalActiveStaff;
}
