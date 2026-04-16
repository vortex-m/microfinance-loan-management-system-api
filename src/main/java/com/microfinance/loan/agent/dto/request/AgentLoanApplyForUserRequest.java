package com.microfinance.loan.agent.dto.request;

import com.microfinance.loan.common.enums.DisbursalMode;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
public class AgentLoanApplyForUserRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Requested amount is required")
    @Min(value = 1000, message = "Minimum loan amount is 1000")
    @Max(value = 200000, message = "Maximum loan amount is 200000")
    private Double requestedAmount;

    @NotNull(message = "Tenure is required")
    @Min(value = 1, message = "Tenure must be at least 1 month")
    @Max(value = 36, message = "Tenure cannot exceed 36 months")
    private Integer tenureMonths;

    @NotBlank(message = "Loan purpose is required")
    private String loanPurpose;

    private String loanPurposeDescription;

    @NotBlank(message = "User remarks are required")
    private String userRemarks;

    @NotNull(message = "Disbursal mode is required")
    private DisbursalMode disbursalMode;

    private String disbursalBankName;
    private String disbursalBankAccount;

    @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$", message = "Invalid IFSC code format")
    private String disbursalIfscCode;

    private String disbursalBankProofUrl;
    private String disbursalBankProofFileName;
}

