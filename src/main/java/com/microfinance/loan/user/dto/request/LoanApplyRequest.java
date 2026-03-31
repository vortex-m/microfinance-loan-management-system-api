package com.microfinance.loan.user.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApplyRequest {

    @NotNull(message = "Requested amount is required")
    @Min(value = 1000, message = "Minimum loan amount is 1000")
    @Max(value = 500000, message = "Maximum loan amount is 500000")
    private Double requestedAmount;

    @NotNull(message = "Tenure is required")
    private Integer tenureMonths;       // 6, 12, 24, 36

    @NotBlank(message = "Loan purpose is required")
    private String loanPurpose;         // BUSINESS, EDUCATION, MEDICAL,
    // HOME, AGRICULTURE, OTHER

    private String loanPurposeDescription;

    @NotBlank(message = "User remarks are required")
    private String userRemarks;
}