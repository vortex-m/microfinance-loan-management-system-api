package com.microfinance.loan.user.dto.response;

import com.microfinance.loan.common.enums.KycStatus;
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
public class UserDashboardResponse {
    private Long userId;
    private Boolean isHome;
    private KycStatus kycStatus;
    private Long totalApplications;
    private Long pendingApplications;
    private Long approvedApplications;
    private Long disbursedApplications;
    private Long totalLoans;
    private Integer totalEmis;
    private Integer paidEmis;
    private Integer overdueEmis;
    private Double totalOutstandingPrincipal;
}
