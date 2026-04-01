package com.microfinance.loan.manager.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuditFlagRequest {
    private Boolean flagged;
    private String reason;
}

