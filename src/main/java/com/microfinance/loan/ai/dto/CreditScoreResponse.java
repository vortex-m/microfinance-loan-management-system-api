package com.microfinance.loan.ai.dto;

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
public class CreditScoreResponse {
	private Long userId;
	private Double creditScore;
	private Double riskScore;
	private String riskLevel;
	private Integer maxPendingDays;
	private Integer overdueEmiCount;
	private LocalDateTime scoreUpdatedAt;
	private Boolean followUpTaskCreated;

}
