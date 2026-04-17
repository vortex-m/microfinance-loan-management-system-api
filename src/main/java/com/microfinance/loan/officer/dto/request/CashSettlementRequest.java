package com.microfinance.loan.officer.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CashSettlementRequest {

    @NotEmpty(message = "At least one payment id is required")
    @Size(max = 200, message = "Maximum 200 payments can be settled at once")
    private List<Long> paymentIds;

    @Size(max = 100, message = "Settlement reference must be at most 100 characters")
    private String settlementReference;
}

