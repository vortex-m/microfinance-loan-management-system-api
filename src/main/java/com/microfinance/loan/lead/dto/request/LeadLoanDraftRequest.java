package com.microfinance.loan.lead.dto.request;

import com.microfinance.loan.common.enums.DisbursalMode;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadLoanDraftRequest {
    @NotNull(message = "Requested amount is required")
    @Min(value = 1, message = "Requested amount must be greater than 0")
    private Double requestedAmount;

    @NotNull(message = "Tenure is required")
    @Min(value = 1)
    @Max(value = 36)
    private Integer tenureMonths;

    @NotBlank(message = "Loan purpose is required")
    private String loanPurpose;

    @NotNull(message = "Disbursal mode is required")
    private DisbursalMode disbursalMode;

    private String disbursalBankName;
    private String disbursalBankAccount;
    private String disbursalIfscCode;
}

