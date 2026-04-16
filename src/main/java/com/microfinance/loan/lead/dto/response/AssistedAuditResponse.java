package com.microfinance.loan.lead.dto.response;

import com.microfinance.loan.common.enums.AssistedActionType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssistedAuditResponse {
    private Long id;
    private String auditCode;
    private AssistedActionType actionType;
    private String remarks;
    private String metadata;
    private Long leadId;
    private Long userId;
    private Long performedByUserId;
    private String performedByName;
    private LocalDateTime createdAt;
}

