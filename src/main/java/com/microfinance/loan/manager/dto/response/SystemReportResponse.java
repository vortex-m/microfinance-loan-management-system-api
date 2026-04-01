package com.microfinance.loan.manager.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SystemReportResponse {
    private Long reportId;
    private String reportCode;
    private String reportType;
    private String reportStatus;
    private String exportFileUrl;
    private LocalDateTime generatedAt;
}

