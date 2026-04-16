package com.microfinance.loan.lead.dto.request;

import com.microfinance.loan.common.enums.OfficerLeadDecision;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfficerLeadDecisionRequest {
    @NotNull(message = "Decision is required")
    private OfficerLeadDecision decision;

    private String remarks;
}

