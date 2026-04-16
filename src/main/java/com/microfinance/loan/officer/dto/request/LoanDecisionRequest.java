package com.microfinance.loan.officer.dto.request;

import com.microfinance.loan.common.enums.ReviewDecision;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
public class LoanDecisionRequest {
	@NotNull(message = "Decision is required")
	private ReviewDecision decision;

	private String officerRemarks;
	private String rejectionReason;
	private String reVerifyReason;

	@DecimalMin(value = "1000.0", message = "Approved amount must be at least 1000")
	private Double approvedAmount;

	@Min(value = 1, message = "Tenure must be at least 1 month")
	@Max(value = 36, message = "Tenure cannot exceed 36 months")
	private Integer approvedTenureMonths;
}
