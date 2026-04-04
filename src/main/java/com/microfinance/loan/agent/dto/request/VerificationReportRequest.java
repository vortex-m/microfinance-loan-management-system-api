package com.microfinance.loan.agent.dto.request;

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
public class VerificationReportRequest {
	@NotNull(message = "Task id is required")
	private Long taskId;

	@NotBlank(message = "Report summary is required")
	private String reportSummary;

	private String residenceRemarks;
	private String businessRemarks;
	private Boolean documentsMatched;
	private Boolean applicantAvailable;

	// Used when the task type is CASH_COLLECTION.
	@DecimalMin(value = "0.0", inclusive = false, message = "Collected amount must be greater than 0")
	private Double cashCollectedAmount;
	private String cashCollectionRemarks;
}
