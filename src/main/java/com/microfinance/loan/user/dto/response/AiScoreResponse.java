package com.microfinance.loan.user.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiScoreResponse {

    // Credit Score
    private Double creditScore;         // 300 - 900
    private String creditRating;        // POOR, FAIR, GOOD, VERY_GOOD, EXCELLENT

    // Risk Score
    private Double riskScore;           // 0.0 - 1.0
    private String riskLevel;           // LOW, MEDIUM, HIGH, CRITICAL

    // Eligibility
    private Boolean isEligible;
    private Double maxEligibleAmount;
    private String eligibilityReason;

    // Last updated
    private LocalDateTime scoreUpdatedAt;

    private String message;
}