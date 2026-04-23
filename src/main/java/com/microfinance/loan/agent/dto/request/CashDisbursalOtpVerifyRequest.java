package com.microfinance.loan.agent.dto.request;

import jakarta.validation.constraints.NotBlank;
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
public class CashDisbursalOtpVerifyRequest {

    @NotNull(message = "Task id is required")
    private Long taskId;

    @NotBlank(message = "OTP is required")
    private String otp;
}
