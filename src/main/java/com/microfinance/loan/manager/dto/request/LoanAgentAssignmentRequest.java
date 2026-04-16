package com.microfinance.loan.manager.dto.request;

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
public class LoanAgentAssignmentRequest {

    @NotNull(message = "Agent user ID is required")
    private Long agentUserId;

    @NotBlank(message = "Reason is required")
    private String reason;
}

