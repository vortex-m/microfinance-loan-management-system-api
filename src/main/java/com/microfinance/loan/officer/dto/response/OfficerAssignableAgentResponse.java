package com.microfinance.loan.officer.dto.response;

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
public class OfficerAssignableAgentResponse {

    private Long agentUserId;
    private String agentCode;
    private String name;
    private String phone;
}
