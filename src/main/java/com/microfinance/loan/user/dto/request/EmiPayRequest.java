package com.microfinance.loan.user.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmiPayRequest {

    @NotNull(message = "Loan ID is required")
    private Long loanId;

    @NotNull(message = "EMI schedule ID is required")
    private Long emiScheduleId;         // which EMI being paid

    @NotNull(message = "Payment amount is required")
    @Min(value = 1, message = "Payment amount must be greater than 0")
    private Double paymentAmount;

    @NotBlank(message = "Payment mode is required")
    private String paymentMode;         // CASH, BANK_TRANSFER

    // Gateway info (filled after gateway response)
    private String gatewayOrderId;

    private String paymentReference;
}