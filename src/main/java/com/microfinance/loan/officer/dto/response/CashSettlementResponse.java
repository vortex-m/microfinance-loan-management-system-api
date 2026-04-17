package com.microfinance.loan.officer.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CashSettlementResponse {
    private Integer settledPaymentsCount;
    private Double settledAmount;
    private String settlementReference;
    private LocalDateTime settledAt;
    private List<Long> settledPaymentIds;
}

