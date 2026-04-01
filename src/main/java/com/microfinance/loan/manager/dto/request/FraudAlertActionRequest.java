package com.microfinance.loan.manager.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FraudAlertActionRequest {
    private String action;
    private String remarks;
}

