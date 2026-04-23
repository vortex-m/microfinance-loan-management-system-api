package com.microfinance.loan.officer.dto.response;

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
public class OfficerCashDisbursalQueueResponse {

    private Long taskId;
    private Long loanApplicationId;
    private String applicationNumber;
    private String applicantName;
    private String assignedAgentName;
    private String assignedAgentEmail;
    private Double loanAmount;
    private String otpStatus;
}
