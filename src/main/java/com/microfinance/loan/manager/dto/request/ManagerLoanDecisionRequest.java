package com.microfinance.loan.manager.dto.request;

import com.microfinance.loan.common.enums.ManagerDecision;
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
public class ManagerLoanDecisionRequest {
    @NotNull(message = "Manager decision is required")
    private ManagerDecision decision;

    private String managerRemarks;
    private String rejectionReason;

    private Long agentUserId;
}

