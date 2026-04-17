package com.microfinance.loan.agent.dto.request;

import com.microfinance.loan.common.enums.AgentAvailability;
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
public class AgentAvailabilityUpdateRequest {

    @NotNull(message = "Availability is required")
    private AgentAvailability availability;
}

