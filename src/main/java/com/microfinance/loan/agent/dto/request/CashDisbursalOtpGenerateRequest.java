package com.microfinance.loan.agent.dto.request;

import jakarta.validation.constraints.NotNull;
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
public class CashDisbursalOtpGenerateRequest {

    @NotNull(message = "Loan application id is required")
    private Long loanApplicationId;
}
