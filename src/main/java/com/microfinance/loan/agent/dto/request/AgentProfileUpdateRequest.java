package com.microfinance.loan.agent.dto.request;


import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentProfileUpdateRequest {

    @NotBlank(message = "Name Should not be blank.")
    private String name;
}
