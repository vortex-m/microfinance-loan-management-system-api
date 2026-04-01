package com.microfinance.loan.manager.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FraudAlertResponse {
    private Long alertId;
    private String alertCode;
    private String alertStatus;
    private String riskLevel;
}

