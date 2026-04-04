package com.microfinance.loan.agent.dto.response;

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
public class VerificationReportResponse {
	private Long reportId;
	private Long taskId;
	private Long loanApplicationId;
	private String reportSummary;
	private Boolean documentsMatched;
	private Boolean applicantAvailable;

	private Double cashCollectedAmount;
	private String cashCollectionRemarks;

	private LocalDateTime createdAt;
}
