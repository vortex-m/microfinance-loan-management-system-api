package com.microfinance.loan.agent.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

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

    @NotNull(message = "Collection planned time is required")
    private LocalDateTime collectionPlannedAt;

    @NotNull(message = "Agent latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
    @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
    private Double agentLatitude;

    @NotNull(message = "Agent longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
    @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
    private Double agentLongitude;
}

