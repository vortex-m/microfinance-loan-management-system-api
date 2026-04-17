package com.microfinance.loan.agent.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
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
public class CashOtpVerifyRequest {

    @NotNull(message = "OTP record id is required")
    private Long otpId;

    @NotNull(message = "Task id is required")
    private Long taskId;

    @NotNull(message = "Collection amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Collection amount must be greater than 0")
    private Double collectionAmount;

    @NotBlank(message = "OTP is required")
    private String otp;

    @NotNull(message = "Agent latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
    @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
    private Double agentLatitude;

    @NotNull(message = "Agent longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
    @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
    private Double agentLongitude;
}

