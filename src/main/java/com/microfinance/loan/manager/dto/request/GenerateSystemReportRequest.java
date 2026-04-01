package com.microfinance.loan.manager.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class GenerateSystemReportRequest {
    private String reportType;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String exportFormat;
}

