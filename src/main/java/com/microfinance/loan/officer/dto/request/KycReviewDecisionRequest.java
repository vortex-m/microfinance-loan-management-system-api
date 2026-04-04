package com.microfinance.loan.officer.dto.request;

import com.microfinance.loan.common.enums.KycStatus;
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
public class KycReviewDecisionRequest {
	@NotNull(message = "reviewStatus is required")
	private KycStatus reviewStatus;

	@NotBlank(message = "Review remarks are required")
	private String reviewRemarks;

	private String rejectionReason;
	private Boolean resubmissionRequested;
	private String resubmissionInstructions;
}
