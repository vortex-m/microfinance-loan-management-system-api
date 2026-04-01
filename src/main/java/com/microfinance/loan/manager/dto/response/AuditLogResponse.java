package com.microfinance.loan.manager.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AuditLogResponse {
    private Long auditId;
    private String auditCode;
    private String actionType;
    private LocalDateTime createdAt;
}

