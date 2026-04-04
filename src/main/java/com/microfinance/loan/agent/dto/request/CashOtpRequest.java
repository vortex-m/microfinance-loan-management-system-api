package com.microfinance.loan.agent.dto.request;

import jakarta.validation.constraints.DecimalMin;
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
public class CashOtpRequest {

    @NotNull(message = "Task id is required")
    private Long taskId;

    @NotNull(message = "Loan id is required")
    private Long loanId;

    @NotNull(message = "EMI schedule id is required")
    private Long emiScheduleId;

    @NotNull(message = "Collection amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Collection amount must be greater than 0")
    private Double collectionAmount;
}

