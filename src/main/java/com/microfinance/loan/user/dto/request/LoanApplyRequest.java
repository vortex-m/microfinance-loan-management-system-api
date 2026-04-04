package com.microfinance.loan.user.dto.request;

import com.microfinance.loan.common.enums.DisbursalMode;
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
    @Max(value = 200000, message = "Maximum loan amount is 200000")
    private Double requestedAmount;

    @NotNull(message = "Tenure is required")
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